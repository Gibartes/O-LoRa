# O-LoRa - Bluetooth - oloraNT
__ Written by Gibartes __
---------------------------------------

This is a software that is 1:1 bluetooth server to link internal user space area. You can utilize this software for other purposes as well as this off-grid network project if you're planning to compose server or controller with bluetooth protocol.

---------------------------------------
## External description about oloraNT
* Named pipe for input to userspace	: /tmp/.olora.out
* Named pipe for output to bluetooth client	: /tmp/.olora.in
* Internal command to exit current session 	:
  * Set flag as FLAG_FIN in the olora packet header
  * It must be zero-fill that source and destination address 
* Verify integrity of the packet using MD5 digest. 
* Both <olorastd.h> and oloapkt.so offer AES256-cbc and RSA encryption/decryption using openSSL.
* Supports python3 library which is able to handle olora packets with C back-end code.
---------------------------------------
## How to build oloraNT?
1. chage directory to ./src
2. make
### If you want to install oloraNT as a service or olorapkt.so to use python, 
1. change directory to ./src
2. make
3. sudo make install
---------------------------------------
## Python Documentations
* The olora packet module allows the defined olora packet structure in C to use in python code. This is a C extenstion so that you need olorapkt.so which is offered by oloraNT and python development support package(python3-dev). The installation step is simple. If you have installed oloraNT, you don't need further steps.

### 1. Declare a packet
```python
	pkt = PACKET(PACKET_HEADER_CONFIG.PACKET_FULL,PACKET_HEADER_CONFIG.DATA_LENGTH)
```
* You can also fill data using a ctype c_uint8 array like
```python
	data = c_uint8*PACKET_HEADER_CONFIG.DATA_LENGTH
	pkt.set_packet(data(B0,B1,B2,....)
```
* Or alternatively,
```python
	buffer = obp.recv()
	if(buffer!=0 and buffer!=b''):
		pkt.packet = buffer
```

### 2. Handle the packet
* You'd better separate header and data from the packet.
```python
	pkt.split()
```
* After spliting, you can see bytes in the packet like this :
```python
	pkt.print_header()            #  print header in the packet
	pkt.print_header(256)        #  print 256 bytes at payload in the packet
```
* You can also manipulate a value in the packet. After revising the packet, you must update the packet.
```python
	prot = 0
	# set a value in the protocol field on the packet header.
	pkt.set_header(PACKET_HEADER_CONFIG.MASK_PROTO,PACKET_HEADER_CONFIG.PROT_TEXT,1)
	# update the changed contents in the packet.
	pkt.combine()
	pkt.update()
	# print current value in the protocol field from the packet header.
	prot = pkt.get_header(PACKET_HEADER_CONFIG.MASK_PROT,prot,1)
	print(prot)
```
* Sometimes, the above way is apt to messy if you need to get more than three values in the header field to process your logics. This library offers the function of header parsing. The data saves in parseinfo where defined in PACKET class, and the return type is OrderedDict. But it does not seem to be beautiful original python output concerning hex type. You can use binascii module or printHex() function to convert hex data intuitively.

```python
	pkt.parse()
	# print parsed packet with OrderedDict.
	print(pkt.parseinfo)
	print(pkt.printHex(pkt.parseinfo['DST']))
```

---------------------------------------
