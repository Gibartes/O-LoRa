#coding:utf-8
#!/usr/bin/env python3

# Standard Libraries # ATmode handler ver.
import setproctitle
import time 
import os 
import sys 
import math
import signal
from multiprocessing import Process, Queue, Pipe, Event, Lock # Local IPC

# 3rd Party Libraries
from digi.xbee.exception import *
from digi.xbee.devices import DigiMeshDevice
from digi.xbee.util import utils
from digi.xbee.models.address import XBee64BitAddress
from digi.xbee.models.status import PowerLevel
from digi.xbee.models.options import DiscoveryOptions
from digi.xbee.models.status import NetworkDiscoveryStatus

# User Defined Libraries
sys.path.append('../bluetooth/python') # Xbeeprocess.py should be in the correct directory!
from packet import *
from define import *

NumOfOtherPids = 5
# 6 : Unit Test Mode (=4+2(userwriter,userreader))
# 5 : System Test Mode (=4+1(userreader))
# 4 : Real Use Mode

''' #### INSTRUCTIONS ####
At Real usage
1. Annotate all 'user*' at __main__ : line 1242~
2. 'after doing '1', change NumOfOtherPids from 6 -> 4 : line 26
'''

############################################################################
############################  Command Routine  #############################
############################################################################

def ForceReset(fullpacket,ExecutorPipe, localxbee, xnet, ev3): # won't be used
    pass


def Write(fullpacket,ExecutorPipe, localxbee, xnet, ev3):  # won't be used
    pass


def GetPowerLevel(fullpacket,ExecutorPipe, localxbee, xnet, ev3):  # won't be used
    pass


def SetPowerLevel(fullpacket,ExecutorPipe, localxbee, xnet, ev3): # won't be used
    pass


def GetChannelMask(fullpacket,ExecutorPipe, localxbee, xnet, ev3):  # won't be used
    pass


def SetChannelMask(fullpacket,ExecutorPipe, localxbee, xnet, ev3):  # won't be used
    pass


def GetPreambleId(fullpacket,ExecutorPipe, localxbee, xnet, ev3):
    ResultPacket = fullpacket[0:56] # copy header
    GotThis = bytearray(0) # what we get from get_parameter(). Cf) get_parameter returns bytearray

    try:
        GotThis += localxbee.get_parameter("HP")
        GotThislength = len(GotThis) 
    except (TimeoutException, InvalidOperatingModeException, XBeeException, ATCommandException) as e: # ERROR
        print("CmdError : Get Preamble ID")
        print('Error Detail : ', end='')
        print(e)
        # header
        ResultPacket[28] |= 0b01000000 # Set OPT |= 0b01000000 to notify error has occured
    else : # SUCCESS
        resultmsg = "Success : Get Preamble ID"
        print(resultmsg) 
        # header
        ResultPacket[28] &= 0b10111111 # Set OPT notify no error
        # body 
        ResultPacket += GotThis # when success, attach as datasection
        ResultPacket[36:38] = (GotThislength).to_bytes(2,'big') # denote data section length          
    finally :
        # SEND 
        ExecutorPipe.send(ResultPacket)
        

def SetPreambleId(fullpacket,ExecutorPipe, localxbee, xnet, ev3):
    # check data length
    if(int.from_bytes(fullpacket[36:38],'big') !=1):
        print("Set Error : Given data section length is wrong ")
    # Get Set Value
    SetValue = fullpacket[36:37] # since PreambleID(HP) is 1 byte
    
    try:
        localxbee.set_parameter("HP", SetValue) # get input as bytearray
    except (TimeoutException, InvalidOperatingModeException, XBeeException, ATCommandException) as e: # ERROR
        print("CmdError : Set Preamble ID")
        print('Error Detail : ', end='')
        print(e)
        # header
        fullpacket[28] |= 0b01000000 # Set OPT |= 0b01000000 to notify error has occured
    else : # SUCCESS
        resultmsg = "Success : Set Preamble ID"
        print(resultmsg) 
        # header
        fullpacket[28] &= 0b10111111 # Set OPT notify no error

    finally :
        # SEND 
        ExecutorPipe.send(fullpacket)
        print(localxbee.get_parameter("HP"))

def GetNetworkId(fullpacket,ExecutorPipe, localxbee, xnet, ev3):
    ResultPacket = fullpacket[0:56] # 2. copy header
    GotThis = bytearray(0) # what we get from get_parameter(). Cf) get_parameter returns bytearray

    try:
        GotThis += localxbee.get_parameter("ID")
        GotThislength = len(GotThis) 
    except (TimeoutException, InvalidOperatingModeException, XBeeException, ATCommandException) as e: # ERROR
        print("CmdError : Get Network ID")
        print('Error Detail : ', end='')
        print(e)
        # header
        ResultPacket[28] |= 0b01000000 # Set OPT |= 0b01000000 to notify error has occured
    else : # SUCCESS
        resultmsg = "Success : Get Network ID"
        print(resultmsg) 
        # header
        ResultPacket[28] &= 0b10111111 # Set OPT notify no error
        # body 
        ResultPacket += GotThis # when success, attach as datasection
        ResultPacket[36:38] = (GotThislength).to_bytes(2,'big') # denote data section length 
    finally :
        # SEND 
        ExecutorPipe.send(ResultPacket)


def SetNetworkId(fullpacket,ExecutorPipe, localxbee, xnet, ev3): 
    # check data length
    if(int.from_bytes(fullpacket[36:38],'big') !=2):
        print("Set Error : Given data section length is wrong ")
    # Get Set Value
    SetValue = fullpacket[36:38] # since PreambleID(HP) is 2 byte

    try:
        localxbee.set_parameter("ID", SetValue) # get input as bytearray
    except (TimeoutException, InvalidOperatingModeException, XBeeException, ATCommandException) as e: # ERROR
        print("CmdError : Set NetWork ID")
        print('Error Detail : ', end='')
        print(e)
        # header
        fullpacket[28] |= 0b00000000 # Set OPT |= 0b01000000 to notify error has occured
    else : # SUCCESS
        resultmsg = "Success : Set Network ID"
        print(resultmsg) 
        # header
        fullpacket[28] &= 0b10111111 # Set OPT notify no error
        
    finally :
        # SEND 
        ExecutorPipe.send(fullpacket)
        print(localxbee.get_parameter("ID"))
    

def GetNodeIdentifier(fullpacket,ExecutorPipe, localxbee, xnet, ev3):
    ResultPacket = fullpacket[0:56] # copy header
    GotThis = bytearray(0) # what we get from get_parameter(). Cf) get_parameter returns bytearray

    try:
        GotThis += localxbee.get_parameter("NI")
        GotThislength = len(GotThis) 
    except (TimeoutException, InvalidOperatingModeException, XBeeException, ATCommandException) as e: # ERROR
        print("CmdError : Get Node Identifier")
        print('Error Detail : ', end='')
        print(e)
        # header
        ResultPacket[28] |= 0b01000000 # Set OPT |= 0b01000000 to notify error has occured
    else : # SUCCESS
        resultmsg = "Success : Get Node Identifier"
        print(resultmsg) 
        # header
        ResultPacket[28] &= 0b10111111 # Set OPT notify no error
        # body (just in case)
        ResultPacket += GotThis # when success, attach as datasection
        ResultPacket[36:38] = (GotThislength).to_bytes(2,'big') # denote data section length 
    finally :
        # SEND 
        ExecutorPipe.send(ResultPacket)


