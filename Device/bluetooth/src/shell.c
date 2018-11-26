#include <olorastd.h>
#include <blue.h>
#include <ncurses.h>

#define DEFAULT_WIN_SIZE 30

static uint8_t *helpText = \
"\
    [ Help Menu ]\n\
    ---------------------------------------------------------\n\
    :help      | show this text message\n\
    :set help  | show the documents for network setting\n\
    :get help  | show the documents for network information\n\
    :exit      | exit this shell\n\
    :appl      | apply header to packet\n\
    :clear     | clear current screen\n\
    :conn      | connect with new device\n\
    :data      | input data\n\
    :show      | show current packet\n\
    :dcon      | disconnect with current device\n\
    :read      | read current data in this packet with chars(plain text)\n\
";

static uint8_t *helpSetText = \
"\
    1. set network environment\n\
    ---------------------------------------------------------\n\
    :set dst   | set destination (defualt : bluetooth device)\n\
                 type destination address like [abcdefgh]\n\
    :set cm    | set channel mask\n\
    :set hp    | set hp\n\
    :set id    | set id\n\
    :set flags | set manually packet flags   (default : auto)\n\
    :set ttl   | set TTL (default : 30)\n\
    :set param | set parameter in header\n\
    :set bd    | set host bluetooth address (auto disconnect)\n\
                 type address like [00:11:22:33:FF:EE]\n\
    :set port  | set host bluetooth channel     (default : 1)\n\
";

static uint8_t *helpGetText = \
"\
    2. Get current network configuration\n\
    ---------------------------------------------------------\n\
    :get dst   | get destination (defualt : bluetooth device)\n\
    :get cm    | get channel mask\n\
    :get hp    | get hp\n\
    :get id    | get id\n\
    :get flags | get manually packet flags   (default : auto)\n\
    :get ttl   | get TTL (default : 30)\n\
    :get param | get parameter in header\n\
    :get bd    | get host bluetooth address\n\
    :get port  | get host bluetooth channel     (default : 1)\n\
    \n\
";

struct OLORA_BLUETOOTH_CONNECTION_INFO{
    uint8_t       status;
    uint8_t      channel;
    uint8_t     dest[17];
    sem_t         *qlock;
    sem_t         *wlock;
    WINDOW       *window;
    WINDOW    *printArea;
    session_t      *sess;    
    struct sockaddr_rc remote_addr;
    struct THREAD_CONTROL_BOX *tcb;
};

void draw_borders(WINDOW *screen,int32_t y,int32_t x){
    int32_t i;
    for(i = 1; i < (x - 1); i++){
        mvwprintw(screen,0,i,"-");
        mvwprintw(screen,y-1,i,"-");
    }
}

void drawSplitLine(WINDOW *screen,int32_t y,int32_t x){
  int32_t i;
  for(i = 1; i < (x - 1); i++){mvwprintw(screen,y,i,"-");}
}

void hex_printw(WINDOW *win,int32_t y,int32_t x,const void *pv, size_t len) {
	if (NULL == pv) { wprintw(win,"NULL"); }
	const uint8_t *p = (const uint8_t*)pv;
	for (size_t i = 0; i < len; ++i,x+=2) { mvwprintw(win,y,x,"%02X ", *p++); }
}

/* STATUS MASKING */
static inline void set_mask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema,uint8_t mask){
	sem_wait(sema);
	tcb->mask |= mask;
	sem_post(sema);
}
static inline void set_mask_naive(struct THREAD_CONTROL_BOX *tcb,uint8_t mask){
	tcb->mask |= mask;
}

static inline uint8_t get_mask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema,uint8_t val){
	uint8_t ret;
	sem_wait(sema);
	ret = tcb->mask&val;
	sem_post(sema);
	return ret;
}

static inline uint8_t take_mask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema,uint8_t val){
	uint8_t ret;
	sem_wait(sema);
	ret = tcb->mask&val;
	tcb->mask&=(~val);
	sem_post(sema);
	return ret;
}

