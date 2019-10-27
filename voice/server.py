# -*- coding:utf-8 -*-
# author: Shaobo Wang
# shaobow@student.unimelb.edu.au

from test_speaker import total_result
import socket
from threading import Thread

HOST = ''
PORT = 9999
ADDR = (HOST, PORT)
sock = None
MAX_CONNECT = 3
g_conn_pool = []

def init():
    global sock
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.bind(ADDR)
    sock.listen(MAX_CONNECT)
    print("Server active")

def accept_client():
    global sock
    while True:
        pip, addr = sock.accept()
        print("Receving")
        g_conn_pool.append(pip)
        thread = Thread(target=message_handle, args=(pip,))
        thread.setDaemon(True)
        thread.start()

def message_handle(pip):
    i = 0
    try:
        while True:
            with open("file", "ab") as f:
                if i == 0:
                    filename = pip.recv(1024)
                    name = str(filename).replace("b\'", '')
                    name = name.replace('\'', '')
                    name = name + ".wav"
                    print(name)
                    i = i + 1
                else:
                    with open(name, "ab") as f:
                        data = pip.recv(1024)
                        if not data:
                            break
                        f.write(data)
        result = total_result(name)
        print(result)
        pip.sendall(result.encode())
        print("Receving")
        pip.close()
        g_conn_pool.remove(pip)
    except ValueError as e:
        print(e)
        pip.send('Try again')
if __name__ == '__main__':

    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(('8.8.8.8', 80))
        ip = s.getsockname()[0]
    finally:
        s.close()
    print(ip)

    init()
    thread = Thread(target=accept_client())
    thread.setDaemon(True)
    thread.start()