def SetNodeIdentifier(fullpacket, ExecutorPipe, localxbee, xnet, ev3):
    # get Data length
    datalength = int.from_bytes(fullpacket[36:38],'big')

    # check data length
        # data length will be dynamic 1~20.

    # Get Set Value
    SetValue = fullpacket[56:56+datalength] 

    """ # OLD STYLE : check byte by byte~
    RealSetValue = bytearray(0) # get just the name from SetValue 
    for index in range(len(SetValue)): # index 0,1,...,19 
        if(SetValue[index] == 0x20): # if 공백문자, break during loop
            break
        else:
            RealSetValue += (SetValue[index]).to_bytes(1,'big') # else append that position's byte     
    """
    try:
        localxbee.set_parameter("NI", SetValue) # get input as bytearray
    except (TimeoutException, InvalidOperatingModeException, XBeeException, ATCommandException) as e: # ERROR
        print("CmdError : Set Node Identifier")
        print('Error Detail : ', end='')
        print(e)
        # header
        fullpacket[28] |= 0b01000000 # Set OPT |= 0b01000000 to notify error has occured
    else : # SUCCESS
        resultmsg = "Success : Set Node Identifier"
        print(resultmsg) 
        # header
        fullpacket[28] &= 0b10111111 # Set OPT notify no error
    finally :
        # SEND 
        ExecutorPipe.send(fullpacket)
        print(localxbee.get_parameter("NI"))

def GetMyAddress(fullpacket,ExecutorPipe, localxbee, xnet, ev3): 
    ResultPacket = fullpacket[0:56] # copy header
    GotThis = bytearray(0) # what we get from get_parameter(). Cf) get_parameter returns bytearray

    try:
        GotThis += bytes(localxbee.get_64bit_addr())
        GotThislength = len(GotThis) 
    except (TimeoutException, InvalidOperatingModeException, XBeeException, ATCommandException) as e: # ERROR
        print("CmdError : Get My Address")
        print('Error Detail : ', end='')
        print(e)
        # header
        ResultPacket[28] |= 0b01000000 # Set OPT |= 0b01000000 to notify error has occured
    else : # SUCCESS
        resultmsg = "Success : Get My Address"
        print(resultmsg) 
        # header
        ResultPacket[28] &= 0b10111111 # Set OPT notify no error
        # body 
        ResultPacket += GotThis # when success, attach as datasection
        ResultPacket[36:38] = (GotThislength).to_bytes(2,'big') # denote data section length 
    finally :
        # SEND 
        ExecutorPipe.send(ResultPacket)


def GetDisocveryTime(fullpacket,ExecutorPipe, localxbee, xnet, ev3): 
    ResultPacket = fullpacket[0:36] # copy header
    GotThis = bytearray(0) # what we get from get_parameter(). Cf) get_parameter returns bytearray

    try:
        GotThis += ((int)(xnet.get_discovery_timeout())).to_bytes(1,'big')
        GotThislength = len(GotThis) 
    except (TimeoutException, InvalidOperatingModeException, XBeeException, ATCommandException) as e: # ERROR
        print("CmdError : Get Discovery Time")
        print('Error Detail : ', end='')
        print(e)
        # header
        ResultPacket[28] |= 0b01000000 # Set OPT |= 0b01000000 to notify error has occured
    else : # SUCCESS
        resultmsg = "Success : Get Discovery Time"
        print(resultmsg) 
        # header
        ResultPacket[28] &= 0b10111111 # Set OPT notify no error
        # body (just in case)
        ResultPacket += GotThis # when success, attach as datasection
        ResultPacket[36:38] = (GotThislength).to_bytes(2,'big') # denote data section length 
    finally :
        # SEND 
        ExecutorPipe.send(ResultPacket)


def SetDisocveryTime(fullpacket,ExecutorPipe, localxbee, xnet, ev3): # won't be used
    pass


def StartDisocveryProcess(fullpacket,ExecutorPipe, localxbee, xnet, ev3):

    # xnet.clear() # do not execute -> some other process may use xnet
    xnet.start_discovery_process()
    ev3.wait()     
    
    devices = xnet.get_devices()
    NumberOfdevices = len(devices) # max number of devices in a single XBee packet :  34
    i = 0 # accumulative number of added devices on packet
    
    # debug print
    print('number of devices :', NumberOfdevices)
    
    HeaderSection = fullpacket[0:56] # copy header
    DataSection = bytearray(0) # will add one by one at the end

    # luckily newly designed packet's datasection goes upto 952, and exactly 952 = 28*34.
    # DataSection = bytearray(952) -> will concatenate discovered device at resultpacket 
    # CAUTION : when dealing with bytearray, like barray[0:4]=b'1', this works but 0~3 will be size 1
    #           THUS! should give a same byte sized RHS for overwriting LHS 
    # header section is identical even though we send more than twice
    # thus, make once and paste if for every full packet's header 
    
    # - won't put number of devices, since we will give the length of data section 
    # - HeaderSection will be shared. Only "SEQ", "DataSectionLength' will be different
    # - DataSection will be re-initialized to zero, when fully occupied packet was sent  


    while (i < NumberOfdevices):
        # Add one device info(Addr(8 bytes),NI(20 bytes))
        DataSection += bytearray(devices[i].get_64bit_addr())   # + Addr          
        NI = (devices[i].get_node_id()).encode('utf8')         
        DataSection += bytearray(NI)                            # + NI
        DataSection += bytearray(20-len(NI))                    # pad empty space with \x00
        i += 1                                                  # finished putting one device info

        if (i%34) == 0: # Case 1. Full-fullpacket(consists of 34 device info)
            # send full packet 
            HeaderSection[30:32] =  b'\x00\x01' #  there's an another packet following, so Android should wait
            HeaderSection[36:38] = 952  # Data Secetion length, device info fully occupies data section(952=28*34)
                                        # User should acknowledge how many devices are there in a upcoming packet from header
                                        # thus, there's no need to give explicit number of devices at the data section
            ExecutorPipe.send(HeaderSection+DataSection) # SEND

            # for next use initialize datasection
            DataSection = bytearray(0) 

    # Case 2. send left
    HeaderSection[30:32] =  b'\x00\x00' #  END of exeternal sequence packets
    HeaderSection[36:38] = ((NumberOfdevices%34) * 28).to_bytes(2,'big') # Data Section length of leftovers
    ExecutorPipe.send(HeaderSection+DataSection)

    # JUST FOR DEBUGGING
    print('discover result datasection : ',DataSection)
    print('discover result packet datasection len : ',len(DataSection))


