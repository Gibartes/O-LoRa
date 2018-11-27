# O-LoRa - Device
*********************************************************************************************************
O-LoRa device controller project collaborate with Gibartes and MinJae
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