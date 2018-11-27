from ctypes import *
import os,sys,struct,platform,hashlib
from collections import OrderedDict


if os.geteuid()==0:
    try:
        LIB_PATH      = "/usr/local/lib/olorapkt.so"
        parser = cdll.LoadLibrary(LIB_PATH)
        parser = None
    except:
        if(platform.machine() == 'x86_64'):
            LIB_PATH = "../src/amd64/olorapkt.so"
        else:
            LIB_PATH = "../src/arm/olorapkt.so" 
else:
    if(platform.machine() == 'x86_64'):
        LIB_PATH = "../src/amd64/olorapkt.so"
    else:
        LIB_PATH = "../src/arm/olorapkt.so"

class PACKET_HEADER_CONFIG(object):
    MASK_SRC        =   0
    MASK_DST        =   8
    MASK_CM         =  16
    MASK_HP         =  24
    MASK_PROTO      =  25
    MASK_ID         =  26
    MASK_FLAGS      =  28
    MASK_FRAG       =  29
    MASK_SEQ        =  30
    MASK_TMS        =  32
    MASK_LEN        =  36
    MASK_TTL        =  38
    MASK_PARAM      =  39
    MASK_DC         =  40
    MASK_DATA       =  56

    FLAG_URGENT     = 128  
    FLAG_ACK        =  64
    FLAG_FIN        =  32
    FLAG_ENCRYPT    =  16
    FLAG_QUERY      =   8
    FLAG_ERROR      =   2
    FLAG_RESP       =   1

    PROT_TCP        =  32
    PROT_UDP        =  16
    PROT_VOICE      =   8
    PROT_RT         =   4
    PROT_GRAPHICS   =   2
    PROT_TEXT       =   1
	    
    PACKET_FULL     = 1008
    PACKET_MINI     =  256
    DATA_LENGTH     = PACKET_FULL - MASK_DATA
    XBEE_DATA_LEN   = PACKET_MINI - MASK_DATA

    BIG_ENDIAN       = 'big'
    LITTLE_ENDIAN    = 'little'    

class PACKET_HEADER_STACK(object):
    HEADER_STACK = [ "SRC","DST","CM","HP","PROTO","ID","FLAGS","FRAG","SEQ","TMS","LEN","TTL","PARAM","DC","PAYLOAD" ]
    BIT_MASK     = [    [PACKET_HEADER_CONFIG.MASK_SRC,8],[PACKET_HEADER_CONFIG.MASK_DST,8],
                        [PACKET_HEADER_CONFIG.MASK_CM,8],[PACKET_HEADER_CONFIG.MASK_HP,1],
                        [PACKET_HEADER_CONFIG.MASK_PROTO,1],[PACKET_HEADER_CONFIG.MASK_ID,2],
                        [PACKET_HEADER_CONFIG.MASK_FLAGS,1],[PACKET_HEADER_CONFIG.MASK_FRAG,1],
                        [PACKET_HEADER_CONFIG.MASK_SEQ,2],[PACKET_HEADER_CONFIG.MASK_TMS,4],
                        [PACKET_HEADER_CONFIG.MASK_LEN,2],[PACKET_HEADER_CONFIG.MASK_TTL,1],
                        [PACKET_HEADER_CONFIG.MASK_PARAM,1],[PACKET_HEADER_CONFIG.MASK_DC,16],
                        [PACKET_HEADER_CONFIG.MASK_DATA,0]]
    HEADER_PAIR = OrderedDict(zip(HEADER_STACK,BIT_MASK))
        
class PACKET_HEADER(Structure):pass

PACKET_HEADER._fields_ = [
    ("SRC"  ,c_uint8*8),  # (8 byte) Bluetooth client address
    ("DST"  ,c_uint8*8),  # (8 byte) Destination address
    ("CM"   ,c_uint8*8),  # (8 byte) Channel Mask
    ("HP"   ,c_uint8*1),  # (1 byte) HP
    ("PROTO",c_uint8*1),  # (1 byte) Protocol    
    ("ID"   ,c_uint8*2),  # (2 byte) Network ID
    ("FLAGS",c_uint8*1),  # (1 byte) MESSAGE Flag
    ("FRAG" ,c_uint8*1),  # (1 byte) Fragmented Packet number    
    ("SEQ"  ,c_uint8*2),  # (2 byte) packet sequence number 
    ("TMS"  ,c_uint8*4),  # (4 byte) timestamp
    ("LEN"  ,c_uint8*2),  # (4 byte) packet data length
    ("TTL"  ,c_uint8*1),  # (1 byte) Time To Alive
    ("PARAM",c_uint8*1),  # (1 byte) Device Parameter
    ("DC"   ,c_uint8*16), # (16 byte) MD5 Hash            
]

