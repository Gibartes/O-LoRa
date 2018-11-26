#include <olorastd.h>



int32_t  executeCommand(int32_t ctl,struct hci_dev_info *di,uint32_t cmd,uint32_t opt);
int32_t  dynamicBind(uint32_t socket,struct sockaddr_rc *addr);
int32_t  createBluetoothSocket(uint8_t channel);
int32_t  createBluetoothSocketClient(struct sockaddr_rc *remote_addr,char *addr,uint8_t channel);
int32_t  acceptBluetoothSocket(int32_t listenfd, bdaddr_t *src);

/* With Backlog queue */
int32_t  bluetoothRecv(int32_t fd,struct list_head *head,struct PACKET_CHAIN *pkt,void *buffer,uint64_t *dlen);
int32_t  bluetoothSend(int32_t fd,struct list_head *head,struct PACKET_CHAIN *pkt);

/* Instant */
int32_t  bluetoothRecvInst(int32_t fd,struct PACKET_CHAIN *pkt,uint64_t *dlen);
int32_t  bluetoothSendInst(int32_t fd,struct PACKET_CHAIN *pkt);

int32_t  createHciSocket(void);
uint64_t getPrimaryLocalBluetoothAddress(int32_t ctl,struct hci_dev_info *di);
int32_t  enableScan(int32_t ctl,int32_t hdev,uint32_t opt);
int32_t  resetBluetooth(int32_t ctl,int32_t hdev,uint32_t opt);
int32_t  upstartBluetooth(int32_t ctl,int32_t hdev,uint32_t opt);
