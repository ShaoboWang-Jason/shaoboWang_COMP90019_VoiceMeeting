#!/usr/bin/env python
# -*- coding:utf-8 -*-
# author: weiwei xie

import numpy as np
import os
from scipy.io.wavfile import read
from extractFeature import extract_feat
import pickle as p

#source = '/Users/pkvi/Desktop/project/test_wav'
# modelpath = '/Users/pkvi/Desktop/project/model'

#use gmm model
modelpath = '/Users/pkvi/Desktop/project/trained_model'
gmm_files = [os.path.join(modelpath,fname) for fname in
              os.listdir(modelpath) if fname.endswith('.gmm')]


#Load the Gaussian Models and model name
#load ubm model
models = [p.load(open(fname,'rb')) for fname in gmm_files]
speakers = [fname.split('/')[-1].split('.gmm')[0] for fname in gmm_files]

##normalize gmm score into [0,1]
def maxminnorm(array):
    re = []
    for i in array:
        a = (i-min(array))/(max(array) - min(array))
        re.append(a)
    return re


def predict(sr,audio):
    vector = extract_feat(audio, sr)

    log_likelihood = np.zeros(len(models))

    for i in range(len(models)):
        gmm = models[i]
        scores = np.array(gmm.score(vector))
        log_likelihood[i] = scores.sum()

    #find the most match speaker id
    speaker_id = np.argmax(log_likelihood)

    # calculate variance
    temp = maxminnorm(log_likelihood)
    confidence = np.var(temp)

    #print('speaker', file, 'is', speakers[speaker_id])
    return speakers[speaker_id], confidence


#测试accuracy

source = '/Users/pkvi/Desktop/project/test_wav/yimeng'
files = os.listdir(source)
for file in files:
    if not file.startswith('.'):
        path = os.path.join(source, file)
        # sr, audio = read(path)
        # speaker_id, confidence = predict(sr,audio)
        # c = '%.2f%%' % (confidence * 100)
        # print(file,speaker_id,c)

a = 'shaobo1.wav'
sr,audio = read(a)
speaker_id, confidence = predict(sr,audio)
c = '%.2f%%' % (confidence * 100)
print(file,speaker_id,c)