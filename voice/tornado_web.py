# -*- coding:utf-8 -*-
# author: Shaobo Wang
# shaobow@student.unimelb.edu.au

import tornado
from tornado import web, ioloop, httpserver
from multiprocessing import Queue
from tornado.concurrent import run_on_executor
from concurrent.futures import ThreadPoolExecutor
import time
import json

global q,username,password,webname,webpassword
webname = ''
webpassword = ''

q = Queue(100)
global r
r = ''
global executor
executor = ThreadPoolExecutor(20)


class MainHandler(tornado.web.RequestHandler):

    global executor
    def get(self):
        global r,webname,webpassword,username,password
        # Only all the data from web is equal to the data from application. Then it will transfer
        if not q.empty():
            if (username == webname and password == webpassword):
                r = q.get()
                self.write(r)
        else:
            if (username == webname and password == webpassword):
                self.write(r)

    def post(self):
        global webname,webpassword
        data = self.request.body
        print(data)
        jsonstr = data.decode('utf8')  # Decode into string
        # @ cannot be read by post method. This will be replaced by %40
        jsonstr1 = jsonstr.replace('%40','@').replace('&', ' ').replace('username=','').replace('pwd=','')
        a = jsonstr1.split(' ')
        # get username and password from web
        webname = a[0]
        webpassword = a[1]
        print(webname)
        print(webpassword)
        self.render('result.html')

class logout(tornado.web.RequestHandler):

    def post(self):
        global webname,webpassword
        result = self.request.body
        webname = ''
        webpassword = ''
        self.render('test.html')

class getUser(tornado.web.RequestHandler):

    def post(self):
        global username,password
        result = self.request.body
        resultJson = result.decode('utf8')
        Arr = json.loads(resultJson)
        # get username and password from android application
        username = Arr[0]
        password = Arr[1]
        print(username)
        print(password)


class IndexHandler(tornado.web.RequestHandler):
    global executor
    def get(self):
        self.render('login.html')

    def post(self):
        global q
        jsonbyte = self.request.body
        jsonstr = jsonbyte.decode('utf8')
        print(jsonstr)
        # get json from android application and put into queue
        q.put(jsonstr)

    def block_task(self, strTime):
        "in block_task %s" % strTime
        for i in range(1, 16):
            time.sleep(1)
            "step %d : %s" % (i, strTime)
        return "Finish %s" % strTime

settings = {
    'static_path': 'static',
    'static_url_prefix': '/static/',

}

application = tornado.web.Application(handlers=
    [
    (r"/", IndexHandler),
    (r"/IndexHandler", IndexHandler),
    (r"/getUser", getUser),
    (r"/logout", logout),
    (r"/MainHandler", MainHandler),

], autoreload=False, debug=False, **settings)


if __name__ == '__main__':
     http_server = tornado.httpserver.HTTPServer(application)
     http_server.listen(8889)
     tornado.ioloop.IOLoop.current().start()