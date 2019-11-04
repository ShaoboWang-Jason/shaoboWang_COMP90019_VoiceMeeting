# Read Me
This is the project for Voice Meeting Recognition. The project is COMP90019 but I wrongly memorized into COMP90025. All the applications can run correctly. Our project goal is to recognize and distinguish people who are talking during the meeting time. It consists of Android and web applications, server and back-end Python algorithm files. Server and tornado_web file are set on Google Cloud and the IP used in file is 35.289.40.211 which is the external IP for Google Cloud Virtual Machine. Network may cause some errors, just like big json transfer error. Please check net and try again.

## Main Component:
* VoiceMeeting-Android: The application of voice recording.
* Server.py: The socket server for receiving voice and return json result
* tornado_web.py: The web server for receiving json result and upload to web
* test_speaker.py: The main python file for voice recognition
* train_model.py: Training the model
* Login.html: The main web login page for html
* Result.html: The web page for display json into charts followed by Login.html
* it5: The model for training.

## Requirment:
* Back-End Python package: scipy, numpy, pickle, extractFeature, map_adaptation, segmentation, pydub, librosa, json
* Server Python package: tornado, socket, threading
* Android Application package: Firebase, AudioRecorder, MediaPlayer
* HTML: bootstrap.min.css, Chart.bundle.min.js, Chart.min.js, utils.js, echarts.min.js, jquery.min.js
* Firebase: Need Google play for FirebaseAuth, need Internet

## How to Use
* Need to first run server and tornado_web python files for receiving data from Android application. 
* Firebase is needed. Therefore, the emulator should be able to connect to Internet.
* The ip and port are uniformed, if you wish to change the IP, you need to first set up server.py and tornado_web.py, and change the ip connection on Android Application.
* "Upload" button should be happened before "data" button since the voice should be transferred to server and get json data back, and then transfer json data to webserver. 
* Short time recording may have some errors just like the information header does not recognize. If click "Upload" button and there is no response for a while waiting time. Click the button again! If tried some times but still not work, please record again. The back-end may not recognize this recording. 
* Sometimes backend will not recognize the file name, if cannot find any data back, please retry upload button.
* Do not click data button if there is an empty data back. Although we have handle exception for empty message, still need to be careful of empty message.
* The userid please try a@qq.com with password 111111


## Limitation:
Our project does not support parallel. For "History" button, Data transfer takes time, so wait a few seconds after login before click button.
