#!/usr/bin/env python
# -*- coding:utf-8 -*-
# author: weiwei xie

import numpy as np
import scipy.signal as signal
from sklearn.cluster import KMeans
from collections import OrderedDict
import librosa

def disteu(x, y):
    M = x.shape[0]
    N = x.shape[1]
    M2 = y.shape[0]
    P = y.shape[1]

    d = np.zeros((N,P), dtype=np.float32)

    if N<P:
        copies = np.zeros(P,dtype=np.int32)
        for n in range(N):
            d[n,:]= np.sum(np.power((x[:,n+copies]-y),2),axis=0)
    else:
        copies = np.zeros(N, dtype=np.int32)
        for p in range(P):
            temp = np.sum(np.power((x-y[:, p + copies]),2),axis=0)
            d[:,p] = temp.T
    d = np.power(d,0.5)
    return d

def vqlbg(d,k):
    e = 0.01
    r = np.mean(d,axis=1)
    r = r.reshape((r.shape[0],1))
    dpr = 10000.0
    loop_cnt = int(np.log2(k))

    output_r = np.zeros((r.shape[0], k), dtype=np.float32)
    available_cols = 0
    for i in range(loop_cnt):
        r = np.concatenate((r * (1 + e), r * (1 - e)), axis=1)
        whileFlag = 0

        while 1:
            if whileFlag != 0:
                r = output_r[:, 0:available_cols]
            z = disteu(d, r)
            ind = np.argmin(z,axis = 1)
            t=0

            available_cols = np.power(2,i+1)
            for j in range(np.power(2,i+1)):
                equal_ind = np.where(ind == j)
                equal_ind = equal_ind[0]
                temp = d[:,equal_ind]
                temp_r = np.mean(temp,axis=1)
                output_r[:,j] = temp_r

                temp_r = temp_r.reshape((r.shape[0], 1))
                x = disteu(temp,temp_r)
                t = t+np.sum(x)
            if ((dpr-t)/t)<e:
                break
            else:
                dpr = t

            whileFlag = 1
    return output_r

#voice activity detect
def enframe(x,framelen, frameshift):
    xlen = len(x)
    nf = (int)((xlen-framelen+frameshift)/frameshift)
    f = np.zeros((nf,framelen), dtype=np.float32)
    indf = frameshift * (np.arange(0,nf)).reshape(nf,1)
    inds = np.arange(0,framelen).reshape(1,framelen)
    indall = np.tile(indf, (1, framelen))+np.tile(inds, (nf, 1))
    f = x[indall]
    return f

def vad(x, framelen=None, sr=None, frameshift=None, plot=False):
    if sr is None:
        sr = 16000
    if framelen is None:
        framelen = 256
    if frameshift is None:
        frameshift = 128
    amp_th1 = 8
    amp_th2 = 20
    zcr_th = 5

    maxsilence = 8
    minlen = 15
    status = 0
    count = 0
    silence = 0

    x = x/np.absolute(x).max()

    tmp1 = enframe(x[0:(len(x)-1)], framelen, frameshift)
    tmp2 = enframe(x[1:(len(x)-1)], framelen, frameshift)
    signs = (tmp1 * tmp2) < 0
    diffs = (tmp1 - tmp2) > 0.05
    zcr = np.sum(signs * diffs, axis=1)

    filter_coeff = np.array([1, -0.9375])
    pre_emphasis = signal.convolve(x,filter_coeff)[0:len(x)]
    amp = np.sum(np.absolute(enframe(pre_emphasis, framelen, frameshift)), axis=1)

    amp_th1 = min(amp_th1, amp.max()/ 3)
    amp_th2 = min(amp_th2, amp.max() / 8)

    x1 = []
    x2 = []
    t = 0

    for n in range(len(zcr)):
        if status == 0 or status == 1:
            if amp[n] > amp_th1:
                x1.append(max(n - count - 1, 1))
                status = 2
                silence = 0
                count = count + 1
            elif amp[n] > amp_th2 or zcr[n]>zcr_th:
                status = 1
                count = count + 1
            else:
                status = 0
                count = 0
            continue
        if status == 2:
            if amp[n] > amp_th2 or zcr[n]>zcr_th:
                count = count + 1
            else:
                silence = silence + 1
                if silence < maxsilence:
                    count = count + 1
                elif count < minlen:
                    status = 0
                    silence = 0
                    count = 0
                else:
                    status = 0
                    count = count - silence / 2
                    x2.append(x1[t] + count - 1)
                    t = t + 1

    return x1, x2