static void setPacketHeaderLink(struct OLORA_BLUETOOTH_CONNECTION_INFO *info,struct PACKET_LINK_LAYER *link,struct PACKET_NETWORK_CURRENT_CONFIG *pnc,uint8_t *data){
    uint8_t cls[4]  = {0,};
    uint8_t con[8]  = {0,};
    uint8_t hlw[64] = {0,};
    uint8_t *ptr;
    
    if(data==NULL){return;}
    ptr = strtok(data, " \n");
    strncpy(cls,ptr,sizeof(cls));
    if(ptr==NULL){return;}
    ptr = strtok(NULL, " \n");
    if(ptr==NULL){return;}
    strncpy(con,ptr,8);
    ptr = strtok(NULL, " \n");
    if(ptr!=NULL){
        strncpy(hlw,ptr,64);
    }

    if(!strcmp(con,"dst")){link->dst   = (uint64_t)strtol(hlw,NULL,16);}
    else if(!strcmp(con,"cm")){
        link->cm    = (uint64_t)strtol(hlw,NULL,16);}
    else if(!strcmp(con,"hp")){
        link->hp    = (uint8_t)strtol(hlw,NULL,16);}
    else if(!strcmp(con,"id")){
        link->id    = (uint16_t)strtol(hlw,NULL,16);}
    else if(!strcmp(con,"flags")){
        link->flags = (uint8_t)strtol(hlw,NULL,16);}
    else if(!strcmp(con,"ttl")){
        pnc->ttl    = (uint8_t)strtol(hlw,NULL,16);}
    else if(!strcmp(con,"param")){
        pnc->param  = (uint8_t)strtol(hlw,NULL,16);}
    else if(!strcmp(con,"port")){
        info->channel  = (uint8_t)strtol(hlw,NULL,16);}
    if(!strcmp(con,"help")){
        sem_wait(info->wlock);
        mvwprintw(info->printArea,3,1,helpSetText);
        sem_post(info->wlock);
        return;
    }
    else if(!strcmp(con,"bd")){ 
        bdaddr_t ba;
        strncpy(info->dest,hlw,17);
        str2ba(info->dest,&ba);
        close(sess->handle);
        info->sess->handle = -1;
        info->sess->hostAddr = batoui64(ba);
    }
}

static uint64_t getPacketHeaderLink(struct OLORA_BLUETOOTH_CONNECTION_INFO *info,\
            const struct PACKET_LINK_LAYER *link,const struct PACKET_NETWORK_CURRENT_CONFIG *pnc,uint8_t *data){
    uint8_t cls[4]  = {0,};
    uint8_t con[8]  = {0,};
    uint8_t *ptr;

    if(data==NULL){return 0;}    
    ptr = strtok(data, " \n");
    if(ptr==NULL){return 0;}
    strncpy(cls,ptr,sizeof(cls));
    ptr = strtok(NULL, " \n");
    if(ptr==NULL){return 0;}
    strncpy(con,ptr,8);
    if(ptr!=NULL){
        ptr = strtok(NULL, " \n");
    }

    if(!strcmp(con,"dst")){
        return link->dst;}
    else if(!strcmp(con,"cm")){
        return link->cm;}
    else if(!strcmp(con,"hp")){
        return (uint64_t)link->hp;}
    else if(!strcmp(con,"id")){
        return (uint64_t)link->id;}
    else if(!strcmp(con,"flags")){
        return (uint64_t)link->flags;}
    else if(!strcmp(con,"ttl")){
        return (uint64_t)pnc->ttl;}
    else if(!strcmp(con,"param")){
        return (uint64_t)pnc->param;}
    else if(!strcmp(con,"port")){
        return (uint64_t)info->channel;}
    else if(!strcmp(con,"bd")){
        return (uint64_t)info->sess->hostAddr;}
    else if(!strcmp(con,"help")){
        sem_wait(info->wlock);
        mvwprintw(info->printArea,3,1,helpGetText);
        sem_post(info->wlock);}
    else{
        sem_wait(info->wlock);
        mvwprintw(info->printArea,2,1,"[-] Link layer doesn't have that component : [%s]\n",con);
        mvwprintw(info->printArea,3,1,"[-] Or some components prohibited manipulation.\n");
        sem_post(info->wlock);
    }return 1;
}

