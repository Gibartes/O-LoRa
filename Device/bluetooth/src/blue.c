#include <blue.h>

#define SERVICE_RESET			0xFF00
#define SERVICE_ENABLE_SCAN 	0xFF01
#define SERVICE_DISABLE_SCAN 	0xFF02

static struct{
	uint32_t cmd;
	int32_t (*func)(int32_t ctl,int32_t hdev,uint32_t opt);
	uint32_t opt;
}internal_command[]={
	{SERVICE_RESET,		   resetBluetooth,	0},
	{SERVICE_ENABLE_SCAN,  enableScan,		0},
	{SERVICE_DISABLE_SCAN, enableScan,		0},
};

int32_t executeCommand(int32_t ctl,struct hci_dev_info *di,uint32_t cmd,uint32_t opt){
	cmd -= SERVICE_RESET;
	return internal_command[cmd].func(ctl,di->dev_id,opt);
}

static inline int32_t bluetooth_create_socket(struct sockaddr_rc *addr,uint8_t channel){
	addr->rc_family = AF_BLUETOOTH;
	addr->rc_channel= htobs(channel);
	addr->rc_bdaddr	= *BDADDR_ANY;
	return socket(AF_BLUETOOTH,SOCK_STREAM,BTPROTO_RFCOMM);
}

int32_t dynamicBind(uint32_t socket,struct sockaddr_rc *addr){
	int32_t err;
	for(uint8_t port=1;port<30;port++){
		addr->rc_channel = htobs(port);
		err = bind(socket,(struct sockaddr *)addr,sizeof(addr));
		if(!err||errno==EINVAL){return 0;}
	}return EINVAL;
}

int32_t createBluetoothSocket(uint8_t channel){
	int32_t listenfd;
	int32_t err = 0 ;
	struct sockaddr_rc host_addr = {0};
	listenfd = bluetooth_create_socket(&host_addr,channel);
	if(listenfd<0){return EINVAL;}
	if(channel){err = dynamicBind(listenfd,&host_addr);}
	if(err==EINVAL){return EINVAL;}
	listen(listenfd,1);	
	return listenfd;
}

int32_t createBluetoothSocketClient(struct sockaddr_rc *remote_addr,char *addr,uint8_t channel){
	remote_addr->rc_family  = AF_BLUETOOTH;
	remote_addr->rc_channel = channel;
	str2ba(addr,&remote_addr->rc_bdaddr);
	return socket(AF_BLUETOOTH,SOCK_STREAM,BTPROTO_RFCOMM);
}

int32_t acceptBluetoothSocket(int32_t listenfd, bdaddr_t *src){
	int32_t clientfd;
	struct sockaddr_rc remote_addr 	= {0};
	socklen_t opt = sizeof(struct sockaddr_rc);
	clientfd = accept(listenfd,(struct sockaddr *)&remote_addr,&opt);
	if(clientfd<0){return -EINVAL;}
	*src = remote_addr.rc_bdaddr;
	return clientfd;
}

/* HCI */
int32_t createHciSocket(void){
	return socket(AF_BLUETOOTH,SOCK_RAW,BTPROTO_HCI);
}

uint64_t getPrimaryLocalBluetoothAddress(int32_t ctl,struct hci_dev_info *di){
	int32_t  i;
	uint64_t loc_addr = 0;
	struct hci_dev_list_req *dl;
	struct hci_dev_req *dr;
	if(!(dl=calloc(0,HCI_MAX_DEV*sizeof(struct hci_dev_req)+sizeof(uint16_t)))){return 0;}
	dl->dev_num = HCI_MAX_DEV;
	dr = dl->dev_req;
	if(ioctl(ctl,HCIGETDEVLIST,(void *)dl)<0){return 0;}
	for(i=0;i<dl->dev_num;i++){
		di->dev_id = (dr+i)->dev_id;
		if(ioctl(ctl,HCIGETDEVINFO,(void *)di)< 0){continue;}
		if(hci_test_bit(HCI_RAW,&di->flags)&&!bacmp(&di->bdaddr,BDADDR_ANY)){
			int32_t dd = hci_open_dev(di->dev_id);
			hci_read_bd_addr(dd,&di->bdaddr,1000);
			hci_close_dev(dd);
			break;
		}
	}
	loc_addr = batoui64(di->bdaddr);
	free(dl);
	return loc_addr;
}

int32_t enableScan(int32_t ctl,int32_t hdev,uint32_t opt){
	struct hci_dev_req dr;
	dr.dev_id  = hdev;
	dr.dev_opt = SCAN_DISABLED;									// No scan
	if(opt==ISCAN){dr.dev_opt = SCAN_INQUIRY;}					// iscan
	else if(opt==PSCAN) {dr.dev_opt = SCAN_PAGE;}				// pscan
	else if(opt==PISCAN){dr.dev_opt = SCAN_PAGE|SCAN_INQUIRY;}	// piscan
	if(ioctl(ctl,HCISETSCAN,(unsigned long)&dr)<0){return errno;}
	return 0;
}

/*
 * Reset local bluetooth device.
*/

int32_t resetBluetooth(int32_t ctl,int32_t hdev,uint32_t opt){
	if(ioctl(ctl,HCIDEVUP,hdev)<0){
		if (errno == EALREADY){return 0;}
		return -1;}
	if(ioctl(ctl,HCIDEVDOWN,hdev)<0){
		return -1;
	}return 0;
}

int32_t upstartBluetooth(int32_t ctl,int32_t hdev,uint32_t opt){
	if(ioctl(ctl,HCIDEVUP,hdev)<0){
		if (errno == EALREADY)
			return 0;
		return 1;}
	return 0;	
}


/* SEND & RECEIVE */

int32_t bluetoothRecv(int32_t fd,struct list_head *head,struct PACKET_CHAIN *pkt,void *buffer,uint64_t *dlen){	
	uint64_t len = 0;
	int32_t rcnt = read(fd,buffer,MAX_BUFFER_SIZE);
	if(rcnt>0){
	    getPacketOffset(pkt,MASK_LEN,0,&len,2);
	    if(len<=DATA_LENGTH){
		    enqueuePacket(head,pkt);
		    return rcnt;}
    }return ERR_SOCKET_IO_FAIL;
}

int32_t bluetoothRecvInst(int32_t fd,struct PACKET_CHAIN *pkt,uint64_t *dlen){
	int32_t rcnt = read(fd,&pkt->packet,MAX_BUFFER_SIZE);
	if(rcnt>0){
	    getPacketOffset(pkt,MASK_LEN,0,dlen,2);
	    if(*dlen<=DATA_LENGTH){return rcnt;}
    }return ERR_SOCKET_IO_FAIL;
}

/* TIMESTAMP */
int32_t bluetoothSend(int32_t fd,struct list_head *head,struct PACKET_CHAIN *pkt){
	time_t seconds;
	seconds = time(NULL);
	setPacketOffset(pkt,MASK_TMS,0,(uint32_t)seconds,4);
    pkt = dequeuePacket(head);
    if(pkt!=NULL){
	    return write(fd,&(pkt->packet),MAX_BUFFER_SIZE);}
    return 0;
}

int32_t bluetoothSendInst(int32_t fd,struct PACKET_CHAIN *pkt){
	time_t seconds;
	seconds = time(NULL);
	setPacketOffset(pkt,MASK_TMS,0,seconds,4);
	return write(fd,&(pkt->packet),MAX_BUFFER_SIZE);
}