#speech segmentation
# Use the feature of the first segment to be the baseline, and loop over all the other
# segments to compute the BIC distance between each one and the baseline. Set a BIC
# distance threshold, decide whether this segment is in the same cluster with the baseline
def cluster_greedy(feature_vectors, cluster_list):
    current_cluster_number = len(cluster_list)
    for index, key in enumerate(feature_vectors.keys()):
        if index == 0:
            base_feature = feature_vectors[key]
            cluster_list[str(current_cluster_number)]=list()
            temp = cluster_list[str(current_cluster_number)]
            temp.append(key)
            cluster_list[str(current_cluster_number)] = temp
        else:
            bic_dis = cluter_on_bic(base_feature, feature_vectors[key])
            if bic_dis < 50:
                temp = cluster_list[str(current_cluster_number)]
                temp.append(key)
                cluster_list[str(current_cluster_number)] = temp

    # Delete the segment related features if they are already clustered.
    for i in cluster_list[str(current_cluster_number)]:
        feature_vectors.pop(i)

# Compute BIC distance between two MFCC features
def cluter_on_bic(mfcc_s1, mfcc_s2):
    mfcc_s = np.concatenate((mfcc_s1, mfcc_s2), axis=1)

    m, n = mfcc_s.shape
    m, n1 = mfcc_s1.shape
    m, n2 = mfcc_s2.shape

    sigma0 = np.cov(mfcc_s).diagonal()
    eps = np.spacing(1)
    realmin = np.finfo(np.double).tiny
    det0 = max(np.prod(np.maximum(sigma0,eps)),realmin)

    part1 = mfcc_s1
    part2 = mfcc_s2

    sigma1 = np.cov(part1).diagonal()
    sigma2 = np.cov(part2).diagonal()

    det1 = max(np.prod(np.maximum(sigma1, eps)), realmin)
    det2 = max(np.prod(np.maximum(sigma2, eps)), realmin)

    BIC = 0.5 * (n * np.log(det0) - n1 * np.log(det1) - n2 * np.log(det2)) - 0.5 * (m + 0.5 * m * (m + 1)) * np.log(n)
    return BIC

# Speech segmentation based on BIC
def compute_bic(mfcc_v, delta):
    m, n = mfcc_v.shape

    sigma0 = np.cov(mfcc_v).diagonal()
    eps = np.spacing(1)
    realmin = np.finfo(np.double).tiny
    det0 = max(np.prod(np.maximum(sigma0, eps)), realmin)

    flat_start = 5

    range_loop = range(flat_start, n, delta)
    x = np.zeros(len(range_loop))
    iter = 0
    for index in range_loop:
        part1 = mfcc_v[:, 0:index]
        part2 = mfcc_v[:, index:n]

        sigma1 = np.cov(part1).diagonal()
        sigma2 = np.cov(part2).diagonal()

        det1 = max(np.prod(np.maximum(sigma1, eps)), realmin)
        det2 = max(np.prod(np.maximum(sigma2, eps)), realmin)

        BIC = 0.5*(n*np.log(det0)-index*np.log(det1)-(n-index)*np.log(det2))-0.5*(m+0.5*m*(m+1))*np.log(n)
        x[iter] = BIC
        iter = iter + 1

    maxBIC = x.max()
    maxIndex = x.argmax()
    if maxBIC > 0:
        return range_loop[maxIndex]-1
    else:
        return -1


def speech_segmentation(mfccs):
    wStart = 0
    wEnd = 200
    wGrow = 200
    delta = 25

    m, n = mfccs.shape

    store_cp = []
    index = 0
    while wEnd < n:
        featureSeg = mfccs[:, wStart:wEnd]
        detBIC = compute_bic(featureSeg, delta)
        index = index + 1
        if detBIC > 0:
            temp = wStart + detBIC
            store_cp.append(temp)
            wStart = wStart + detBIC + 200
            wEnd = wStart + wGrow
        else:
            wEnd = wEnd + wGrow

    return np.array(store_cp)