def SendSyncUnicast(fullpacket,ExecutorPipe, localxbee, xnet, ev3):
    
    # ---------------------------- CAUTION !! ------------------------------ 
    # the remote_device should be discovered in advance! thus xnet should have the dstdevice info.
    # ----------------------------------------------------------------------- 
    
    # 0. Common Header configuration 
    datalength = int.from_bytes(fullpacket[36:38],'big') # get Data Section length
    originalfulldata = fullpacket[56:56+datalength] # Pure data section of original fullpacket 
    header        = fullpacket[0:56]   # both used for xbeepacket & ResultPacket header
    header[0:8]   = bytes(localxbee.get_64bit_addr())   # SRC addr
                                                        # DST addr (defined by user)
    header[16:24] = bytes(localxbee.get_parameter("CM"))[1:9]  # use only 2~9th byte position; 8bytes
    header[24:25] = bytes(localxbee.get_parameter("HP"))
    header[26:28] = bytes(localxbee.get_parameter("ID"))   
    header[32:36] = (int(time.time())).to_bytes(4,'big') # common timestamp 

    # 1. Get needed send times
        # data length : 0,1,..,952
        # thus sendtimes : 1,2...,5
    NeededSendTimes = math.ceil(datalength/200) 
    print("Need Send Times : ", NeededSendTimes)
    
    #EOTindex = datalength 
    # old EOTindex : 0,1,...,987
    # new EOTindex : 0,1,...,951
    # (new) datalength : 0,1,...,952
    # If you are reusing 'EOTindex' code
    # Be careful about 
    # First. max val 987->951
    # Second. when EOTindex = datalength, check if there's error when EOT=988(or 952)

    '''
    # 1. Get where EOT is located at originalfulldata (EOT can be (0,1,...,987) : 988 types)
    EOTindex = None
    for i in range(0,988):
        if originalfulldata[i]==0x03:
            EOTindex = i
            break
    NeededSendTimes = math.ceil((EOTindex+1)/220);print('NeededSentime :' , NeededSendTimes) # the place where EOT is located    
    
    print('EOTINDEX:' , EOTindex) 
    '''

    # 2. Send Synchro UniCast
    SendResult = [None]*NeededSendTimes  # 1: Success, 0:Fail    
    #header[28:30] = EOTindex.to_bytes(2,'big') # we needed EOTindex info BUT now
    # since we have datalength info at header[36:38] don't need to use OLDSEQ[28:30] part. 
    DstAddr = XBee64BitAddress(fullpacket[8:16])
    DstDevice = xnet.get_device_by_64(DstAddr);print('print Dstdevice : ',DstDevice)        

    for i in range(NeededSendTimes): # possible i : 0,1,2,3,4
        try:
            if (i<NeededSendTimes-1): # Not LAST packet 
                header[29:30] = (i+1).to_bytes(1,'big') # internal packet sequence : 
                header[29] |= (NeededSendTimes<<5) # encrypt how many numbers of Xbeepackets are needed to send the input fullpacket   
                localxbee.send_data(DstDevice ,header+originalfulldata[200*i:200*i+200])
            elif (i==NeededSendTimes-1): # LAST packet
                header[29:30] = (i+1).to_bytes(1,'big')
                header[29] |= (NeededSendTimes<<5) # encrypt how many numbers of Xbeepackets are needed to send the input fullpacket
                localxbee.send_data(DstDevice ,header+originalfulldata[200*i:datalength])
        except:
            print("broadcasting fail")
            SendResult[i] = 0 # Send Fail
        else:
            SendResult[i] = 1 # Send Success 

    # upper 'header[28]' usage : how many number of Xbeepackets needed
    # lower 'header[28]' usage : success/fail

    # 3. Make Return Command Header & Datasection
    datasection = bytearray(0)
    header[28] |= (NeededSendTimes<<5) # 6,7,8 bit of OPTbyte denotes Total Sent Xbeepacket Numbers 
    for i in range(NeededSendTimes):    # from the (first xbeepkt)~(EOT xbeepkt)
        header[28] = header[28] | (0b00000001<<i)
        if SendResult[i]==1:           # 1. IF Success
            if (i<NeededSendTimes-1):
                datasection += originalfulldata[200*i:200*i+200]
            elif (i==NeededSendTimes-1):
                datasection += originalfulldata[200*i:datalength]
            
        elif SendResult[i]==0:         # 2. IF Fail
            datasection += b' <' + (i+1).to_bytes(1,'big') +b'> '

    # don't need padding EOT at the end 
    '''
    # must end with EOT
    if EOTindex!=987:
        datasection += b'\x03'
    #print('datasec : ', datasection)
    # make it 988 bytes
    datasection = datasection + bytearray(988-len(datasection))
    '''
    # 4. CMD_Result
    
    ResultPacket = header + datasection
    ExecutorPipe.send(ResultPacket)
  
    # debug
    for i in range(NeededSendTimes):
        print('Send Result of XBeepacket = ', i,SendResult[i])

def SendAsyncUnicast(fullpacket,ExecutorPipe, localxbee, xnet, ev3): # won't be used
    pass

def SendBroadcast(fullpacket, ExecutorPipe, localxbee, xnet, ev3): # should distinguish broadcast when received or should note when sending

    # 0. Common Header configuration 
    datalength = int.from_bytes(fullpacket[36:38],'big') # get Data Section length
    originalfulldata = fullpacket[56:56+datalength] # Pure data section of original fullpacket 
    header        = fullpacket[0:56]   # both used for xbeepacket & ResultPacket header
    header[0:8]   = bytes(localxbee.get_64bit_addr())   # SRC addr
                                                        # DST addr (defined by user)
    header[16:24] = bytes(localxbee.get_parameter("CM"))[1:9]  # use only 2~9th byte position; 8bytes
    header[24:25] = bytes(localxbee.get_parameter("HP"))
    header[26:28] = bytes(localxbee.get_parameter("ID"))   
    header[32:36] = (int(time.time())).to_bytes(4,'big') # common timestamp 

    # 1. Get needed send times
        # data length : 0,1,..,952
        # thus sendtimes : 1,2...,5
    NeededSendTimes = math.ceil(datalength/200) 
    print("Need Send Times : ", NeededSendTimes)
 
    # old EOTindex : 0,1,...,987
    # new EOTindex : 0,1,...,951
    # (new) datalength : 0,1,...,952
    # If you are reusing 'EOTindex' code
    # Be careful about 
    # First. max val 987->951
    # Second. when EOTindex = datalength, check if there's error when EOT=988(or 952)

    '''
    # 1. Get where EOT is located at originalfulldata (EOT can be (0,1,...,987) : 988 types)
    EOTindex = None
    for i in range(0,988):
        if originalfulldata[i]==0x03:
            EOTindex = i
            break
    NeededSendTimes = math.ceil((EOTindex+1)/220);print('NeededSentime :' , NeededSendTimes) # the place where EOT is located    
    
    print('EOTINDEX:' , EOTindex) 
    '''

    # 2. Send BroadCast
    SendResult = [None]*NeededSendTimes  # 1: Success, 0:Fail    
    #header[28:30] = EOTindex.to_bytes(2,'big') # we needed EOTindex info BUT now
    # since we have datalength info at header[36:38] don't need to use OLDSEQ[28:30] part. 

    for i in range(NeededSendTimes): # possible i : 0,1,2,3,4
        try:
            if (i<NeededSendTimes-1): # Not LAST packet 
                header[29:30] = (i+1).to_bytes(1,'big') # internal packet sequence : 
                header[29] |= (NeededSendTimes<<5) # encrypt how many numbers of Xbeepackets are needed to send the input fullpacket   
                localxbee.send_data_broadcast(header+originalfulldata[200*i:200*i+200])
            elif (i==NeededSendTimes-1): # LAST packet
                header[29:30] = (i+1).to_bytes(1,'big')
                header[29] |= (NeededSendTimes<<5) # encrypt how many numbers of Xbeepackets are needed to send the input fullpacket
                localxbee.send_data_broadcast(header+originalfulldata[200*i:datalength])
        except:
            print("broadcasting fail")
            SendResult[i] = 0 # Send Fail
        else:
            SendResult[i] = 1 # Send Success 

    # upper 'header[28]' usage : how many number of Xbeepackets needed
    # lower 'header[28]' usage : success/fail

    # 3. Make Return Command Header & Datasection
    datasection = bytearray(0)
    header[28] |= (NeededSendTimes<<5) # 6,7,8 bit of OPTbyte denotes Total Sent Xbeepacket Numbers 
    for i in range(NeededSendTimes):    # from the (first xbeepkt)~(EOT xbeepkt)
        header[28] = header[28] | (0b00000001<<i)
        if SendResult[i]==1:           # 1. IF Success
            if (i<NeededSendTimes-1):
                datasection += originalfulldata[200*i:200*i+200]
            elif (i==NeededSendTimes-1):
                datasection += originalfulldata[200*i:datalength]
            
        elif SendResult[i]==0:         # 2. IF Fail
            datasection += b' <' + (i+1).to_bytes(1,'big') +b'> '

    # don't need padding EOT at the end 
    '''
    # must end with EOT
    if EOTindex!=987:
        datasection += b'\x03'
    #print('datasec : ', datasection)
    # make it 988 bytes
    datasection = datasection + bytearray(988-len(datasection))
    '''
    # 4. CMD_Result
    
    ResultPacket = header + datasection
    ExecutorPipe.send(ResultPacket)
    #print("result packet header: ", ResultPacket[0:56]);print('----\n--------')
    #print("result packet body: ", ResultPacket[56:56+datalength])
    # debug
    for i in range(NeededSendTimes):
        print('Send Result of XBeepacket = ', i,SendResult[i])



