#/usr/bin/python3

import setproctitle
import subprocess as sp
import psutil
import sys,signal
from select import select
from termcolor import *
from multiprocessing import *

sys.path.append('./include')
from define import *

class ControlGate(Process):
    def __init__(self):
        Process.__init__(self)
        setproctitle.setproctitle('oloraGT')
        self.readList = []
        self.ntIn  = ObjectPipe(PIPE_LIST.NT_IN)
        self.ntOut = ObjectPipe(PIPE_LIST.NT_OUT)
        self.xbIn  = ObjectPipe(PIPE_LIST.XB_IN)
        self.xbOut = ObjectPipe(PIPE_LIST.XB_OUT)
        self.__pipeOpen()
        
    def __pipeOpen(self):
        self.ntIn.mkfifo()
        self.ntOut.mkfifo()
        self.xbIn.mkfifo()
        self.xbOut.mkfifo()
        print(colored('[+] [GATE] Waiting for gates open.','yellow',attrs=['bold']))
        self.ntOut.open(os.O_WRONLY)
        self.ntIn.open(os.O_RDONLY)
        print(colored('[+] [GATE] OloraNT is ready.','blue',attrs=['bold']))
        #self.xbOut.open(os.O_WRONLY)
        #self.xbIn.open(os.O_RDONLY)
        print(colored('[+] [GATE] OloraXB is ready.','blue',attrs=['bold']))
        #self.readList.append(self.ntIn)
        #self.readList.append(self.xbIn)
        
    # Debugging Function
    def __echoNT(self):
        while(True):          
            packet = self.ntIn.recv()
            if(packet!=0 and packet!=b''):
                print(colored('[+] [GATE] BLE --> BLE.','grey',attrs=['bold']))
                self.ntOut.write(packet)
            else:
                print(colored('[+] [GATE] Connection Lost','red',attrs=['bold']))
                break
              
    def __selector(self,timeout=5):
        flags = False
        desc  = 0
        rd,wr,er = select(self.readList,[],[],timeout)
        opt = 0
        for sock in rd:
            if( sock == self.ntIn.pipe):
                packet = self.ntIn.recv()
                if(packet!=0 and packet!=b''):
                    print(colored('[+] [GATE] BLE --> XBEE.','grey',attrs=['bold']))
                    self.xbOut.write(packet)
                else:
                    self.ntIn.close()
                    flags = True
                    desc = PRSS_LIST.OLORANT          
            elif( sock == self.xbIn.pipe):
                packet = self.xbIn.recv()
                if(packet!=0 and packet!=b''):
                    print(colored('[+] [GATE] XBEE --> BLE.','grey',attrs=['bold']))
                    self.ntOut.write(packet)                 
                else:
                    self.xbIn.close()
                    flags = True
                    desc = PRSS_LIST.OLORAXB
            if(flags==True):
                print(colored('[!] [GATE] {0} Pipe descriptor is now removed in watch_list.'.format(sock),'red',attrs=['bold']))
                self.readList.remove(sock)
                self.rebuildHandler(desc)
                flags = False
                desc  = PRSS_LIST.UNSELECTED
                
    def rebuildHandler(self,desc):
        if(desc==PRSS_LIST.OLORANT):
            os.system("sudo service olorant restart")
            self.ntOut.open(os.O_WRONLY)
            self.ntIn.open(os.O_RDONLY)
            self.readList.append(self.ntIn)
            print(colored('[!] [GATE] {REBUILDED}.'.format(sock),'yellow',attrs=['bold']))
        elif(desc==PRSS_LIST.OLORANT):
            os.system("python3 ./src/oloraXB.py & ")
            self.xbOut.open(os.O_WRONLY)
            self.xbIn.open(os.O_RDONLY)
            self.readList.append(self.xbIn)
            print(colored('[!] [GATE] {REBUILDED}.'.format(sock),'yellow',attrs=['bold']))
                
    def run(self):
        self.__echoNT()
        #while(True):
        #    self.__selector()


if __name__ == '__main__':
    def signal_handler(signal,frame):
        sys.exit(0)
    signal.signal(signal.SIGPIPE,signal.SIG_DFL)
    signal.signal(signal.SIGINT, signal_handler)
    
    cg = ControlGate()
    cg.start()
    sys.exit(0)
