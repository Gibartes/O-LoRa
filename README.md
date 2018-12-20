# O-LoRa

---------------------------------------

## O-LoRa - Device

---------------------------------------

O-LoRa device controller project collaborating with Gibartes and MinJae

---------------------------------------

- Components :
  - oloraNT
    - This software is bluetooth server to link internal user space area. 
  - oloraGT
    - This python code plays a role in safely connecting between bluetooth(oloraNT) and xbee(oloraXB).
  - oloraXB
    - This python code controlls xbee module.

---------------------------------------

- Dependency
  - oloraNT
    - blueZ
    - OpenSSL
    - libpam
  - oloraXB
    - oloraNT
    - digi-xbee

---------------------------------------

- Usage for start.sh
  - First, you must change permissions of start.sh. Authorize the permissions as follows :
    - chmod +x ./start.sh
  - Build oloraNT.  (Required at the first time)
    - ./start.sh make
  - Install oloraNT as a service and olora library.  (Required at the first time)
    - sudo ./start.sh make install
  - Install oloraGT and oloraXB as services.
    - sudo ./start.sh make cu install
  - Kill all the current olora processes.
    - ./start.sh kill
  - Run all olora processes.
    - ./start.sh run
  - See a log file written by oloraNT.
    - ./start.sh log

## O-LoRa - Bluetooth - oloraNT
__ Written by Gibartes __
---------------------------------------

This is a software that is 1:1 bluetooth server to link internal user space area. You can utilize this software for other purposes as well as this off-grid network project if you're planning to compose server or controller with bluetooth protocol.

---------------------------------------
### External features about oloraNT
* Named pipe for input to userspace	: /tmp/.olora.out
* Named pipe for output to bluetooth client	: /tmp/.olora.in
* Internal command to exit current session 	:
  * Set flag as FLAG_FIN in the olora packet header.
  * Source address must be zero-fill and destination address must be bluetooth address of this host.
  * Otherwise, the packet is dropped by the security rule.
* Verify integrity of the packet using MD5 digest. 
* The SRC field on the olora packet have to be setted as a bluetooth address of the client which is connected with oloraNT.
* Both <olorastd.h> and oloapkt.so offer AES256-cbc and RSA encryption/decryption using openSSL.
* Supports python3 library which is able to handle olora packets with C back-end code.
* The path of log file is /var/log/olora.service.log which is recorded events concerning the status of connection or errors.
---------------------------------------
### How to build oloraNT?
1. chage directory to ./src
2. make
#### If you want to install oloraNT as a service or olorapkt.so to use python, 
1. change directory to ./src
2. make
3. sudo make install
---------------------------------------
### Python Documentations
* The olora packet module allows the defined olora packet structure in C to use in python code. This is a C extenstion so that you need olorapkt.so which is offered by oloraNT and python development support package(python3-dev). The installation step is simple. If you have installed oloraNT, you don't need further steps. Type "pip install pyolora" on your terminal.

#### 1. Declare a packet
```python
	from olora.packet import *
	from olora.define import *

	pkt = PACKET(PACKET_HEADER_CONFIG.PACKET_FULL,PACKET_HEADER_CONFIG.DATA_LENGTH)
```
* You can also fill data using a ctype c_uint8 array like
```python
	data = c_uint8*PACKET_HEADER_CONFIG.DATA_LENGTH
	# case I
	pkt.set_packet(data(B0,B1,B2,....))
	# case 2
	j = PACKET_HEADER_CONFIG.MASK_DATA
	for i in buffer:
		pkt.put_data(j,i)
		j+=1
	# case 3
	pkt.move_wcursor(PACKET_HEADER_CONFIG.MASK_DATA)
	for i in buffer:
		pkt.put_seq_data(i)
```
* I'd recommend the third case to fill data. Or alternatively,
```python
	buffer = obp.recv()
	if(buffer!=0 and buffer!=b''):
		pkt.packet = buffer
```
* But in this case, some features cannot be used because of type problem.