def SwitchRoom(fullpacket, ExecutorPipe, localxbee, xnet, ev3): 
    SetValue = fullpacket[56:59] # PreambleID(HP) first byte, NetworkID(ID) second, third byte
    GotThis = bytearray(0)

    try:
        localxbee.set_parameter("HP", SetValue[0:1]) # get input as bytearray
        localxbee.set_parameter("ID", SetValue[1:3])
    except (TimeoutException, InvalidOperatingModeException, XBeeException, ATCommandException) as e: # ERROR
        print("CmdError : SwitchRoom")
        print('Error Detail : ', end='')
        print(e)
        # header
        fullpacket[28] |= 0b01000000 # Set OPT |= 0b01000000 to notify error has occured
    else : # SUCCESS
        resultmsg = "Success : Switch Room"
        print(resultmsg) 
        # header
        fullpacket[28] &= 0b10111111 # Set OPT notify no error
    finally :
        # SEND 
        ExecutorPipe.send(ResultPacket)
        print('after Switiching Room HP: ',localxbee.get_parameter("HP"))
        print('after Switiching Room ID: ',localxbee.get_parameter("ID"))   


routine_dic = {
    b'\x00\x00' : ForceReset,           # ooo      
    b'\x00\x01' : Write,                # won't be used
    b'\x00\x02' : GetPowerLevel,        # won't be used
    b'\x00\x03' : SetPowerLevel,        # won't be used
    b'\x00\x04' : GetChannelMask,       # won't be used
    b'\x00\x05' : SetChannelMask,       # won't be used
    b'\x00\x06' : GetPreambleId,        # ooo
    b'\x00\x07' : SetPreambleId,        # ooo
    b'\x00\x08' : GetNetworkId,         # ooo
    b'\x00\x09' : SetNetworkId,         # ooo
    b'\x00\x0a' : GetNodeIdentifier,    # ooo
    b'\x00\x0b' : SetNodeIdentifier,    # ooo
    b'\x00\x0c' : GetMyAddress,         # won't be used
    b'\x00\x0d' : GetDisocveryTime,     # oo
    b'\x00\x0e' : SetDisocveryTime,     # oo
    b'\x00\x0f' : StartDisocveryProcess,# ooo      
    b'\x00\x10' : SendSyncUnicast,      # ooo
    b'\x00\x11' : SendAsyncUnicast,     # won't be used   
    b'\x00\x12' : SendBroadcast,        # ooo
    b'\x00\x13' : SwitchRoom            # ooo
    
}


##################################################################
####################### Process Definition #######################
##################################################################
    
def sync_client_read(CmdQueue, PidQueue, ReseterPid): 
    
    # First create HK pipe
    HKpipe = ObjectPipe("/tmp/.xbolora.in") # ("/tmp/.xbolora.in")
    HKpipe.mkfifo()
    HKpipe.open(os.O_RDONLY)

    # Get my PID 
    MyPid = os.getpid()
    ReseterPid.put(MyPid)

    # make a copy of PidQueue
    PidQueueCopy = []
    for i in range(NumOfOtherPids):
        PidQueueCopy.append(PidQueue.get())
    
    # set signal routine
    def ATmodeErrorRoutine(signum, frame):
        nonlocal MyPid
        nonlocal PidQueueCopy
        global NumOfOtherPids
        nonlocal HKpipe
        
        print("Sync client received ATmode Error signal from Executor")
        # 1.1 Kill other subprocess, except parent process
        for i in range(len(PidQueueCopy)):
            os.kill(PidQueueCopy[i],signal.SIGKILL) 
        # 1.2 Kill myself
        try:
            HKpipe.close()
        except:
            pass
        #print('sync_client_reader will die..')
        os.kill(MyPid, signal.SIGKILL) 
    # add signal routine
    signal.signal(signal.SIGUSR1, ATmodeErrorRoutine)    


    # MAIN LOOP OF SYNC_CLIENT_READ
    while True:
        # 0. receive from pipe HK made 
        fullpacket = bytearray(HKpipe.recv()) # originpacket(packet.Packet) 
        
        # A. check if the packet is a 'FULL' packet 
        if len(fullpacket) <= 1008: 
            print('CMD_Listener got full cmd packet under 1008')
            # 1. IF Reset(Restart) Flag up
            if (fullpacket[28] >= 0b10000000):
                # 1.1 Kill other subprocess, except parent process
                for i in range(NumOfOtherPids):
                    os.kill(PidQueueCopy[i],signal.SIGKILL)  
                # 1.2 Kill myself
                try:
                    HKpipe.close()
                except:
                    pass
                #print('sync_client_reader will die..')
                os.kill(MyPid, signal.SIGKILL)             
            '''
            # 1.5 check for XBEESLEEPCMD ; b'\xAA\x01'
            elif (fullpacket[30:32] == b'\xAA\x01'):
                for i in range(NumOfOtherPids):
                    print(PidQueueCopy[i], i)
                    os.kill(PidQueueCopy[i],signal.SIGSTOP)             
            # 1.6 check for XBEEWAKECMD ; b'\xAA\x02'
            elif (fullpacket[30:32] == b'\xAA\x02'):
                #print('gonna wake you up')
                for i in range(NumOfOtherPids):
                    print(PidQueueCopy[i], i)
                    os.kill(PidQueueCopy[i],signal.SIGCONT)              
            '''
            # 2. Dump 
            else:
                CmdQueue.put(fullpacket) 
        # B. if not full packet bypass
        else:
            print("/tmp/.xbolora.in Broke Detected!!") # ("/tmp/.xbolora.in")
            HKpipe = ObjectPipe("/tmp/.xbolora.in") # ("/tmp/.xbolora.in")
            HKpipe.open(os.O_RDONLY)
    
