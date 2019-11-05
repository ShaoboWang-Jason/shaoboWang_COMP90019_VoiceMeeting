#!/usr/bin/env python
# -*- coding:utf-8 -*-
# author: weiwei xie

import os
import pickle as p
import numpy as np
from extractFeature import extract_feat
import warnings
warnings.filterwarnings("ignore")
from segmentation import run_seg
from pydub import AudioSegment
import librosa
import json
from scipy.io.wavfile import read
#model ---ubm
#trained model ---gmm

modelpath = '/home/wangshaobo/comp90025/UI/it5'

gmm_files = [os.path.join(modelpath,fname) for fname in
              os.listdir(modelpath) if fname.endswith('.gmm')]


#Load the Gaussian Models and model name
#load ubm model
models = [p.load(open(fname,'rb')) for fname in gmm_files]
speakers = [fname.split('/')[-1].split('.gmm')[0] for fname in gmm_files]

genderDic = {'female':('1','11','12','13','16','17','18','19','42',
                       '20','22','30','34','37','38','39','43'),
             'male':('2','3','4','5','6','7','8','9','10','14','15',
                     '21','23','24','25','26','27','28','29','31',
                     '32','33','35','36','41','40')}

def cal_confidence(array):
    al = array.tolist()
    al.sort(reverse = True)
    max_score = al[0]
    sec_score = al[1]
    confidence = sec_score/(sec_score+max_score)
    return confidence

def predict(sr,audio):
    vector = extract_feat(audio, sr)

    log_likelihood = np.zeros(len(models))

    for i in range(len(models)):
        gmm = models[i]
        #gmm score
        scores = np.array(gmm.score(vector))
        #print('scores',scores)
        log_likelihood[i] = scores.sum()

    #find the most match speaker id
    speaker_id = np.argmax(log_likelihood)

    # calculate confidence
    confidence = cal_confidence(log_likelihood)

    #print('speaker', file, 'is', speakers[speaker_id])
    return speakers[speaker_id], confidence


##segment wav and predict each
def seg_each_wav(filename):
    seg_point = run_seg(filename)
    return seg_point



#wav time converted to ms
#use dic to store each segment and speaker tag
def total_result(data):
    seg_point = list(seg_each_wav(data))
    dic = {}
    total_time = librosa.get_duration(filename=data)
    seg_point.append(0)
    seg_point.append(total_time)
    seg_point.sort()
    #print(seg_point)
    sound = AudioSegment.from_file(data)
    #each segmentate wav predict
    id = []
    s_time = []
    e_time = []
    gender = []
    con = []
    t = [1]*5
    for i in range(len(seg_point)-1):

        start_time = seg_point[i]*1000
        end_time = seg_point[i+1]*1000

        if end_time - start_time >=2000:
            s_wav = sound[start_time:end_time]
            #print((start_time,end_time))
            sr = s_wav.frame_rate
            audio = np.frombuffer(s_wav.get_array_of_samples(), dtype=np.int16)
            result, confidence = predict(sr, audio)
        else:
            result, confidence = 'not sure', 'none'
        id.append(result)
        if result in genderDic['male']:
            gender.append("male")
        else:
            gender.append("female")
        s_time.append(round(start_time/1000,2))
        e_time.append(round(end_time/1000,2))
        if result in dic.keys():
            temp = dic[result]
            if confidence != 'none':
                c = "%.2f" % (confidence *150)
            else:
                c = "none"
            temp.append((start_time/1000, end_time/1000))
        else:
            if confidence != 'none':
                c = "%.2f" % (confidence * 150)
            else:
                c = "none"
            dic[result] = [(start_time/1000, end_time/1000)]
        con.append(c)
    t[0], t[1], t[2], t[3],t[4] = id, s_time, e_time, con, gender
    return (str(t))