static void showCurrentNetworkState(struct OLORA_BLUETOOTH_CONNECTION_INFO *info,session_t *sess,struct PACKET_LINK_LAYER *link){
	mvwprintw(info->window,5,1,"     BT-ADDR :");
	mvwprintw(info->window,5,15,"%016lX\n",sess->hostAddr);
	mvwprintw(info->window,6,1,"  BT-CHANNEL :");
	hex_printw(info->window,6,15,&info->channel,sizeof(uint8_t));
	mvwprintw(info->window,7,1," Destination :");
	mvwprintw(info->window,7,15,"%016lX\n",link->dst);
	mvwprintw(info->window,8,1,"Channel Mask :");
	mvwprintw(info->window,8,15,"%016lX\n",link->cm);
	mvwprintw(info->window,9,1,"          HP :");
	mvwprintw(info->window,9,15,"%02lX\n",link->hp);
	hex_printw(info->window,9,15,&link->hp,sizeof(uint8_t));
	mvwprintw(info->window,10,1,"          ID :");
	mvwprintw(info->window,10,15,"%04lX\n",link->id);
	mvwprintw(info->window,11,1,"       FLAGS :");
	mvwprintw(info->window,11,15,"%02lX\n",link->flags);
	mvwprintw(info->window,12,1,"         TTL :");
	mvwprintw(info->window,12,15,"%02lX\n",sess->pnc.ttl);
	mvwprintw(info->window,13,1,"       PARAM :");
	mvwprintw(info->window,13,15,"%02lX\n",sess->pnc.param);
}

void *ListenerTask(void *param){
    struct OLORA_BLUETOOTH_CONNECTION_INFO *info = (struct OLORA_BLUETOOTH_CONNECTION_INFO *)param;
    struct THREAD_CONTROL_BOX *tcb = info->tcb;
    struct PACKET_CHAIN pkt;
    int32_t  status = 0;

    while(1){
       	pthread_mutex_lock(&input);
	    pthread_cond_wait(&input_cond,&input);
	    pthread_mutex_unlock(&input);
        if(get_mask(tcb,tcb->sig,STATUS_KILL)){return NULL;}
		if(get_mask(tcb,tcb->sig,STATUS_TIMO_L)){
		    takeMask(tcb,tcb->sig,STATUS_TIMO_L);
		    continue;}
		status = recvPacket(sess,&pkt,&data,tcb->sess->method);
		if(status>0){
            sem_wait(info->wlock);
            wattron(info->printArea,A_BOLD|A_BLINK|A_STANDOUT);
            wattron(info->printArea,COLOR_PAIR(3));
            mvwprintw(info->printArea,1,1,"[Recv Message From Host]\n");
            wattroff(info->printArea,COLOR_PAIR(3));
            wattroff(info->printArea,A_BOLD|A_BLINK|A_STANDOUT);
            mvwprintw(info->printArea,2,1,"%s\n",data.data);
            wrefresh(info->printArea);
            sem_post(info->wlock);
        }else{
            set_mask(tcb,tcb->sig,STATUS_KILL);
            return NULL;
        }
    }
}

void *TalkerTask(void *param){
    struct OLORA_BLUETOOTH_CONNECTION_INFO *info = (struct OLORA_BLUETOOTH_CONNECTION_INFO *)param;
    struct THREAD_CONTROL_BOX *tcb = info->tcb;
    struct PACKET_CHAIN *pkt;
    int32_t  status = 0;
    
    while(1){
       	pthread_mutex_lock(&output);
	    pthread_cond_wait(&output_cond,&output);
	    pthread_mutex_unlock(&output);
        if(get_mask(tcb,tcb->sig,STATUS_KILL)){return NULL;}
        sem_wait(info->qlock);
	    pkt = dequeuePacket(tcb->sess->stream);
		sem_post(info->qlock);
		if(pkt==NULL){continue;}
		status = bluetoothSendInst(tcb->sess->sock,pkt);
        sem_post(tcb->sig);
        if(status < 0){
            sem_wait(info->wlock);
            mvwprintw(info->printArea,1,1,"[*] Some error during sending packet with code [%d].\n",status);
            sem_post(info->wlock);
        }else{
            set_mask(tcb,tcb->sig,STATUS_KILL);
            return NULL;
        }
    }
}