def executor(CmdQueue, MsgQueue, ExecutorPipe,  ev3, PidQueue, ReseterPid, WaitPipe): 
    PidQueue.put(os.getpid())#;print('executor putted mypid')
    Reseter = ReseterPid.get()#;print('executor got reseterpid')

    # Instantiate an XBee device object
    PORT = "/dev/ttyUSB0"  
    BAUD_RATE = 9600 # non-programmable should be 9600
    localxbee = DigiMeshDevice(PORT, BAUD_RATE)
    
    CMD_NUM = None
    while True:
        # Open device
        try:
            print('before opening device...')
            localxbee.open()
        except (TimeoutException, InvalidOperatingModeException, XBeeException) as e:
            print(e) # SHOULD ALERT INTERNAL RESET SIGNAL ROUTINE TO REBOOT, BUT FOR NOW SIMPLY PRINT OUT ERROR MESSAGE FOR DEBUGGING
            print('Started ATmode at Executor!! Will send AT mode signal for Reboot')
            os.kill(Reseter, signal.SIGUSR1) # -> Signal synsclient-reader to reset whole XBEE!
            time.sleep(10)
        else:
            # Create Xnet Instance & Configure
            print('successfuly opened localxbee device')
            xnet = localxbee.get_network()
            xnet.set_discovery_options({DiscoveryOptions.DISCOVER_MYSELF, DiscoveryOptions.APPEND_DD})
            xnet.set_discovery_timeout(4)
            
            # Add callback routine.
            # 1. message callback
            localxbee.add_data_received_callback(my_data_received_callback)
            # 2. discovery finished callback
            xnet.add_discovery_process_finished_callback(callback_discovery_finished)
            # 3. Repeat below infinitely
            while (True):
                # 1. Fetch
                fullpacket = CmdQueue.get() # 1024 byte fullpacket 36+988 bytearray
                # 2. Figure out command 
                CMD_NUM = fullpacket[39:40]
                print('before routine')
                # 3. Execute (routine functions are defined at rountine.py module)
                try:
                    routine_dic[bytes(CMD_NUM)](fullpacket, ExecutorPipe, localxbee, xnet, ev3)
                except:
                    print("Wrong Command Number")
                    pass
                else :
                    # 4. wait until syn_client write send
                    WaitPipe.recv();print('Executor woke up')
        finally:
            print('finally close localxbee') # only reaches here when it's too late, but won't come here..
            if localxbee is not None and localxbee.is_open():
                localxbee.close()
                
                    

def sync_client_write(Sender1Pipe,  ev2, PidQueue, DebugQueue, lk, AwakePipe): 
    
    PidQueue.put(os.getpid())
    
    HKpipe = ObjectPipe("/tmp/.xbolora.out") # ("/tmp/.xbolora.out")
    HKpipe.mkfifo()
    HKpipe.open(os.O_WRONLY)
    
    # FIRST SEND GATE.PY MSG TO OPEN THE GATE AGAIN
    #HOST = '127.0.0.1'
    #PORT = 10732
    #OpenGateRequest = PACKET(PACKET_FULL,PACKET_DATA_LEN)
    #OpenGateRequest.put_data(30,0xAA)
    #OpenGateRequest.put_data(31,0x00)    
    #sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #sock.connect((HOST,PORT))
    #sock.sendall(OpenGateRequest.packet)

    while(True):

        # 1. receive one full(1024)packet(result packet)
        ResultPacket = Sender1Pipe.recv()                       # 1024byte fullpacket from executorpipe
        convResultPacket = PACKET(PACKET_FULL,PACKET_DATA_LEN)  # packet.Packet
        AwakePipe.send(b'Wake Executor')                        # Wake up Executor 
        
        # 2. convert fullpacket -> packet.Packet
        for i in range(len(ResultPacket)): # length == 1024
            convResultPacket.put_data(i, ResultPacket[i]) # copy i'th position's byte 

        # 3. priority: sync > async. Thus block async_client
        ev2.clear();print('ev2 clear')  
        lk.acquire();print('sync client(WR) acquired lock')

        # 4. send this(packet.Packet) to HK's pipe 
        try:
            HKpipe.write(convResultPacket.packet)
        except:
            print("Broken HKPipe at Sync-client(WR) [XBee >>> Gate]")
            HKpipe.close()
            HKpipe = ObjectPipe("/tmp/.xbolora.out") # ("/tmp/.xbolora.out")
            HKpipe.open(os.O_WRONLY)

        # For debugging
        DebugQueue.put(ResultPacket)   
        
        lk.release();print('sync client(WR) released lock')
        # 5. unblock async_client
        ev2.set();print('ev2 set')     # free client2 from blocking
    

    