#### 2. Handle the packet
* You'd better separate header and data from the packet.
```python
	pkt.split()
```
* After spliting, you can see bytes in the packet like this :
```python
	pkt.print_header()            #  print header in the packet
	pkt.print_payload(256)        #  print 256 bytes at payload in the packet
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
* Olora system uses MD5 Hash for verifying integrity of the packet. You can make a MD5 digest and put in the header with PACKET class.

```python
	pkt.cal_hash(PACKET_HEADER_CONFIG.DATA_LENGTH)
	print("HASH-VALUE : ",pkt.hash)
	pkt.set_hash()
	pkt.combine()
	pkt.update()
	print("HASH-VALUE in the header : ",pkt.get_hash())
```

* PyOlora also offers UNIX pipe object with ObjectPipe Class for handling pipe easily because oloraNT uses two pipes to communicate with userspace system. 
  1. Declare an ObjectPipe object with path of the pipe. 
  2. Make a pipe files if have not been created by others.
  3. Open the declared pipe with mode like os.O_WRONLY or os.O_RDONLY, and so on. But you need to think about the fact that code hangs untill other process catch your pipe.

```python
	readPipe = ObjectPipe(PIPE_LIST.NT_IN)
	readPipe.mkfifo()
	readPipe.open(os.ORDONLY)
```

* After setting up the pipe, you can get the descriptor number as pipe.pipe which can be applied to select(). pipe.recv() delivers data from the pipe and, pipe.write(buffer) writes data in the buffer to the pipe.

```python
	# Receive data from the readable pipe.
	buffer = readPipe.recv()
	if(buffer!=0 and buffer!=b''):
		pkt.packet = buffer
	# Write data to the writable pipe.
	writeCNT = writePipe.write(buffer)
	if(writeCNT<=0):
		# handling error
```

#### 3. Sqlite helper class
* PyOlora has universial sqlite helper class to handle some database. To use sqlite helper, you have to import a package in pyolora as follows :

```python
	from olora.database import *
	TableName = SomeDataBaseTableName
	db = DataBaseQuerry(TableName)
```

* Methods defined in DataBaseQuerry class 
  1. connect(path)
    * Connect a database file in designated path, and create a database handle. This job has to be executed at the first time.
  2. close()
    * Close the database handle.
  3. build(table)
    * Build a database table. It is required that a list of columns which consist of the table.
  4. delete(primaryKey)
    * Delete a item with primary key in the database. If this method success, it returns True. Otherwise it returns False.
  5. insert(row)
    * Insert one item to the database. It cannot be overlapped a primary key in the row in the same database.
  6. read(primaryKey)
    * Return a row in the database which contented with the primary key. If this method success, it returns that row. Otherwise it returns False.
  7. readByCol(primaryKey,column)
    * Return a row in the database which satisfied with the specific column on the primary key. If this method success, it returns that row. Otherwise it returns False.
  8. modify(primaryKey,column,value)
    * Update a row which gratified with the primary key in the database. If this method success, it returns True. Otherwise it returns False.
  9. getTop()
    * Get the first row in the current connected database. If this method success, it returns that row. Otherwise it returns False.

---------------------------------------

## O-LoRa - Gate - oloraGT
__ Written by Gibartes __
---------------------------------------

 This python code plays a role in safely connecting between bluetooth(oloraNT) and xbee(oloraXB).

---------------------------------------
- I/O List
  - oloraNT
    - oloraNT -> Gate : "/tmp/.olora.out"
    - oloraNT <- Gate : "/tmp/.olora.in"
  - oloraXB
    - oloraXB -> Gate : "/tmp/.xbolora.out"
    - oloraXB <- Gate : "/tmp/.xbolora.in"

- The Flows of packets
  - 1st 	: oloraNT -> Gate -> oloraXB
  - 2nd 	: oloraNT <- Gate <- oloraXB
---------------------------------------

## O-LoRa - Xbee - oloraXB
__ Written by MinJae __
---------------------------------------

 This python code controlls xbee module.

---------------------------------------
- Components :
---------------------------------------

## O-LoRa - Android
---------------------------------------

O-LoRa Android application collaborating with Myeongeun and MinSu

---------------------------------------

- You can download lastest olora android application in google [play store](https://play.google.com/store/apps/details?id=com.team_olora.olora_beta).


---------------------------------------
