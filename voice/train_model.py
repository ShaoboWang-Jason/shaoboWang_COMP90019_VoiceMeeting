#!/usr/bin/env python
# -*- coding:utf-8 -*-
# author: weiwei xie

import os
import numpy as np
from scipy.io.wavfile import read
from sklearn.mixture import GaussianMixture
from extractFeature import extract_feat
import warnings
import pickle as p
from sklearn import preprocessing
warnings.filterwarnings('ignore')
from map_adaptation import map_adaptation

#read one speaker's all wav and extract features
def read_one_wav(id):
    features = np.asarray(())
    source = '/Users/pkvi/Desktop/project/wav'
    filepath = source + '/id' + str(id)
    files = os.listdir(filepath)
    for file in files:
        if not file.startswith('.'):
            temp_path = os.path.join(filepath,file)
            if os.path.isdir(temp_path):
                f2 = os.listdir(temp_path)
                for wav in f2:
                    if not wav.startswith('.'):
                        p = os.path.join(temp_path,wav)
                        if not os.path.isdir(p):
                            sr, audio = read(p)
                            vector = extract_feat(audio,sr)

                            if features.size == 0:
                                features = vector
                            else:
                                features = np.vstack((features, vector))
    return features


# ###train ubm model
# id = 10270
# i = 0
# f = np.asarray(())
# while id < 10310:
#     features = read_one_wav(id)
#     if i == 0:
#         f = features
#         id +=1
#         print('features',i,'already done')
#         i += 1
#     else:
#         f = np.vstack((f, features))
#         id +=1
#         print('features', i, 'already done')
#         i += 1
#
# ubm_features = preprocessing.scale(f)
# ubm = GaussianMixture(covariance_type='diag')
# ubm.fit(ubm_features)
# ##dumping the trained ubm model
# picklefile = '/Users/pkvi/Desktop/project/trained_model/ubm'
# p.dump(ubm,open(picklefile,'wb'))
# print('ubm already done')

###train gmm model for each enroll speaker
#load ubm model
modelpath = '/Users/pkvi/Desktop/project/trained_model'
ubm_path = [os.path.join(modelpath,fname) for fname in
              os.listdir(modelpath) if fname.endswith('.ubm')]
ubm_model = p.load(open(ubm_path[0],'rb'))

id = 10270
a = 1
while id <10310:
    features = read_one_wav(id)
    #create gmm and ubm
    gmm = map_adaptation(ubm_model, features, max_iterations=1, relevance_factor=16)
    # gmm = GaussianMixture(covariance_type='diag')
    # gmm.fit(features)
    #dumping the trained gaussian model
    picklefile = '/Users/pkvi/Desktop/project/model/' + str(a) + '.gmm'
    p.dump(gmm, open(picklefile, 'wb'))
    print('speaker', a,'already done')
    a += 1
    id +=1