def reorganizer(MsgQueue, DatabasePipe, PidQueue):
    '''
    1.  Fetch Message from MsgQueue
    2.  Distinguish each message, and put them in a dictionary
    3.  When some specific field's Ready-to-relay requirement(e.g. # minipacket==5 ) is fulfilled
        Each entry's first 4 byte represents the latest received time.
        During spinlock, we will check for too old entries by comparing with current time.
    4.  pop out that field and transform into a full-packet
    5.  send to client2 using pipe
    '''
    PidQueue.put(os.getpid())    
    database = {}
    TIMEBOUND = 3   # 3 sec
    latesttime = None
    LostNotice = b'[-----Lost Data Section-----]'
    LostNoticeLen = len(LostNotice)

    while(True):
        num = 0 # number of looping while(MsgQueue.empty) 
        partialdata = bytearray(0)
        FlushEntry = []   # [addr1,ts_A,addr2,ts_B,addr1,ts_C,...] 홀짝이 한 쌍이야 -> 키 중복 막으려고 할 수 없이 list로 
        FlushPacketList = []  # clear
        while(MsgQueue.empty()):
            num +=1
            time.sleep(0.1) # check every 100ms 
            currenttime = time.time()
            
            # Traverse database for finding potential flushable entry

            if(len(database)!=0):   # if database is empty, there's no need to check
                for k1,v1 in database.items():  # k1:addr, v1: {timestamp_1: [latesttime,mp1,...,mp5,count,commonheader,maxcount],...}
                    for k2,v2 in v1.items():    # k2:timestamp_i, v2: [latesttime_b,mp1,mp2,...,mp5,count,commonheader,maxcount] where mp = 56header+200data : 9 features

                        # FLUSH OUT selected OLD ENTRY
                        latesttime = int.from_bytes(bytes(v2[0]),'big')
                        if(currenttime-latesttime > TIMEBOUND):
                            # CASE 1. FlushEntry is Emtpy
                            if len(FlushEntry)==0: 
                                print("I'M AT CASE 1 HAHAHAHA")    
                                # 1. Extract datasection 
                                maxcount = database[k1][k2][8]
                                datalength = int.from_bytes(database[k1][k2][7][36:38],'big')
                                for i in range(1,maxcount+1): # maxcount : number of peer packets 1~5
                                    if( len(database[k1][k2][i]) == 0 ):    # CASE A. missing packet -> concat(Lostnotice)
                                        partialdata += LostNotice           
                                    else:                                   # CASE B. existing packet -> concat(Datasection)
                                        if i !=maxcount:
                                            partialdata += database[k1][k2][i][56:256]
                                        elif i == maxcount:
                                            partialdata += database[k1][k2][i][56:56+datalength%200]

                                # THERE's no need of using EOT        
                                '''
                                # 2. Check EOT(0x03)'s existence 
                                IsEOThere = False
                                for j in range(len(partialdata)):
                                    if(partialdata[j]==0x03):
                                        IsEOThere = True
                                        break
                                if(IsEOThere == False): # If EOT doesn't exist concate 0x03 at end of paritaldata 
                                    partialdata += b'\x03' 
                                else:                   # Else, do nothing cuz the phone will only read until EOT
                                    continue 
                                
                                #### until here the partial data must have a EOT character!!! ####
                                #### either it was concated here or already existed in the received packet ####
                                '''

                                # 3. Create
                                commonheader = database[k1][k2][7]  # get commonheader
                                commonheader[36:38] = (datalength).to_bytes(2,'big')
                                FlushPacketList.append(commonheader + partialdata) # header(56)+ partialdata
                                print('case 1 number of flushing packetlist : ',len(FlushPacketList))
                                FlushEntry.append(k1)
                                FlushEntry.append(k2)                                 
                            
                            # CASE 2. FlushEntry isn't Empty
                            else:  
                                #print("I'M AT CASE 2 HAHAHAHA")  
                                for i in range(0,len(FlushEntry),2):
                                    if not (k1==FlushEntry[i] and k2==FlushEntry[i+1]):         
                                        # 1. Extract datasection 
                                        maxcount = database[k1][k2][8]
                                        datalength = int.from_bytes(database[k1][k2][7][36:38],'big')
                                        for i in range(1,maxcount+1):
                                            if( len(database[k1][k2][i]) == 0 ):    # CASE A. missing packet -> concat(Lostnotice)
                                                partialdata += LostNotice           
                                            else:                                   # CASE B. existing packet -> concat(Datasection)
                                                if i !=maxcount:
                                                    partialdata += database[k1][k2][i][56:256]
                                                elif i == maxcount:
                                                    if (datalength % 200 == 0):  
                                                        partialdata += database[k1][k2][i][56:256] # e.g datalength = 800
                                                    else:
                                                        paritaldata += database[k1][k2][i][56:56+datalength%200] # e.g datalength = 801                  

                                        '''
                                        # 2. Check EOT(0x03)'s existence 
                                        IsEOFhere = False
                                        for j in range(len(partialdata)):
                                            if(partialdata[j]==3):
                                                IsEOFhere = True
                                                break
                                        if(IsEOFhere == False): # If EOT doesn't exist concate 0x03 at end of paritaldata 
                                            partialdata += b'\x03' 
                                        else:                   # Else, do nothing cuz the phone will only read until EOT
                                            continue 
                                        
                                        #### until here the partial data must have a EOT character!!! ####
                                        #### either it was concated here or already existed in the received packet ####
                                        '''

                                        # 3. Create
                                        commonheader = database[k1][k2][7]  # get commonheader
                                        commonheader[36:38] = datalength.to_bytes(2,'big')
                                        FlushPacketList.append(commonheader + partialdata) # header(36)+partialdata+zeropad
                                        print('case 1 number of flushing packetlist : ',len(FlushPacketList))
                                        FlushEntry.append(k1)
                                        FlushEntry.append(k2)         
                                    
                                    else: #  (k1==FlushEntry[i] and k2==FlushEntry[i+1]) == True:
                                        continue # to prevent overwrite & duplication, do nothing when k1,k2 is already in FlushEntry                                        

        # Escaped age check loop due to Msgqueue isn't empty
        print('num of age loop : ' , num)
        print('len of FlushEntry : ', len(FlushEntry))

        # delete outdated entry 
        for i in range(0,len(FlushEntry),2):
            k1 = FlushEntry[i]; k2 = FlushEntry[i+1] 
            del database[k1][k2];print('deleted old entry')
            if len(database[k1])==0: # if became a zomebie(empty) entry 
                del database[k1];print('deleted zombie entry')

        print('number of flushing packetlist : ',len(FlushPacketList))
        
        # send non-full old Fullpackets
        for i in range(len(FlushPacketList)):
            DatabasePipe.send(FlushPacketList[i])
            print("sent  old entry")
        
        # 2. fetch one from MsgQueue
        xbeedata = MsgQueue.get()             # xbeedata : 256byte bytearray(36header+220data)      
        currenttime_b = (int)(currenttime).to_bytes(4,'big')
        srcaddr = bytes(xbeedata[0:8])        # get key1 : addr
        timestamp = bytes(xbeedata[32:36])    # get key2 : timestamp
        datasection_full = bytearray(0)        # ideally will become if all 5 comes 988byte (220+220+220+220+108) bytearray

        # 3. search if there pre-exists a same entry(deviceAddr)
        ToFlushEntry = {} # clear -> here if statifies 5 element's len, that will be t,h,e ONLY entry 
        if srcaddr not in database:
            if timestamp not in srcaddr:    # CASE1. first packet of Fullpacket -> create new entry
                database[srcaddr] = {timestamp : [b'']*9}; print('case 1') #  make a empty array
                database[srcaddr][timestamp][0] = currenttime_b # First index always denotes latest receive time of certain entry
                internalseq = xbeedata[29]&0b00000111   # figure out sequence
                database[srcaddr][timestamp][internalseq] = xbeedata # put it in the right position
                database[srcaddr][timestamp][6] = 1
                database[srcaddr][timestamp][7] = xbeedata[0:56] # remember header -> needed when nonfull flush happens
                database[srcaddr][timestamp][8] = (xbeedata[29] & 0b11100000)>>5 # maxcount : number of peer-xbeepackets (1~5) 

                # will only continue if EOT packet was the first packet
                maxcount = database[srcaddr][timestamp][8]  
                if (database[srcaddr][timestamp][6] == maxcount): # Every XBeepacket arrived.(acutally only packet...)
                    # CASE1. not last packet : i=1~maxcount-1 
                    for i in range(1,maxcount):                                 
                        datasection_full += database[srcaddr][timestamp][i][56:256]
                    # CASE2. last packet : i=maxcount
                    datalength = int.from_bytes(database[srcaddr][timestamp][7][36:38],'big')    
                    datasection_full += database[srcaddr][timestamp][maxcount][56:56+datalength%200] 
                    commonheader = database[srcaddr][timestamp][7]          # HEADER SECTION
                    DatabasePipe.send(commonheader + datasection_full)
                    print('FULL XBEEPACKET FLUSHED : CASE 1')
                    ToFlushEntry[srcaddr] = timestamp   

        elif srcaddr in database: # found key1 entry(srcaddr)
            if timestamp not in database[srcaddr]:    # CASE2. from same remote_device, but different time -> create new entry
                database[srcaddr][timestamp] = [b'']*9
                database[srcaddr][timestamp][0] = currenttime_b
                internalseq = xbeedata[29]&0b00000111   # figure out sequence
                database[srcaddr][timestamp][internalseq] = xbeedata;print('case 2') # put it in the right position
                database[srcaddr][timestamp][6] = 1
                database[srcaddr][timestamp][7] = xbeedata[0:56] # remember header -> needed when nonfull flush happens
                database[srcaddr][timestamp][8] = (xbeedata[29] & 0b11100000)>>5 # maxcount : number of peer-xbeepackets  

                # will only continue if EOT packet was the first packet
                maxcount = database[srcaddr][timestamp][8]  
                if (database[srcaddr][timestamp][6] == maxcount): # Every XBeepacket arrived.(acutally only packet...)
                    # CASE1. non EOT packet : i=1~maxcount-1 
                    for i in range(1,maxcount):                                 
                        datasection_full += database[srcaddr][timestamp][i][56:256]
                    # CASE2. EOT packet : i=maxcount
                    datalength = int.from_bytes(database[srcaddr][timestamp][7][36:38],'big')    
                    datasection_full += database[srcaddr][timestamp][maxcount][56:56+datalength%220] 
                    commonheader = database[srcaddr][timestamp][7]          # HEADER SECTION
                    DatabasePipe.send(commonheader + datasection_full)
                    print('FULL XBEEPACKET FLUSHED : CASE 2')
                    ToFlushEntry[srcaddr] = timestamp                        
                      

            else:   # CASE3. from same remote_device, same sent time -> append 
                database[srcaddr][timestamp][0] = currenttime_b;print('case 3') # update latest receive time  
                internalseq = xbeedata[29]&0b00000111   # figure out sequence 
                database[srcaddr][timestamp][internalseq] = xbeedata     
                database[srcaddr][timestamp][6] += 1    # plus count
            
                # check if this entry's every peer Xbeepackets arrived
                maxcount = database[srcaddr][timestamp][8];print('\n\nmaxcount : ',maxcount )
                if (database[srcaddr][timestamp][6] == maxcount): # Every XBeepacket arrived.
                    # CASE1. non EOT packet : 1~maxcount-1 
                    for i in range(1,maxcount):                                 
                        datasection_full += database[srcaddr][timestamp][i][56:256]
                    # CASE2. EOT packet : maxcount
                    datalength = int.from_bytes(database[srcaddr][timestamp][7][36:38],'big')    
                    datasection_full += database[srcaddr][timestamp][maxcount][56:56+datalength%220] 
                    commonheader = database[srcaddr][timestamp][7]          # HEADER SECTION
                    DatabasePipe.send(commonheader + datasection_full)
                    print('FULL XBEEPACKET FLUSHED : CASE 3')
                    ToFlushEntry[srcaddr] = timestamp                        

        # delete entry after creating fullpacket -> 새로운 들어온 xbeepackt에 대해 하나의 entry만 바뀌기 때문에 하나 밖에 없음.
        for srcaddr,timestamp in ToFlushEntry.items():             
            del database[srcaddr][timestamp]                      
            if len(database[srcaddr])==0: # check if it became a zombie entry
                del database[srcaddr]
       

        print('length of database now :' , len(database))    
   