void *WatchDog(void *param){
    struct OLORA_BLUETOOTH_CONNECTION_INFO *info = (struct OLORA_BLUETOOTH_CONNECTION_INFO *)param;
    struct THREAD_CONTROL_BOX *tcb = info->tcb;
	struct pollfd pf[1];    // socket listen

	int32_t   err     = 0;
	int32_t   tout    = tcb->ticks*4; // timeout
	
    pf[0].fd = tcb->sess->sock;           /* H -> C (STATIC) */
	pf[0].events = POLLIN|POLLPRI|POLLHUP|POLLERR;
	
	while(1){
	    err = poll(pf,1,tout);
        if(err<0){
            logWrite(tcb->Log,tcb->log,"[*] [WatchDog] exit : ENO:[%d]-EC:[%d]-Polling.",errno,err);
            break;
        }else if(err==0){setMask(tcb,tcb->sig,STATUS_TIMO_L);}
        if(pf[0].revents&(POLLHUP|POLLERR)){setMask(tcb,tcb->sig,STATUS_KILL);}
        
 		pthread_mutex_lock(&input);
		pthread_cond_signal(&input_cond);
		pthread_mutex_unlock(&input);
        pthread_mutex_lock(&output);		
	    pthread_cond_signal(&output_cond);
		pthread_mutex_unlock(&output);
		if(getMask(tcb,tcb->sig,STATUS_KILL)){break;}
	}
    return NULL;
}


