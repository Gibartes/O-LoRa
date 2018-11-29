#include <olorastd.h>
#include <blue.h>
#include <sess.h>

// echo test
int32_t main(int argc,char **argv){
    if(argc<2){
        printf("[*] echo testing tool\n");
	    printf("[*] usage: %s <bt-address> <bt-port>\n", argv[0]);
    return 1;}
   
    int32_t  e = 0;
    int32_t  sock     = -1;
	int32_t  hci_sock = -1;
	int32_t  status   = 0;
	uint64_t len      = 0;
	uint64_t loc_addr = 0;
	uint64_t rem_addr = 0;
    uint8_t  channel  = 1;
    char     *dest    = argv[1];
    session_t sess;
    struct sockaddr_rc remote_addr;
    struct hci_dev_info di;
	struct PACKET_CHAIN	pkt;
	struct PACKET_DATA data;
    struct PACKET_LINK_LAYER link;
    struct PACKET_NETWORK_CURRENT_CONFIG pnc;
    if(argc==3){channel = (uint8_t) atoi(argv[2]);}

    printf("[*] echo testing tool\n");

    hci_sock = createHciSocket();
    loc_addr = getPrimaryLocalBluetoothAddress(hci_sock,&di);
    close(hci_sock);
    if(loc_addr==0){
		printf("[!] no bd address\n");
		return 0;
    }
    sock = createBluetoothSocketClient(&remote_addr,dest,channel);
    if(sock<0){
		printf("[!] socket create failure [%d]\n",sock);
		return 0;
    }
    status = connect(sock,(struct sockaddr *)&remote_addr,sizeof(remote_addr));
    if(status!=0){
        printf("[!] connection failed\n");
        close(sock);
        return status;}
    
    rem_addr  = batoui64(remote_addr.rc_bdaddr);
    link.src  = loc_addr;
    link.dst  = rem_addr;
    pnc.ttl   = 30;
    pnc.param = 2;
    printf("[*] Connection success at %016llX\n",rem_addr);
    printf("STATUS : %d",status);
    
    uint64_t tst  = 0;
    uint8_t testbuffer[10] = "TESTBUFER";
    while(status>=0){
        uint8_t hash[MD5_DIGEST_LENGTH];
        uint8_t test[MD5_DIGEST_LENGTH];
                
        memset(hash,0,MD5_DIGEST_LENGTH);
	    memset(&pkt.packet,0,BUFFER_SIZE);
	    memset(data.data,0,DATA_LENGTH);
	    memcpy(data.data,testbuffer,10);
	    
	    //if(fgets(data.data,DATA_LENGTH,stdin)==NULL){
    	//    printf("[*] Read fail.");
	    //    break;}
	    setHeader(&pkt,&link,&pnc,data.data,DATA_LENGTH);
        hash_md5(hash,data.data,DATA_LENGTH);
        setPacketOffset16(&pkt,MASK_DC,hash,MD5_DIGEST_LENGTH);
        printf("HASH : ");
        hexPrint(hash,MD5_DIGEST_LENGTH);
        
	    e = bluetoothSendInst(sock,&pkt);
	    printf("__HEX__ %llu\n",tst);
	    hexPrint(&pkt.packet,128);
	    if(e<=0){
	        printf("[*] Connection Disabled. Cause : %d\n",e);
	        break;
	    }
	    memset(&pkt.packet,0,BUFFER_SIZE);
	    memset(data.data,0,DATA_LENGTH);	    
	    e = bluetoothRecvInst(sock,&pkt,&len);
	    if(e<=0){
	        printf("[*] Connection Disabled. Cause : %d\n",e);
	        break;
	    }
	    pkt2data(&pkt,&data);
	    printf("[GET]: %s",data.data);
	    printf("__HEX__ %llu\n",tst++);
	    hexPrint(&pkt.packet,128);
	    if(e<BUFFER_SIZE){read(sock,data.data,BUFFER_SIZE);} 
    }
    
}