def async_client(Sender2Pipe, ev2, PidQueue, DebugQueue, lk): 
  
    PidQueue.put(os.getpid())

    HKpipe = ObjectPipe("/tmp/.xbolora.out") # ("/tmp/.xbolora.out")
    HKpipe.mkfifo()
    HKpipe.open(os.O_WRONLY)

    while (True):
        # 1. receive one full(<=1008)packet(result packet)
        FullPacket = Sender2Pipe.recv() # <=1008byte fullpacket
        #print('async client recieved message file')
        convFullPacket = PACKET(PACKET_FULL,PACKET_DATA_LEN) # packet.Packet
        
        # 2. convert fullpacket -> packet.Packet
        for i in range(len(FullPacket)): # length == 1024
            convFullPacket.put_data(i, FullPacket[i]) # copy i'th position's byte 
        #print('async client converted message file')
        # 3. wait for sync_client, if he is to sending  
        print('ev2 wait')
        ev2.wait()
        lk.acquire();print('async client acquired lock')
        #print('async client about to put MSG file to HKpipe')
        # 4. send this(packet.Packet) to HK's pipe / otherwise user UserQueue for testing
        try:
            HKpipe.write(convFullPacket.packet)
        except:
            print("Broken HKPipe at Async Client [XBee >>> Gate]")
            HKpipe.close()
            HKpipe = ObjectPipe("/tmp/.xbolora.out") # ("/tmp/.xbolora.out")
            HKpipe.open(os.O_WRONLY)

        #print('async client putted MSG in HKpipe')
        # For debugging send to DEBUGGER USER PIPE
        DebugQueue.put(FullPacket)
        lk.release();print('async client released lock')  


##################################################################
################## Callback & Signal Routine #####################
##################################################################

def my_data_received_callback(xbee_message):

    global MsgQueue # executor process를 스코프로 함
    MsgQueue.put(xbee_message.data) # xbee_message.data == 56header+200data
    print("received message at callback")


def callback_discovery_finished(status):
    
    global ev3
    if status == NetworkDiscoveryStatus.SUCCESS:
        print("Discovery process finished successfully.")
    else:
        print("There was an error discovering devices: %s" % status.description)
    ev3.set() 
    # there is some hazard of deadlock happening if callback discovery finishes really really fast
    # But, since discovery takes at least 4 seconds... there almost 0.001% of deadlock happening by ev3.

##################################################################
####################### test server ##############################
##################################################################

