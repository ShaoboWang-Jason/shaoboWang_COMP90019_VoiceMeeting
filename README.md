# Read Me
This is the project for Voice Meeting Recognition. Our project goal is to recognize and distinguish people who are talking during the meeting time. It consists of Android and web applications, server and back-end Python algorithm files. Server and tornado_web file are set on Google Cloud and the IP used in file is 35.289.40.211 which is the external IP for Google Cloud Virtual Machine. 

Requirment:
1. Back-End Python package: scipy, numpy, pickle, extractFeature, map_adaptation, segmentation, pydub, librosa, json
2. Server Python package: tornado, socket, threading
3. Android Application package: Firebase, AudioRecorder, MediaPlayer
4. HTML: bootstrap.min.css, Chart.bundle.min.js, Chart.min.js, utils.js, echarts.min.js, jquery.min.js


Running:
Need to first run server and tornado_web python files for receiving data from Android application. "Upload" button should be happened before "data" button since the voice should be transferred to server and get json data back, and then transfer json data to webserver. The webserver is 35.289.40.211:8889.


Limitation:
Our project does not support parallel. For "History" button, Data transfer takes time, so wait a few seconds after login before click button.
