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