def userwriter(PidQueue): # userwriter should be OFF on Acutal Usage
    #PidQueue.put(os.getpid())

    # HK pipe (User 입장에선 명령을 내리는 파이프)
    CmdPipe = ObjectPipe("/tmp/.xbolora.in") # ("/tmp/.xbolora.in")
    CmdPipe.mkfifo()
    CmdPipe.open(os.O_WRONLY)

    j=0
    ResetMoment = 5;DiscoverMoment = 10;SleepMoment=3;WakeMoment=10
    while True:
        # 1. configure test input(full bytearray packet)
        sendpkt = PACKET(PACKET_FULL,PACKET_DATA_LEN)
        data = c_uint8*PACKET_FULL
        sendpkt.set_packet(data(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,0xFF,\
        0xA0,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,0xB0,0xC0,0xD0,0xAA,0xBC,0xA0,0xFF,0xDB,0x9C,0x80,0x20,0x03))
        '''
            b'\x00\x00' : ForceReset, # ok
            b'\x00\x01' : Write, # pass
            b'\x00\x02' : GetPowerLevel, # pass
            b'\x00\x03' : SetPowerLevel, # pass
            b'\x00\x04' : GetChannelMask, # ok
            b'\x00\x05' : SetChannelMask, # ok ->  user configure permission...not sure
            b'\x00\x06' : GetPreambleId, # ok
            b'\x00\x07' : SetPreambleId, # ok
            b'\x00\x08' : GetNetworkId, # ok
            b'\x00\x09' : SetNetworkId, # ok
            b'\x00\x0a' : GetNodeIdentifier, # ok
            b'\x00\x0b' : SetNodeIdentifier, # ok 
            b'\x00\x0c' : GetMyAddress, # ok
            b'\x00\x0d' : GetDisocveryTime, # ok
            b'\x00\x0e' : SetDisocveryTime, # ok
            b'\x00\x0f' : StartDisocveryProcess,# ok      
            b'\x00\x10' : SendSyncUnicast, # ok
            b'\x00\x11' : SendAsyncUnicast, #  ok  
            b'\x00\x12' : SendBroadcast, # ok
            b'\x00\x13' : SwitchRoom # ok
        '''
        ## Change CMD ##
        sendpkt.put_data(30,0x00) # 0x00  
        sendpkt.put_data(31,0x10) # put here

        ## Change Dst Addr ##
        destaddr = bytearray(b'\x00\x13\xa2\x00A{2E')
        for i in range(8,16):
            sendpkt.put_data(i,destaddr[i-8])                                

        ## Change Data section ##
        for i in range(36,256): # packetfull data section's length
            sendpkt.put_data(i,49) # int 1
        for i in range(256,476):
            sendpkt.put_data(i,50) # int 2
        for i in range(476,696):
            sendpkt.put_data(i,51) # int 3
        for i in range(696,916):
            sendpkt.put_data(i,52) # int 4
        for i in range(916,1023):
            sendpkt.put_data(i,53) # int 5 
        sendpkt.put_data(1023,3) # EOT at end 


        j +=1
        # send test input. 
        if (j == ResetMoment): # Reset function check at 20th
            print("WILL SEND RESET RESET")
            sendpkt.put_data(27, 0b10000000) 
            CmdPipe.write(sendpkt.packet)
            print('accumulated number of commands : ',j)
        #elif (j == DiscoverMoment): # discover at 2nd
        #    print("\n\n\ndiscovery cmd sent\n\n\n")
        #    sendpkt.put_data(31,0x0f) # put here
        #    CmdPipe.write(sendpkt.packet);print('accumulated number of commands : ',j)
        if (j == SleepMoment): # discover at 2nd
            print("\n\n\nsleep cmd sent\n\n\n")
            sendpkt.put_data(30,0xaa) # put here
            sendpkt.put_data(31,0x01) # put here
            CmdPipe.write(sendpkt.packet);print('accumulated number of sent commands : ',j)
        elif (j == WakeMoment): # discover at 2nd
            print("\n\n\nwake cmd sent\n\n\n")
            sendpkt.put_data(30,0xaa) # put here
            sendpkt.put_data(31,0x02) # put here
            CmdPipe.write(sendpkt.packet);print('accumulated number of sent commands : ',j)
        else:                       # usually send
            CmdPipe.write(sendpkt.packet);print('accumulated number of sent commands : ',j)


        print("sleep for 3 seconds");print('j is ',j)
        time.sleep(300)


def userreader(PidQueue, DebugQueue):

    PidQueue.put(os.getpid())
    
    while True:
        # show result        
        rcvpkt = DebugQueue.get()
        datalength = int.from_bytes(rcvpkt[36:38],'big')
        print('---------------result below--------------')       
        print('[ header ] : ',end='')
        print(rcvpkt[0:56],'\n')
        print('[ body ] : ',end='')
        print((rcvpkt[56:56+datalength]))
        print('---------------result above--------------')        

##################################################################
####################### Main Process #############################
##################################################################


if __name__ == "__main__":
    setproctitle.setproctitle(PROCESS_LIST.XBEE_CNODE)   
    executable = sys.executable
    args = sys.argv[:]
    args.insert(0, sys.executable)

    # IPC (2 Queues, 2 Pipe)
    CmdQueue = Queue()                  # Receiver >>--CmdQueue-->> Executor
    MsgQueue = Queue()                  # Executor >>--MsgQueue-->> Database 
    ExecutorPipe, Sender1Pipe = Pipe()  # Executor >>--Pipe-->> Sender_1
    DatabasePipe, Sender2Pipe = Pipe()  # Database >>--Pipe-->> Sender_2
    WaitPipe, AwakePipe = Pipe()        # Sender_2 >>--Pipe-->> Executor 

    PidQueue = Queue()                  # get pid number 
    ReseterPid = Queue()                # sync_client_read === resetter
    DebugQueue = Queue()

    # 1. EV1
    # Tried to use EV1 to synchro CMD executor & CMD returner, but deadlock can happen somtimes due to context switch
    # Instead use WaitPipe, AwakePipe to synchronize which will force our WANTED sequence of flow.
    # 2. EV2 for giving sync_client_writer higher priority than async_client
    ev2 = Event();ev2.set()
    # 3. EV3 for blocking on discovery process command until discovery finish callback wakes him up
    ev3 = Event() 
    # 4. Out-Pipe is used by Sync_client and Async_client, thus there should be a mutual exclusion
    lk = Lock()

    # Instantize Process
    p1 = Process(target=sync_client_read, args=(CmdQueue, PidQueue, ReseterPid ))                             # Receives CMD from User 
    p2 = Process(target=executor, args=(CmdQueue, MsgQueue, ExecutorPipe, ev3, PidQueue, ReseterPid, WaitPipe))    # Executes CMD(Includes discovery) one by one
    p3 = Process(target=sync_client_write, args=(Sender1Pipe,  ev2, PidQueue, DebugQueue, lk, AwakePipe ))             # Sends CMD_result to User (Includes discovery netinfo)               
    p4 = Process(target=reorganizer, args=(MsgQueue, DatabasePipe, PidQueue ))                        # Re-arrange packets into a specific full packet 
    p5 = Process(target=async_client, args=(Sender2Pipe, ev2, PidQueue, DebugQueue, lk))                       # Sends other xbee's MSG to User
    
    # Start Process
    p1.start()
    p2.start()
    p3.start()
    p4.start()
    p5.start()

    ################## loopback TEST #####################  
    #userout = Process(target= userwriter, args = (PidQueue,))
    userin = Process(target= userreader, args = (PidQueue, DebugQueue))
    #userout.start()
    userin.start()
    ####################################################### 
    
    print('Every XBee process started')

    # End Process by p1's detection
    p2.join()#;print('executor joined')
    p3.join()#;print('sync-client-writer joined')
    p4.join()#;print('reorganizer joined')
    p5.join()#;print('async-client joined')
    
    #userout.join();print('userwriter joined')
    userin.join()#;print('userreader joined')

    p1.join()#;print('sync-client-READER joined') # p1, kills other processes, then kill itself at last
    
    print(" Every XBee process DIED \n NOW RESTART")

    os.execvp(executable, args)

    # this line can be only reached when all the subprocesses dies
    # then automatially goes up to while True: loop, which gives us a Reset effect
    # Cauction : Xbeeprocess.py itself won't be killed