pkt_parser                              = cdll.LoadLibrary(LIB_PATH)
pkt_parser.initPacketHeader.argtypes    = [POINTER(PACKET_HEADER),]
pkt_parser.setHeaderOffset.argtypes     = [POINTER(PACKET_HEADER),c_uint32,c_uint32,c_uint64,c_uint8]
pkt_parser.getHeaderOffset.argtypes     = [POINTER(PACKET_HEADER),c_uint32,c_uint32,POINTER(c_uint64),c_uint8]
pkt_parser.setHeaderOffset16.argtypes   = [POINTER(PACKET_HEADER),c_uint32,c_void_p,c_uint8]
pkt_parser.getHeaderOffset16.argtypes   = [POINTER(PACKET_HEADER),c_uint32,c_void_p,c_uint8]
pkt_parser.hexPrint.argtypes            = [c_void_p,c_uint32]
pkt_parser.str2pkth.argtypes            = [POINTER(PACKET_HEADER),c_void_p]
pkt_parser.str2pktb.argtypes            = [c_void_p,c_void_p,c_uint32]
pkt_parser.pkth2str.argtypes            = [POINTER(PACKET_HEADER),c_void_p]
pkt_parser.pktCombine.argtypes          = [c_void_p,c_void_p,c_void_p,c_uint32]
pkt_parser.checkDataBlank.argtypes      = [c_void_p,c_uint32]
pkt_parser.checkDataBlank.restypes      = [c_int32]
pkt_parser.initArea.argtypes            = [c_void_p,c_uint32]
pkt_parser.hash_md5.argtypes            = [c_void_p,c_void_p,c_uint32]

def pkt_split_packet_header_and_body(packet,size,opt):
    PAYLOAD    = c_uint8*size
    PAYLOAD = PAYLOAD(0,)
    HEADER     = PACKET_HEADER()
    pkt_parser.initPacketHeader(byref(HEADER))
    pkt_parser.initArea(PAYLOAD,c_uint32(size))
    pkt_parser.str2pkth(byref(HEADER),packet)
    pkt_parser.str2pktb(packet,PAYLOAD,c_uint32(0))
    return (HEADER,PAYLOAD)
    
def pkt_get_header_from_packet(packet):
    HEADER     = PACKET_HEADER()
    pkt_parser.initPacketHeader(byref(HEADER))
    pkt_parser.str2pkth(byref(HEADER),packet)
    return HEADER

def pkt_set_header_data(head,mask,data,size):
    pkt_parser.setHeaderOffset(byref(head),mask,0,data,size)

def pkt_get_header_data(head,mask,data,size,end=BIG_ENDIAN):
    data     = c_uint64(data)
    pkt_parser.getHeaderOffset(byref(head),mask,0,byref(data),size)
    return data.value.to_bytes(size,byteorder=end)

def pkt_get_header_data_raw(head,mask,data,size):
    data     = c_uint64(data)
    pkt_parser.getHeaderOffset(byref(head),mask,0,byref(data),size)
    return data

def pkt_combine_header_payloads(head,payload,opt,size):
    packet    = c_uint8*size
    packet    = packet(0,)
    pkt_parser.initArea(packet,c_uint32(size))
    pkt_parser.pktCombine(packet,byref(head),byref(payload),c_uint32(opt))
    return packet

def initialize_section(data,size):
    pkt_parser.initArea(byref(data),c_uint32(size))

def hex_print(arr,size):
    pkt_parser.hexPrint(byref(arr),size)

def pkt_check_data_is_blank(payload,size):
    size = c_uint32(size)
    res     = pkt_parser.checkDataBlank(payload,size)
    return res

def check_system_endian():
    return 0 if sys.byteorder == LITTLE_ENDIAN else 1

 

