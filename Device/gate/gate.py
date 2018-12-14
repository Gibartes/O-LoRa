#/usr/bin/python3

import setproctitle
import subprocess as sp
import psutil
import sys,signal
import argparse

from select import select
from termcolor import *
from multiprocessing import *

from olora.packet import *
from olora.define import *

class ControlGate(Process):
    def __init__(self,debug=False,echo=False,collect=False):
        Process.__init__(self)
        setproctitle.setproctitle('oloraGT')
        self.debug      = debug
        self.echo       = echo
        self.readList   = []
        self.ntIn       = ObjectPipe(PIPE_LIST.NT_IN)
        self.ntOut      = ObjectPipe(PIPE_LIST.NT_OUT)
        self.xbIn       = ObjectPipe(PIPE_LIST.XB_IN)
        self.xbOut      = ObjectPipe(PIPE_LIST.XB_OUT)
        self.__pipeOpen()
        
    def __pipeOpen(self):
        self.ntIn.mkfifo()
        self.ntOut.mkfifo()
        self.xbIn.mkfifo()
        self.xbOut.mkfifo()
        print(colored('[+] [GATE] Waiting for gates open.','yellow',attrs=['bold']))
        self.ntOut.open(os.O_WRONLY)
        self.ntIn.open(os.O_RDONLY)
        self.readList.append(self.ntIn.pipe)        
        print(colored('[+] [GATE] OloraNT is ready.','blue',attrs=['bold']))
        if(self.echo==False):
            self.xbOut.open(os.O_WRONLY)
            self.xbIn.open(os.O_RDONLY)
            self.readList.append(self.xbIn.pipe)
            print(colored('[+] [GATE] OloraXB is ready.','blue',attrs=['bold']))
        
    # Debugging Function
    def __echoNT(self):
        pkt      = PACKET(PACKET_HEADER_CONFIG.PACKET_FULL,PACKET_HEADER_CONFIG.DATA_LENGTH)
        while(True):          
            packet = self.ntIn.recv()
            if(packet!=0 and packet!=b''):
                pkt.packet = packet
                pkt.split()
                pkt.parse()
                if(self.debug):
                    print(pkt.parseinfo)
                    pkt.print_payload(256)
                if(pkt.parseinfo['SRC']==pkt.parseinfo['DST']):
                    print(colored('[+] [GATE] Client Broken.','green',attrs=['bold']))
                else:                    
                    print(colored('[+] [GATE] BLE --> BLE.','grey',attrs=['bold']))
                    self.ntOut.write(packet)
            else:
                print(colored('[+] [GATE] Connection Lost','red',attrs=['bold']))
                break
              
    def __selector(self,timeout=5):
        flags    = False
        desc     = 0
        rd,wr,er = select(self.readList,[],[],timeout)
        opt      = 0
        pkt      = PACKET(PACKET_HEADER_CONFIG.PACKET_FULL,PACKET_HEADER_CONFIG.DATA_LENGTH)
        for sock in rd:
            pkt.reset()
            if( sock  == self.ntIn.pipe):
                packet = self.ntIn.recv()
                if(packet!=0 and packet!=b''):
                    pkt.packet = packet
                    pkt.split()
                    pkt.parse()
                    if(pkt.parseinfo['SRC']==0 or pkt.parseinfo['DST']==0):
                        print(colored('[+] [GATE] Dropped. This is an internal packet.','yellow',attrs=['bold']))
                        continue
                    if(self.debug):
                        print("From BLE to XBEE")
                        print(pkt.parseinfo)
                        pkt.print_payload(256)
                    print(colored('[+] [GATE] BLE --> XBEE.','cyan',attrs=['bold']))
                    self.xbOut.write(packet)
                else:
                    self.ntIn.close()
                    flags = True
                    desc = PRSS_LIST.OLORANT
            elif( sock == self.xbIn.pipe):
                packet = self.xbIn.recv()
                if(packet!=0 and packet!=b''):
                    pkt.packet = packet
                    pkt.split()
                    pkt.parse()
                    if(pkt.parseinfo['SRC']==0 or pkt.parseinfo['DST']==0):
                        print(colored('[+] [GATE] Dropped. This is an internal packet.','yellow',attrs=['bold']))
                        continue
                    if(self.debug):
                        print("From XBEE to BLE")
                        print(pkt.parseinfo)
                        pkt.print_payload(256)
                    print(colored('[+] [GATE] XBEE --> BLE.','cyan',attrs=['bold']))
                    self.ntOut.write(packet)                 
                else:
                    self.xbIn.close()
                    flags = True
                    desc = PRSS_LIST.OLORAXB
            if(flags==True):
                print(colored('[!] [GATE] {0} Pipe descriptor is now removed in watch_list.'.format(sock),'red',attrs=['bold']))
                self.readList.remove(sock)
                self.__rebuildHandler(desc)
                flags = False
                desc  = PRSS_LIST.UNSELECTED
                
    def __rebuildHandler(self,desc):
        if(desc==PRSS_LIST.OLORANT):
            print(colored('[+] [GATE] Waiting for gates open.','yellow',attrs=['bold']))
            #os.system("sudo service olorant restart")
            self.ntOut.open(os.O_WRONLY)
            self.ntIn.open(os.O_RDONLY)
            self.readList.append(self.ntIn.pipe)
            print(colored('[!] [GATE] {REBUILDED}.','green',attrs=['bold']))
        elif(desc==PRSS_LIST.OLORANT):
            print(colored('[+] [GATE] Waiting for gates open.','yellow',attrs=['bold']))
            #os.system("sudo service oloraxb restart")
            self.xbOut.open(os.O_WRONLY)
            self.xbIn.open(os.O_RDONLY)
            self.readList.append(self.xbIn.pipe)
            print(colored('[!] [GATE] {REBUILDED}.','green',attrs=['bold']))

    def run(self):
        if(self.echo):
            self.__echoNT()
            return 0
        while(True):
            self.__selector()
        return 0

if __name__ == '__main__':
    def signal_handler(signal,frame):
        sys.exit(0)
        
    signal.signal(signal.SIGPIPE,signal.SIG_DFL)
    signal.signal(signal.SIGINT, signal_handler)
    
    echoMode  = False
    debugMode = False
    parser = argparse.ArgumentParser(description="select run mode")
    parser.add_argument("-r",action="store",dest="mode",type=str,default='n',required=False)
    args = parser.parse_args()

    mode = args.mode
    if(mode=="help"):
        print("""
\tDescription about -r option
\td : set debug flags   - Print packets which passed by oloraGT
\te : set echo flags    - Test for oloraNT
\tn : set normal flags  - Normal state (default).
        """)
        sys.exit(0)
    if('e' in mode):echoMode  = True
    if('d' in mode):debugMode = True
    if('n' in mode):
        echoMode  = False
        debugMode = False 
    cg = ControlGate(debugMode,echoMode)
    cg.start()
    sys.exit(0)
