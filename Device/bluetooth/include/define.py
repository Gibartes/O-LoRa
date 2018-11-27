from packet import *
import os,re,sys,time

class PIPE_LIST(object):
    NT_IN      = "/tmp/.olora.out"
    NT_OUT     = "/tmp/.olora.in"
    XB_IN      = "/tmp/.xbolora.out"
    XB_OUT     = "/tmp/.xbolora.in"

class PRSS_LIST(object):
    UNSELECTED = 0
    OLORANT    = 1
    OLORAXB    = 3
    
class ObjectPipe:
    def __init__(self,channel):
        self.path = channel
        self.pipe = 0
    def mkfifo(self):
        try:
            os.mkfifo(self.path)
            return 0
        except OSError as e:return -1
    def open(self,flag):
        self.pipe = os.open(self.path,flag)
        return self.pipe
    def write(self,rcv,close=False):
        res = os.write(self.pipe,rcv)
        if close:self.pipe.close()
        return res
    def recv(self,size=1008,close=False):
        res	 = os.read(self.pipe,size)
        if close:self.pipe.close()
        return res
    def close(self):
        try:self.pipe.close()
        except:pass