class PACKET:
    HEADSIZE = PACKET_HEADER_CONFIG.MASK_DATA
    def __init__(self,type=PACKET_HEADER_CONFIG.PACKET_MINI,size=PACKET_HEADER_CONFIG.XBEE_DATA_LEN):
        self.type       = type
        self.size       = size
        self.wcursor    = 0
        self.rcursor    = 0
        self.packet     = c_uint8*self.type
        self.packet     = self.packet(0,)
        self.header     = 0
        self.payload    = 0
        self.hash       = c_uint8*16
        self.hash       = self.hash(0,)
        self.temp       = None
        self.opt        = 0
        self.overflow   = 0
        self.accessed   = False
        self.parseinfo  = dict()
        if(self.type==PACKET_HEADER_CONFIG.PACKET_MINI):
            self.opt=0
        else:self.opt=1
        self.init_header()
        self.init_payload()

    # 해더 초기화
    def init_header(self):
        HEADER     = PACKET_HEADER()
        pkt_parser.initPacketHeader(byref(HEADER))
        self.header = HEADER

    # 데이터 영역 초기화
    def init_payload(self):
        self.payload     = c_uint8*self.size
        self.payload     = self.payload(0,)    
        pkt_parser.initArea(self.payload,c_uint32(self.size))

    # 데이터 해쉬값 얻기
    def cal_hash(self,length):
        pkt_parser.hash_md5(self.hash,self.payload,length)

    # 데이터 해쉬값 얻기
    def set_hash(self):
        pkt_parser.setHeaderOffset16(byref(self.header),PACKET_HEADER_CONFIG.MASK_DC,self.hash,16)

    # 데이터 해쉬값 얻기
    def get_hash(self):
        temp       = c_uint8*16
        temp       = temp(0,)
        pkt_parser.getHeaderOffset16(byref(self.header),PACKET_HEADER_CONFIG.MASK_DC,temp,16)
        return temp
        
    # 현재 패킷 내용 얻기
    def get_packet(self):
        return self.packet

    # 패킷 변경
    def set_packet(self,packet):
        self.temp = packet
        self.update()

    # 연속적으로 패킷 데이터 받음
    def put_seq_data(self,data):
        if(self.wcursor>=self.type):
            self.overflow     = 1
            return
        self.packet[self.wcursor] = c_uint8(data)
        self.wcursor+=1
        self.accessed     = True

    # 패킷에 한 바이트의 데이터를 입력
    def put_data(self,index,data):
        self.packet[index] = c_uint8(data)
        self.accessed      = True

    # 연속적으로 패킷 데이터를 출력
    def get_seq_data(self):
        if(self.rcursor>=self.type):
            self.overflow    = 1
            return 0
        if(self.overflow==0):
            data = self.packet[self.rcursor]
            self.rcursor+=1
            return data
        return None

    # 한 바이트의 패킷 데이터를 출력
    def get_data(self,index):
        return self.packet[index]

  # Write Cursor 이동
    def move_wcursor(self,pos):
        self.wcursor = pos

  # Move Cursor 이동
    def move_rcursor(self,pos):
        self.rcursor = pos

    # 현재 커서 위치 반환
    def current(self):
        return (self.wcursor,self.rcursor)

    # 패킷을 해더, 페이로드로 분리
    def split(self):
        (self.header,self.payload) = pkt_split_packet_header_and_body(self.packet,self.size,self.opt)

    # 해더, 페이로드를 합쳐 하나의 패킷으로 설정, 임시 데이터에 저장함
    def combine(self):
        self.temp = pkt_combine_header_payloads(self.header,self.payload,self.opt,self.type)
        self.accessed     = True

    # 임시 데이터에 저장된 패킷으로 내용 업데이트 ( combine -> update )
    def update(self):
        self.packet       = self.temp
        self.split()
        self.temp         = None
        self.accessed     = False

    # 임시 데이터가 존재하는가? (combine을 실행하고 update 진행했는가?)
    def isRevised(self):
        return self.accessed

    # 해더 교환
    def header_xchg(self,header):
        self.header  = header
        self.accessed     = True

    # 해더 정보 출력
    def get_header(self,mask,data,size,end=BIG_ENDIAN):
        return pkt_get_header_data(self.header,mask,data,size,end)

    # 가공되지 않은 해더 정보 출력 ( if you want to convert byte string to int, call unpack() method )
    def get_header_raw(self,mask,data,size):
        return pkt_get_header_data_raw(self.header,mask,data,size)

    # 해더 설정 변경
    def set_header(self,mask,data,size):
        pkt_set_header_data(self.header,mask,data,size)
        self.accessed = 1

    # 16진수로 해더 내용 출력
    def print_header(self):
        hex_print(self.header,PACKET.HEADSIZE)

    # 16진수로 Payload 내용 출력     (시스템 엔디안 반영)
    def print_payload(self,size=PACKET_HEADER_CONFIG.XBEE_DATA_LEN):
        hex_print(self.payload,size)

    # 16진수로 Packet 출력           (시스템 엔디안 반영)
    def print(self,size):
        hex_print(self.packet,size)

  # Payload가 비어있는지 확인
    def isEmpty(self):
        return pkt_check_data_is_blank(self.payload,self.size)

  # Payload가 마지막 부분인지 확인함.
    def isLast(self,opt):
        if(opt==1 and self.wcursor >= self.type):return 1
        if(opt==0 and self.rcursor >= self.type):return 1
        return 0

    # 커서의 상태가 오버플로우되어 있는 상태인가?
    def isOverflow(self):
        if(self.overflow):
            self.overflow = 0
            return 1
        else:return 0

  # Binary to python type int
    def unpack(self,size,data,mode=">"):
        if(mode==">"):    # Big Endian
            if  (size==1):return struct.unpack('>B',data)
            elif(size==2):return struct.unpack('>H',data)
            elif(size==4):return struct.unpack('>L',data)                
            elif(size==8):return struct.unpack('>Q',data)
        else:            # Little Endian
            if  (size==1):return struct.unpack('<B',data)
            elif(size==2):return struct.unpack('<H',data)
            elif(size==4):return struct.unpack('<L',data)                
            elif(size==8):return struct.unpack('<Q',data)            
        return 0

  # Parsing Packet Header
    def parse(self):
        _container = []
        for _iter,_val in enumerate(PACKET_HEADER_STACK.HEADER_STACK):
            data = 0
            if  (_val=="PAYLOAD"):data = self.payload
            elif(_val=="DC"):data = self.get_hash()
            else:
                data = self.get_header(PACKET_HEADER_STACK.HEADER_PAIR[_val][0],
                                     data,PACKET_HEADER_STACK.HEADER_PAIR[_val][1])
            _container.append(data)
        self.parseinfo = OrderedDict(zip(PACKET_HEADER_STACK.HEADER_STACK,_container))
        
    # 패킷 리셋
    def reset(self):
        self.wcursor    = 0
        self.rcursor    = 0
        self.overflow    = 0
        self.temp        = None
        self.accessed     = False
        self.packet     = c_uint8*self.type
        self.packet        = self.packet(0,)
        self.parseinfo    = dict()
        self.init_header()
        self.init_payload()  
        
