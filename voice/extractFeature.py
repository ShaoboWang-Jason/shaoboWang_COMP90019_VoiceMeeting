#!/usr/bin/env python
# -*- coding:utf-8 -*-
# author: weiwei xie

from sklearn import preprocessing
import python_speech_features as mfcc
import numpy as np
import librosa
# from sklearn.mixture import GaussianMixture
from scipy.io.wavfile import read
# import pickle as p




def cal_delta(array):
    rows, col = array.shape
    deltas = np.zeros((rows, 20))
    N = 2
    for i in range(rows):
        index = []
        j = 1
        while j <= N:
            if i-j < 0:
                first = 0
            else:
                first = i - j
            if i + j > rows -1:
                second = rows -1
            else:
                second = i + j
            index.append((second, first))
            j += 1
        deltas[i] = (array[index[0][0]] - array[index[0][1]] + (2 * array[index[1][0]] - array[index[1][1]]))/10
    return deltas

def extract_feat(audio, rate):
    mfcc_feat = mfcc.mfcc(audio, rate, 0.025, 0.01, 20, appendEnergy=True)
    mfcc_feat = preprocessing.scale(mfcc_feat)
    delta = cal_delta(mfcc_feat)
    combined = np.hstack((mfcc_feat, delta))

    return combined


def feature(filename):
    #all wav need to store in utterances_spec
    utterances_spec = []
    # Load  wav files
    y, sr = librosa.load(filename, sr=None)
    # extract mel spectrogram feature
    #mel_basis = librosa.feature.melspectrogram(y,sr, n_fft=1024, hop_length=512, n_mels=128)
    #s = np.log10(mel_basis + 1e-6)
    s = librosa.feature.mfcc(y,sr,n_mfcc=40)


    return  s


#
# path1 = '/Users/pkvi/Desktop/project/wav/id10270/x6uYqmx31kE/00018.wav'
# path3 = '/Users/pkvi/Desktop/project/wav/id10301/rXRbmL7nzIo/00002.wav'
# path2 = '/Users/pkvi/PycharmProjects/voice/venv/00003.wav'
#


# gmm = GaussianMixture().fit(a)
# labels = gmm.predict(a)
# print(labels)

# sr, audio = read(path3)
# print(sr)
# vector = extract_feat(audio, sr)
# features = np.asarray(())
# if features.size == 0:
#     features = vector

# sr2, audio2 = read(path2)
# vector2 = extract_feat(audio2,sr2)
# features2 = np.vstack((features,vector2))
#
# sr3, audio3 = read(path3)
# vector3 = extract_feat(audio3,sr3)
# features3 = np.asarray(())
# features3 = vector3
#
#
# gmm1 = GaussianMixture(covariance_type='diag')
# gmm1.fit(features)
# picklefile = '/Users/pkvi/Desktop/project/fea1.gmm'
# p.dump(gmm1,open(picklefile, 'wb'))
# gmm2 = GaussianMixture(covariance_type='diag')
# gmm2.fit(features3)
#
#
# test_path = '/Users/pkvi/Desktop/project/test_wav/000011.wav'
# test_sr, test_audio = read(test_path)
#
# log_likelihood = np.zeros(2)
#
# test_feature = extract_feat(test_audio, test_sr)
#
# scores = np.array(gmm1.score(test_feature))
# log_likelihood[0] = scores.sum()
# scores2 = np.array(gmm2.score(test_feature))
# log_likelihood[1] = scores2.sum()
#
# winner = np.argmax(log_likelihood)
# print(winner)

