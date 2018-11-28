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

## O-LoRa - Bluetooth - oloraNT
__ Written by Gibartes __
---------------------------------------

This is a software that is 1:1 bluetooth server to link internal user space area. You can utilize this software for other purposes as well as this off-grid network project if you're planning to compose server or controller with bluetooth protocol.

---------------------------------------
### External description about oloraNT
* Named pipe for input to userspace	: /tmp/.olora.out
* Named pipe for output to bluetooth client	: /tmp/.olora.in
* Internal command to exit current session 	:
  * Set flag as FLAG_FIN in the olora packet header
  * It must be zero-fill that source and destination address 
* Verify integrity of the packet using MD5 digest. 
* Both <olorastd.h> and oloapkt.so offer AES256-cbc and RSA encryption/decryption using openSSL.
* Supports python3 library which is able to handle olora packets with C back-end code.
---------------------------------------
### How to build oloraNT?
1. chage directory to ./src
2. make
#### If you want to install oloraNT as a service or olorapkt.so to use python, 
1. change directory to ./src
2. make
3. sudo make install
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

- Components :


---------------------------------------