# example to use
if __name__ == '__main__':

    retv     = 0
    retv2    = 0
    
    # 패킷 생성
    pkt      = PACKET(PACKET_HEADER_CONFIG.PACKET_FULL,PACKET_HEADER_CONFIG.DATA_LENGTH)
    data     = c_uint8*PACKET_HEADER_CONFIG.DATA_LENGTH
    pkt.set_packet(data(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,0xFF,0xA0,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63))

    # 해더 바디 분리
    pkt.split()
    print("HEADER     : ")
    pkt.print_header()
    print("BODY     : ")
    pkt.print_payload(PACKET_HEADER_CONFIG.XBEE_DATA_LEN)

    # 해더 정보 and 해더 수정하기 
    retv = pkt.get_header(PACKET_HEADER_CONFIG.MASK_DST,retv,8)
    print(retv)
    pkt.set_header(PACKET_HEADER_CONFIG.MASK_SRC,0x001A7DDA7113,8)
    pkt.set_header(PACKET_HEADER_CONFIG.MASK_DST,0xDEADBEEF0986,8)
    pkt.set_header(PACKET_HEADER_CONFIG.MASK_PROTO,PACKET_HEADER_CONFIG.PROT_TEXT,1)    
    pkt.set_header(PACKET_HEADER_CONFIG.MASK_FLAGS,PACKET_HEADER_CONFIG.FLAG_ACK,1)
    pkt.set_header(PACKET_HEADER_CONFIG.MASK_FRAG,0,1)
    pkt.set_header(PACKET_HEADER_CONFIG.MASK_SEQ,0,2)
    pkt.cal_hash(PACKET_HEADER_CONFIG.DATA_LENGTH)
    pkt.set_hash()
    
    # 패킷 갱신하기
    print("BEFORE COMBINE and UPDATE : ")
    pkt.print(56)
    pkt.combine()
    pkt.update()
    print("AFTER COMBINE and UPDATE : ")
    pkt.combine()
    pkt.update()
    pkt.print(56)
    
    # 패킷 파싱하기
    pkt.parse()
    print(pkt.parseinfo)