void *InteractiveShellTask(void *param){
    struct OLORA_BLUETOOTH_CONNECTION_INFO *info = (struct OLORA_BLUETOOTH_CONNECTION_INFO *)param;
    struct THREAD_CONTROL_BOX *tcb = info->tcb;
    struct PACKET_NETWORK_CURRENT_CONFIG pnc;   
    struct PACKET_CHAIN pkt;
    struct PACKET_DATA data;
    struct PACKET_LINK_LAYER link;
    struct PACKET_LINK_LAYER curs;
    struct sockaddr_rc remote_addr;
    
    int32_t  status = -1;
    int32_t  parent_x, parent_y, new_x, new_y;
    uint8_t  buffer[DATA_LENGTH];
    uint64_t res   = 0;

    session_t *sess = tcb->sess;
    link.src = sess->clientAddr; // constant
    sess->handle    = -1;
    sess->pnc.ttl   = 30;
    
    memset(&link,0,sizeof(struct PACKET_LINK_LAYER));
    
    while(1){

        wrefresh(info->window);
        wrefresh(info->printArea);

        getmaxyx(stdscr,new_y,new_x);
        if(new_y != parent_y || new_x != parent_x){
            parent_x = new_x;
            parent_y = new_y;
            wresize(info->window,new_y-DEFAULT_WIN_SIZE,new_x);
            wresize(info->printArea,DEFAULT_WIN_SIZE,new_x);
            mvwin(info->printArea,new_y-DEFAULT_WIN_SIZE,0);
        }
        memset(buffer,0,DATA_LENGTH);
        wattron(info->window,A_BOLD|A_BLINK);
        if(sess->sock<0){
            wattron(info->window,COLOR_PAIR(1));
            mvwprintw(info->window,0,1,"[STATUS] : Disconnected");
            mvwprintw(info->window,1,1,"[You : %016lX][BD_Host : %s]\n",sess->clientAddr,info->dest);
            wattroff(info->window,COLOR_PAIR(1));
        }else{
            wattron(info->window,COLOR_PAIR(2));
            mvwprintw(info->window,0,1,"[STATUS] : Connected with ");
        	mvwprintw(info->window,1,1,"[You : %016lX][BD_Host : %016lX]\n",sess->clientAddr,sess->hostAddr);
            wattroff(info->window,COLOR_PAIR(2));
        }
        wattroff(info->window,A_BOLD|A_BLINK);
		drawSplitLine(info->window,2,new_x);
		mvwprintw(info->window,3,1,"Current Network Info\n");
		drawSplitLine(info->window,4,new_x);
        showCurrentNetworkState(info,sess,&link);
		drawSplitLine(info->window,14,new_x);
		wrefresh(info->window);
        wmove(info->window,15,1);
        wgetstr(info->window,buffer);
        fflush(stdin);
        wclear(info->window);
        wclear(info->printArea);
        wrefresh(info->window);
        wrefresh(info->printArea);
        
        if(!strncmp(buffer,":help",5)){
            sem_wait(info->wlock);
            mvwprintw(info->printArea,3,1,helpText);
            sem_post(info->wlock);
        }
        else if(!strncmp(buffer,":clear",6)){
            wclear(info->window);
            wclear(info->printArea);
        }        
        else if(!strncmp(buffer,":exit",5)){
            set_mask(tcb,tcb->sig,STATUS_KILL);
            return NULL;
        }
        else if(!strncmp(buffer,":conn",5)){
            if(sess->sock>0){
                mvwprintw(info->printArea,2,1,"[-] Please disconnect with current sesssion.\n");
                continue;
            }
            sess->sock = createBluetoothSocketClient(&remote_addr,info->dest,info->channel);
            if(sock<0){
                mvwprintw(info->printArea,2,1,"[-] Create Socket Failure. %d\n",sess->sock);
                continue;            
            }
            status = connect(sess->sock,(struct sockaddr *)&remote_addr,sizeof(remote_addr));
            if(status!=0){
                mvwprintw(info->printArea,2,1,"[-] Connection Failed. %d\n",sess->sock);
                close(sess->sock);
                sess->sock = -1;
                continue;
             }
             sess->hostAddr = batoui64(remote_addr.rc_bdaddr);
             mvwprintw(info->printArea,2,1,"[+] Connection Success. %d\n",sess->sock);
        }
        else if(!strncmp(buffer,":dcon",5)){
            set_mask(tcb,tcb->sig,STATUS_KILL);
            close(sess->sock);
            sess->sock = -1;
        }
        else if(!strncmp(buffer,":appl",5)){
            setHeader(&pkt,&link,&pnc,data.data,strlen(data.data));
        }       
        else if(!strncmp(buffer,":data",5)){
            memset(buffer,0,DATA_LENGTH);
            wmove(info->window,15,1);
            wgetstr(info->window,buffer);
            fflush(stdin);
            memcpy(data.data,buffer,sizeof(buffer));
            data2pkt(&pkt,data.data);
            setHeader(&pkt,&link,&sess->pnc,data.data,strlen(data.data));            
            wclear(info->window);     
        }
        else if(!strncmp(buffer,":set",4)){
            setPacketHeaderLink(sess,info,&link,&sess->pnc,buffer);
        }
        else if(!strncmp(buffer,":get",4)){
            res = getPacketHeaderLink(sess,info,&link,&sess->pnc,buffer);
            sem_wait(info->wlock);
            mvwprintw(info->printArea,2,1,"[*] DEC : %019lu HEX : %016lX\n",res,res);
            sem_post(info->wlock);
        }        
        else if(!strncmp(buffer,":send",5)){
            sem_wait(info->qlock);
            enqueuePacket(sess->stream,&pkt);
            sem_post(info->qlock);
            continue;
        }
        else if(!strncmp(buffer,":show",5)){
            uint64_t seq =0;
            uint64_t len =0;
            uint8_t  orig[MD5_DIGEST_LENGTH];
        	uint8_t sd[SHA256_DIGEST_LENGTH];
            uint64_t tms = 0;
	        time_t seconds;
	        seconds = time(NULL);
	        set_packet_offset(&pkt,MASK_TMS,0,(uint32_t)seconds,4);
            get_packet_offset(&pkt,MASK_SEQ,0,&seq,4);
            get_packet_offset(&pkt,MASK_TMS,0,&tms,2);
            get_packet_offset(&pkt,MASK_LEN,0,&len,2);
            get_packet_offset16(&pkt,MASK_DC,orig,MD5_DIGEST_LENGTH);
            get_packet_offset16(&pkt,MASK_SD,sd,SHA256_DIGEST_LENGTH);
            sem_wait(info->wlock);
	        mvwprintw(info->printArea, 2,1, "%016lX\n",sess->clientAddr);
	        mvwprintw(info->printArea, 2,21,"%016lX\n",link.dst);
	        mvwprintw(info->printArea, 3,1, "%016lX\n",link.cm);
	        mvwprintw(info->printArea, 3,21,"%02lX\n", link.hp);
	        mvwprintw(info->printArea, 3,23,"%04lX\n", link.id);
	        mvwprintw(info->printArea, 3,27,"%02lX\n", link.flags);
	        mvwprintw(info->printArea, 3,29,"%08lX\n", seq);
	        mvwprintw(info->printArea, 4,1, "%08lX\n", tms);
	        mvwprintw(info->printArea, 4,9, "%04lX\n", len);
	        mvwprintw(info->printArea, 4,13,"%02lX\n", sess->pnc.ttl);
	        mvwprintw(info->printArea, 4,15,"%02lX\n", sess->pnc.param);
        	hex_printw(info->printArea,4,21,orig,sizeof(uint64_t));	        
        	hex_printw(info->printArea,5,1,orig+8,sizeof(uint64_t));
        	hex_printw(info->printArea,5,21,sd,sizeof(uint64_t));
        	hex_printw(info->printArea,6,1,sd+8,sizeof(uint64_t));
        	hex_printw(info->printArea,6,21,sd+16,sizeof(uint64_t));
        	hex_printw(info->printArea,7,1, sd+24,sizeof(uint64_t));
	        mvwprintw(info->printArea, 7,21, "LEN : %04lu Bytes\n",len);
        	for(int32_t i=0,j=8,size=0;i<new_y-DEFAULT_WIN_SIZE;j++,i+=2){
        	    hex_printw(info->printArea,j,1, data.data+(i)*sizeof(uint64_t),sizeof(uint64_t));
        	    hex_printw(info->printArea,j,21,data.data+(i+1)*sizeof(uint64_t),sizeof(uint64_t));
        	    size+=16;
        	    if(size>DATA_LENGTH){break;}
        	}sem_post(info->wlock);
        }        
        else{
            sem_wait(info->wlock);
            mvwprintw(info->printArea,3,1,helpText);
            sem_post(info->wlock);
        }
        wattron(info->printArea,COLOR_PAIR(2));
        mvwprintw(info->printArea,1,1,"[LAST LOG] ");
        wattroff(info->printArea,COLOR_PAIR(2));
        mvwprintw(info->printArea,1,11,buffer);        
    }
}

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
    printf("[*] Connection success at %016X\n",rem_addr);
    
    while(status>=0){
	    memset(&pkt.packet,0,BUFFER_SIZE);
	    memset(data.data,0,DATA_LENGTH);
	    if(fgets(data.data,DATA_LENGTH,stdin)==NULL){
    	    printf("[*] Read fail.");
	        break;}
	    setHeader(&pkt,&link,&pnc,data.data,DATA_LENGTH);
	    e = bluetoothSendInst(sock,&pkt);
	    printf("__HEX__\n");
	    hexPrint(data.data,256);
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
	    printf("__HEX__\n");	    
	    hexPrint(data.data,256);
	    if(e!=BUFFER_SIZE){read(sock,data.data,BUFFER_SIZE);} 
    }
}
