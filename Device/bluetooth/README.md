# O-LoRa - Bluetooth - oloraNT
__ Written by Gibartes __
---------------------------------------

Simple bluetooth server to link internal user space area. You can utilize this software for other purposes as well as this off-grid network project if you're planning to compose server or controller with bluetooth protocol.

---------------------------------------
## External description about oloraNT
* Named pipe for input to userspace	: /tmp/.olora.out
* Named pipe for output to bluetooth client	: /tmp/.olora.in
* Internal command to exit current session 	:
  * Set flag as FLAG_FIN in the olora packet header
  * It must be zero-fill that source and destination address 
* Verify integrity of the packet using MD5 digest. 
* <olorastd.h> offers AES256-cbc and RSA encryption/decryption using openSSL.
* Supports python3 library which is able to handle olora packets with C back-end code.
---------------------------------------
## How to build oloraNT?
1. chage directory to ./src
2. make
### If you want to install oloraNT as service or olorapkt.so to use python, 
1. change directory to ./src
2. make
3. sudo make install
---------------------------------------