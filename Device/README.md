# O-LoRa - Device
*********************************************************************************************************
O-LoRa device controller project collaborating with Gibartes and MinJae
*********************************************************************************************************

- Components :
  - oloraNT
    - This software is bluetooth server to link internal user space area. 
  - oloraGT
    - This python code plays a role in safely connecting between bluetooth(oloraNT) and xbee(oloraXB).
  - oloraXB
    - This python code controlls xbee module.

*********************************************************************************************************
- Dependency
  - oloraNT
    - blueZ
    - OpenSSL
    - libpam
  - oloraXB
    - oloraNT
    - digi-xbee

*********************************************************************************************************
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