def multi_segmentation(file, sr, frame_size, frame_shift,save_seg=False, cluster_method=None):
    y, sr = librosa.load(file, sr=sr)

    mfccs = librosa.feature.mfcc(y, sr, n_mfcc=12, hop_length=frame_shift, n_fft=frame_size)
    seg_point = speech_segmentation(mfccs / mfccs.max())

    seg_point = seg_point * frame_shift
    seg_point = np.insert(seg_point, 0, 0)
    seg_point = np.append(seg_point, len(y))
    rangeLoop = range(len(seg_point) - 1)

    output_segpoint = []
    for i in rangeLoop:
        temp = y[seg_point[i]:seg_point[i + 1]]
        # add a detection of silence before vad
        max_mean = np.mean(temp[temp.argsort()[-800:]])
        if max_mean < 0.005:
            continue
        # vad detect
        x1, x2 = vad(temp, sr=sr, framelen=frame_size, frameshift=frame_shift)
        if len(x1) == 0 or len(x2) == 0:
            continue
        elif seg_point[i + 1] == len(y):
            continue
        else:
            output_segpoint.append(seg_point[i + 1])
    #
    # if save_seg:
    #     if not os.path.exists("save_audio"):
    #         os.makedirs("save_audio")
    #     else:
    #         shutil.rmtree("save_audio")
    #         os.makedirs("save_audio")
    #     save_segpoint = output_segpoint.copy()
    #     # Add the start and the end of the audio file
    #     save_segpoint.insert(0, 0)
    #     save_segpoint.append(len(y))
    #     for i in range(len(save_segpoint)-1):
    #         tempAudio = y[save_segpoint[i]:save_segpoint[i+1]]
    #         librosa.output.write_wav("save_audio/%s.wav" % i, tempAudio, sr)

    # If the chosen cluster method is kmeans, use kmeans to perform clustering
    if cluster_method == 'kmeans':
        classify_segpoint = output_segpoint.copy()
        # Add the start and the end of the audio file
        classify_segpoint.insert(0, 0)
        classify_segpoint.append(len(y))

        # Length of codebook
        # k = 16
        # vq_features = np.zeros((len(classify_segpoint) - 1,k*12),dtype=np.float32)
        vq_features = np.zeros((len(classify_segpoint) - 1, 12), dtype=np.float32)
        for i in range(len(classify_segpoint) - 1):
            tempAudio = y[classify_segpoint[i]:classify_segpoint[i + 1]]
            mfccs = librosa.feature.mfcc(tempAudio, sr, n_mfcc=12, hop_length=frame_shift, n_fft=frame_size)
            mfccs = mfccs / mfccs.max()
            vq_code = np.mean(mfccs, axis=1)
            vq_features[i, :] = vq_code.reshape(1, vq_code.shape[0])
            # vq_code = vqlbg.vqlbg(mfccs,k)
            # vq_features[i,:] = vq_code.reshape(1,vq_code.shape[0]*vq_code.shape[1])

        K = range(1, len(classify_segpoint))
        square_error = []
        for k in K:
            kmeans = KMeans(n_clusters=k, random_state=0).fit(vq_features)
            square_error.append(kmeans.inertia_)
        #
        # plt.figure('Kmeans Number of clusters evaluate')
        # plt.plot(K, square_error, "bo-")
        # plt.title('Please choose the best number of clusters under Elbow Criterion')
        # plt.xlabel("Number of clusters")
        # plt.ylabel("SSE For each step")
        # plt.ylim(0, square_error[0]*1.5)
        # plt.grid(True)
        # plt.show()

        k_n = input("Please input the best K value: ")
        kmeans = KMeans(int(k_n), random_state=0).fit(vq_features)
        print("The lables for", len(kmeans.labels_), "speech segmentation belongs to the clusters below:")
        for i in range(len(kmeans.labels_)):
            print(kmeans.labels_[i], "")

    # If the chosen cluster method is bic, use bic distance to perform clustering
    if cluster_method == "bic":
        classify_segpoint = output_segpoint.copy()
        # Add the start and the end of the audio file
        classify_segpoint.insert(0, 0)
        classify_segpoint.append(len(y))
        feature_vectors = OrderedDict()
        for i in range(len(classify_segpoint) - 1):
            tempAudio = y[classify_segpoint[i]:classify_segpoint[i + 1]]
            mfccs = librosa.feature.mfcc(tempAudio, sr, n_mfcc=12, hop_length=frame_shift, n_fft=frame_size)
            mfccs = mfccs / mfccs.max()
            feature_vectors[str(i)] = mfccs

        # # Define a empty cluster before perform clustering
        # cluster_list = {}
        # # Call the function cluster_greedy recursively
        # while len(feature_vectors.keys()) > 0:
        #     cluster_greedy(feature_vectors, cluster_list)
        #
        # print('There are total %d clusters' % (len(cluster_list)), 'and they are listed below: ')
        # for index, key in enumerate(cluster_list.keys()):
        #     print('cluster %d' % index, ": ", cluster_list[key])

    return np.asarray(output_segpoint) / float(sr)

def run_seg(filename):
    frame_size = 256
    frame_shift = 128
    sr = 16000

    ##segment the whole wav and get segment point
    seg_point = multi_segmentation(filename, sr, frame_size, frame_shift,cluster_method='bic')
    # print('The segmentation point for this audio file is listed (Unit: /s)', seg_point)
    return seg_point

