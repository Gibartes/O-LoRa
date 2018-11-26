#include <olorastd.h>

// Ascending order for quick sort
static inline int32_t compare(const void * a, const void * b) {
	return (*(int*)a - *(int*)b);
}
// check data is blank if it is blank, then 
int32_t checkDataBlank(uint8_t *data, uint32_t size) {
	uint8_t *temp = malloc(size * sizeof(uint8_t));
	if (temp) {
		memcpy(temp, data, size);
		qsort(temp, size, sizeof(uint8_t), compare);
		if (temp[size - 1] == 0) { free(temp); return 1; }
		free(temp);
	}return 0;
}
// print data with hex format
void hexPrint(const void *pv, size_t len) {
	if (NULL == pv) { printf("NULL"); }
	const uint8_t *p = (const uint8_t*)pv;
	for (size_t i = 0; i < len; ++i) { printf("%02X ", *p++); }
	printf("\n");
}
int32_t createNamedPipe(const char *name,const uint32_t oflags,mode_t mode){
	if(open(name,oflags)<0){mkfifo(name,mode);}
	return open(name,oflags);
}

// set message unit and set list header
void initUnit(struct PACKET_CHAIN *pkt) {
	init_list_head(&pkt->list);
}
// create message container box queue.
void copyAndInitUnit(struct PACKET_CHAIN *pkt, uint8_t *text) {
	memcpy((void *)&pkt->packet, text, sizeof(union PACKET));
	init_list_head(&pkt->list);
}
// enqueue
void enqueuePacket(struct list_head *head, struct PACKET_CHAIN *pkt) {
	pkt->flag |= PACKET_USED;
	pkt->flag -= PACKET_USED;
	list_add_tail(&pkt->list, head);
}
// dequeue
struct PACKET_CHAIN *dequeuePacket(struct list_head *head) {
	if (!list_empty(head)) {
		struct PACKET_CHAIN *temp = list_first_entry(head, struct PACKET_CHAIN, list);
		temp->flag |= PACKET_USED;
		if (!(temp->flag&PACKET_PROTECTION)) {
			list_del_init(&temp->list);
		}return temp;
	}return NULL;
}

void pkt2data(struct PACKET_CHAIN *pkt,struct PACKET_DATA *data){
	memcpy(data->data, pkt->packet.data + MASK_DATA, DATA_LENGTH);
}

void data2pkt(struct PACKET_CHAIN *pkt,struct PACKET_DATA *data){
	memcpy(pkt->packet.data + MASK_DATA, data->data, DATA_LENGTH);
}

void byteSwap(union bcursor_t *ori,union bcursor_t *new,const uint32_t subset, const uint8_t bitmode, const uint8_t cond){
    if(cond){
	    switch(bitmode){
	        case(1):
                new->u8[subset] = ori->u8[subset];
		        break;
	        case(2):
		        new->u16[subset] = __bswap_16(ori->u16[subset]);
		        break;
	        case(4):
		        new->u32[subset] = __bswap_32(ori->u32[subset]);
		        break;
	        case(8):
		        new->u64 = __bswap_64(ori->u64);
		        break;
	        default:break;
	    }
    }
}

/* Endian Consideration */
#if __BYTE_ORDER == __LITTLE_ENDIAN || __BYTE_ORDER == __LITTLE_ENDIAN__

uint64_t batoui64(bdaddr_t addr) {
	uint64_t convert = 0;
	memcpy(&convert, &addr, sizeof(bdaddr_t));
	return convert;
}
bdaddr_t uitoba64(uint64_t baddr) {
	bdaddr_t addr;
	memcpy(&addr, &baddr, sizeof(bdaddr_t));
	return addr;
}
static inline void __setPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, const uint64_t opt, const uint8_t bitmode) {
	union bcursor_t temp;
	memcpy(&temp, (uint8_t*)&(msg->packet) + offset, sizeof(union bcursor_t));
	switch(bitmode){
	    case(1):
		    temp.u8[subset] |= UINT8_MAX;
		    temp.u8[subset] &= (uint8_t)opt;
		    memcpy((uint8_t*)&(msg->packet) + offset, &temp, sizeof(uint8_t));
		    break;
	    case(2):
		    temp.u16[subset] |= UINT16_MAX;
		    temp.u16[subset] &= (uint16_t)opt;
		    temp.u16[subset] = __bswap_16(temp.u16[subset]);
		    memcpy((uint8_t*)&(msg->packet) + offset, &temp, sizeof(uint16_t));
		    break;
	    case(4):
		    temp.u32[subset] |= UINT32_MAX;
		    temp.u32[subset] &= (uint32_t)opt;
		    temp.u32[subset] = __bswap_32(temp.u32[subset]);
		    memcpy((uint8_t*)&(msg->packet) + offset, &temp, sizeof(uint32_t));
		    break;
	    case(8):
		    temp.u64 |= UINT64_MAX;
		    temp.u64 &= opt;
		    temp.u64 = __bswap_64(temp.u64);
		    memcpy((uint8_t*)&(msg->packet) + offset, &temp, sizeof(uint64_t));
		    break;
	    default:break;
	}
}

static inline void  __getPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, uint64_t *opt, const uint8_t bitmode) {
	union bcursor_t temp;
	memcpy(&temp, (uint8_t*)&(msg->packet) + offset, sizeof(union bcursor_t));
	switch(bitmode){
	    case(1):
		    *opt = temp.u8[subset] & UINT8_MAX;
		    break;
	    case(2):
		    *opt = __bswap_16(temp.u16[subset] & UINT16_MAX);
		    break;
	    case(4):
		    *opt = __bswap_32(temp.u32[subset] & UINT32_MAX);
		    break;
	    case(8):
		    *opt = __bswap_64(temp.u64&UINT64_MAX);
		    break;
	    default:break;
	}
}

void setPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, const uint64_t opt, const uint8_t bitmode){
    __setPacketOffset(msg,offset,subset,opt,bitmode);
}

void getPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, uint64_t *opt, const uint8_t bitmode){
    __getPacketOffset(msg,offset,subset,opt,bitmode);
}

void setHeaderOffset(struct PACKET_HEADER *pkt,const uint32_t offset,const uint32_t subset,const uint64_t opt,const uint8_t bitmode){
	union bcursor_t temp;
	memcpy(&temp,(uint8_t*)pkt+offset,sizeof(union bcursor_t));
	switch(bitmode){
		case(1):
			temp.u8[subset]|=UINT8_MAX;
			temp.u8[subset]&=(uint8_t)opt;
			memcpy((uint8_t*)pkt+offset,&temp,sizeof(uint8_t));
			break;
		case(2):
			temp.u16[subset]|=UINT16_MAX;
			temp.u16[subset]&=(uint16_t)opt;
			temp.u16[subset]=__bswap_16(temp.u16[subset]);
			memcpy((uint8_t*)pkt+offset,&temp,sizeof(uint16_t));
			break;
		case(4):
			temp.u32[subset]|=UINT32_MAX;
			temp.u32[subset]&=(uint32_t)opt;
			temp.u32[subset]=__bswap_32(temp.u32[subset]);
			memcpy((uint8_t*)pkt+offset,&temp,sizeof(uint32_t));
			break;
		case(8):
			temp.u64|=UINT64_MAX;
			temp.u64&=opt;
			temp.u64=__bswap_64(temp.u64);
			memcpy((uint8_t*)pkt+offset,&temp,sizeof(uint64_t));
			break;
		default:break;
	}
}
void getHeaderOffset(struct PACKET_HEADER *pkt,const uint32_t offset,const uint32_t subset,uint64_t *opt,const uint8_t bitmode){
	union bcursor_t temp;
	memcpy(&temp,(uint8_t*)pkt+offset,sizeof(union bcursor_t));
	switch(bitmode){
		case(1):
			*opt = temp.u8[subset]&UINT8_MAX;
			break;
		case(2):
			*opt = __bswap_16(temp.u16[subset]&UINT16_MAX);
			break;
		case(4):
			*opt = __bswap_32(temp.u32[subset]&UINT32_MAX);
			break;
		case(8):
			*opt = __bswap_64(temp.u64&UINT64_MAX);
			break;
		default:break;
	}
}

#else
uint64_t batoui64(bdaddr_t addr) {
	uint64_t convert = 0;
	memcpy(&convert, &addr, sizeof(bdaddr_t));
	return __bswap_64(convert);
}
bdaddr_t uitoba64(uint64_t baddr) {
	bdaddr_t addr;
    baddr = __bswap_64(baddr);
	memcpy(&addr, &baddr, sizeof(bdaddr_t));
	return addr;
}

static inline void __setPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, const uint64_t opt, const uint8_t bitmode) {
	union bcursor_t temp;
	memcpy(&temp, (uint8_t*)&(msg->packet) + offset, sizeof(union bcursor_t));
	switch(bitmode){
	    case(1):
		    temp.u8[subset] |= UINT8_MAX;
		    temp.u8[subset] &= (uint8_t)opt;
		    memcpy((uint8_t*)&(msg->packet) + offset, &temp, sizeof(uint8_t));
		    break;
	    case(2):
		    temp.u16[subset] |= UINT16_MAX;
		    temp.u16[subset] &= (uint16_t)opt;
		    memcpy((uint8_t*)&(msg->packet) + offset, &temp, sizeof(uint16_t));
		    break;
	    case(4):
		    temp.u32[subset] |= UINT32_MAX;
		    temp.u32[subset] &= (uint32_t)opt;
		    memcpy((uint8_t*)&(msg->packet) + offset, &temp, sizeof(uint32_t));
		    break;
	    case(8):
		    temp.u64 |= UINT64_MAX;
		    temp.u64 &= opt;
		    memcpy((uint8_t*)&(msg->packet) + offset, &temp, sizeof(uint64_t));
		    break;
	    default:break;
	}
}
static inline void __getPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, uint64_t *opt, const uint8_t bitmode) {
	union bcursor_t temp;
	memcpy(&temp, (uint8_t*)&(msg->packet) + offset, sizeof(union bcursor_t));
	switch(bitmode){
	    case(1):
		    *opt = temp.u8[subset] & UINT8_MAX;
		    break;
	    case(2):
		    *opt = temp.u16[subset] & UINT16_MAX;
		    break;
	    case(4):
		    *opt = temp.u32[subset] & UINT32_MAX;
		    break;
	    case(8):
		    *opt = temp.u64&UINT64_MAX;
		    break;
	    default:break;
	}
}

void setPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, const uint64_t opt, const uint8_t bitmode){
    __setPacketOffset(msg,offset,subset,opt,bitmode);
}

void getPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, uint64_t *opt, const uint8_t bitmode){
    __getPacketOffset(msg,offset,subset,opt,bitmode);
}

void setHeaderOffset(struct PACKET_HEADER *pkt,const uint32_t offset,const uint32_t subset,const uint64_t opt,const uint8_t bitmode){
	union bcursor_t temp;
	memcpy(&temp,(uint8_t*)pkt+offset,sizeof(union bcursor_t));
	switch(bitmode){
		case(1):
			temp.u8[subset]|=UINT8_MAX;
			temp.u8[subset]&=(uint8_t)opt;
			memcpy((uint8_t*)pkt+offset,&temp,sizeof(uint8_t));
			break;
		case(2):
			temp.u16[subset]|=UINT16_MAX;
			temp.u16[subset]&=(uint16_t)opt;
			memcpy((uint8_t*)pkt+offset,&temp,sizeof(uint16_t));
			break;
		case(4):
			temp.u32[subset]|=UINT32_MAX;
			temp.u32[subset]&=(uint32_t)opt;
			memcpy((uint8_t*)pkt+offset,&temp,sizeof(uint32_t));
			break;
		case(8):
			temp.u64|=UINT64_MAX;
			temp.u64&=opt;
			memcpy((uint8_t*)pkt+offset,&temp,sizeof(uint64_t));
			break;
		default:break;
	}
}
void getHeaderOffset(struct PACKET_HEADER *pkt,const uint32_t offset,const uint32_t subset,uint64_t *opt,const uint8_t bitmode){
	union bcursor_t temp;
	memcpy(&temp,(uint8_t*)pkt+offset,sizeof(union bcursor_t));
	switch(bitmode){
		case(1):
			*opt = temp.u8[subset]&UINT8_MAX;
			break;
		case(2):
			*opt = temp.u16[subset]&UINT16_MAX;
			break;
		case(4):
			*opt = temp.u32[subset]&UINT32_MAX;
			break;
		case(8):
			*opt = temp.u64&UINT64_MAX;
			break;
		default:break;
	}
}
#endif

void setPacketOffset16(struct PACKET_CHAIN *msg, const uint32_t offset, uint8_t *hash, const uint32_t bitmode) {
	if((bitmode%MD5_DIGEST_LENGTH)){return;}
	struct bcursor_T temp[bitmode>>3];
	memcpy(temp,hash,bitmode);
	for (int32_t i = bitmode >> 3; --i >= 0;) {
		__setPacketOffset(msg, offset + 8 * i, 0, temp[i].bytes.u64, 8);
	}
}

void setHeaderOffset16(struct PACKET_HEADER *msg, const uint32_t offset, uint8_t *hash, const uint32_t bitmode) {
	if((bitmode%MD5_DIGEST_LENGTH)){return;}
	struct bcursor_T temp[bitmode>>3];
	memcpy(temp,hash,bitmode);
	for (int32_t i = bitmode >> 3; --i >= 0;) {
		setHeaderOffset(msg, offset + 8 * i, 0, temp[i].bytes.u64, 8);
	}
}

void getPacketOffset16(struct PACKET_CHAIN *msg, const uint32_t offset, uint8_t *hash, const uint32_t bitmode) {
	if((bitmode%MD5_DIGEST_LENGTH)){return;}
	union bcursor_t temp;
	temp.u64 = 0;
	memset(hash,0,bitmode);
	for (int32_t i = bitmode >> 3; --i >= 0;) {
		__getPacketOffset(msg, offset + 8 * i, 0, &temp.u64, 8);
		memcpy(hash+8*i, &temp,8);
	}
}

void getHeaderOffset16(struct PACKET_HEADER *msg, const uint32_t offset, uint8_t *hash, const uint32_t bitmode) {
	if((bitmode%MD5_DIGEST_LENGTH)){return;}
	union bcursor_t temp;
	temp.u64 = 0;
	memset(hash,0,bitmode);
	for (int32_t i = bitmode >> 3; --i >= 0;) {
		getHeaderOffset(msg, offset + 8 * i, 0, &temp.u64, 8);
		memcpy(hash+8*i, &temp,8);
	}
}

void maskPacketOffset(struct PACKET_CHAIN *pkt, const uint32_t offset, const uint32_t subset, const uint64_t opt, const uint8_t bitmode) {
	uint64_t temp = 0;
	__getPacketOffset(pkt, offset, subset, &temp, bitmode);
	__setPacketOffset(pkt, offset, subset, opt | temp, bitmode);
}

void unmaskPacketOffset(struct PACKET_CHAIN *pkt, const uint32_t offset, const uint32_t subset, const uint64_t opt, const uint8_t bitmode) {
	uint64_t temp = 0;
	__getPacketOffset(pkt, offset, subset, &temp, bitmode);
	__setPacketOffset(pkt, offset, subset, opt&(~temp), bitmode);
}

static inline void __setHeaderLinkLayer(struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link) {
	__setPacketOffset(pkt,MASK_SRC,  0,link->src,8);
	__setPacketOffset(pkt,MASK_DST,  0,link->dst,8);
	__setPacketOffset(pkt,MASK_CM,   0,link->cm,8);
	__setPacketOffset(pkt,MASK_HP,   0,link->hp,1);
	__setPacketOffset(pkt,MASK_PROTO,0,link->proto,1);	
	__setPacketOffset(pkt,MASK_ID,   0,link->id,2);
	__setPacketOffset(pkt,MASK_FLAGS,0,link->flags,1);
	__setPacketOffset(pkt,MASK_FRAG, 0,link->frag,1);	
	__setPacketOffset(pkt,MASK_SEQ,  0,link->seq,4);
}

static inline void __setHeaderDataLayer(struct PACKET_CHAIN *pkt, uint8_t *data, size_t length) {
    uint8_t hash[MD5_DIGEST_LENGTH] = {0,};
    hash_md5(hash,data,length);
	__setPacketOffset(pkt,MASK_LEN,0,length,2);                   // write data length
    setPacketOffset16(pkt,MASK_DC,hash,MD5_DIGEST_LENGTH);        // write data hash in the header
	memcpy((uint8_t*)&(pkt->packet)+MASK_DATA,data,DATA_LENGTH);    // copy data to the packet
}

int32_t compareMD5(struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,const uint64_t len){
    uint8_t hash[MD5_DIGEST_LENGTH];
    uint8_t temp[MD5_DIGEST_LENGTH];
    memset(hash,0,MD5_DIGEST_LENGTH);
    memset(temp,0,MD5_DIGEST_LENGTH);
    getPacketOffset16(pkt,MASK_DC,temp,MD5_DIGEST_LENGTH);
    hash_md5(hash,data->data,len);  
    return strncmp((char*)temp,(char*)hash,MD5_DIGEST_LENGTH)==0?1:0;
}

int32_t echoLink(uint64_t host, struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link) {
	uint64_t client = 0;
	__getPacketOffset(pkt, MASK_DST, 0, &client, 8);
	if (client != host) { return ERR_FALSE_ADDR; }
	__getPacketOffset(pkt, MASK_SRC, 0, &client, 8); // get client address    
	link->src = host;
	link->dst = client;
	return SUCCESS;
}

void setHeaderLinkLayer(struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link) {
	if (link!=NULL) {__setHeaderLinkLayer(pkt, link);}
}

void setHeaderDataLayer(struct PACKET_CHAIN *pkt, uint8_t *data, size_t length) {
	if(data!=NULL||length!=0){__setHeaderDataLayer(pkt, data, length);}
	else if(data!=NULL||length==0){__setPacketOffset(pkt,MASK_LEN,0,0,2);}	
	else{__setPacketOffset(pkt,MASK_LEN,0,0xFFFFFFFF,2);}   // Remarking Error
}

void setHeader(struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link, struct PACKET_NETWORK_CURRENT_CONFIG *pnc, uint8_t *data, size_t length) {
	__setHeaderLinkLayer(pkt, link);
	__setHeaderDataLayer(pkt, data, length);	
	__setPacketOffset(pkt,MASK_TTL,0,pnc->ttl,2);
	__setPacketOffset(pkt,MASK_PARAM,0,pnc->param,2);	
}

void setReportPacket(uint64_t host,uint64_t client,struct PACKET_CHAIN *pkt,uint8_t *data,size_t length){
	__setPacketOffset(pkt,MASK_SRC,  0,host,8);
	__setPacketOffset(pkt,MASK_DST,  0,client,8);
	__setPacketOffset(pkt,MASK_CM,   0,0xFFFFFFFF,8);
	__setPacketOffset(pkt,MASK_HP,   0,0xFF,1);
	__setPacketOffset(pkt,MASK_PROTO,0,0x01,1);	
	__setPacketOffset(pkt,MASK_ID,   0,0xFFFF,2);
	__setPacketOffset(pkt,MASK_FLAGS,0,FLAG_ERROR|FLAG_FIN|FLAG_URGENT,1);
	__setPacketOffset(pkt,MASK_FRAG, 0,0,1);	
	__setPacketOffset(pkt,MASK_SEQ,  0,0,2);
	__setPacketOffset(pkt,MASK_LEN,  0,length,2);	
	__setPacketOffset(pkt,MASK_TTL,  0,0,1);
	__setHeaderDataLayer(pkt,data,length);
}
/*
#if defined(__x86_64__) || defined(__amd64__)
void getHeaderLinkLayer(struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link) {
	__getPacketOffset(pkt,MASK_SRC,  0,&(link->src),8);
	__getPacketOffset(pkt,MASK_DST,  0,&(link->dst),8);
	__getPacketOffset(pkt,MASK_CM,   0,&(link->cm),8);
    printf("inside get link...\n");
	__getPacketOffset(pkt,MASK_HP,   0,(uint64_t *)((uint64_t)link->hp),1);
	__getPacketOffset(pkt,MASK_PROTO,0,(uint64_t *)((uint64_t)link->proto),1);		
	__getPacketOffset(pkt,MASK_ID,   0,(uint64_t *)((uint64_t)link->id),2);
	__getPacketOffset(pkt,MASK_FLAGS,0,(uint64_t *)((uint64_t)link->flags),1);
	__getPacketOffset(pkt,MASK_FRAG, 0,(uint64_t *)((uint64_t)link->frag),1);	
	__getPacketOffset(pkt,MASK_SEQ,  0,(uint64_t *)((uint64_t)link->seq),4);
    printf("ended link...\n");
}
#else
*/
void getHeaderLinkLayer(struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link) {
    uint64_t hp     = 0;
    uint64_t proto  = 0;
    uint64_t id     = 0;
    uint64_t flags  = 0;
    uint64_t frag   = 0;
    uint64_t seq    = 0;
	__getPacketOffset(pkt,MASK_SRC,  0,&(link->src),8);
	__getPacketOffset(pkt,MASK_DST,  0,&(link->dst),8);
	__getPacketOffset(pkt,MASK_CM,   0,&(link->cm),8);
	__getPacketOffset(pkt,MASK_HP,   0,&hp,1);
	__getPacketOffset(pkt,MASK_PROTO,0,&proto,1);	
	__getPacketOffset(pkt,MASK_ID,   0,&id,2);
	__getPacketOffset(pkt,MASK_FLAGS,0,&flags,1);
	__getPacketOffset(pkt,MASK_FRAG, 0,&frag,1);	
	__getPacketOffset(pkt,MASK_SEQ,  0,&seq,2);
	link->hp    = (uint8_t)hp;
	link->proto = (uint8_t)proto;	
	link->id    = (uint16_t)id;
	link->flags = (uint8_t)flags;
	link->frag  = (uint8_t)frag;	
	link->seq   = (uint16_t)seq;
}
//#endif

/* For python Code (Backend code) */
void initPacketHeader(struct PACKET_HEADER *pkt){
	memset(pkt,0,sizeof(struct PACKET_HEADER));
}
void str2pkth(struct PACKET_HEADER *pkt,void *buf){
	memset(pkt,0,sizeof(struct PACKET_HEADER));
	memcpy((uint8_t*)pkt,buf,sizeof(struct PACKET_HEADER));
}
void str2pktb(void *pkt,void *buf,uint32_t opt){
	if(opt){
		memset(buf,0,DATA_LENGTH);
		memcpy(buf,(uint8_t*)pkt+sizeof(struct PACKET_HEADER),DATA_LENGTH);
		return;
	}
	else{
	    memset(buf,0,XBEE_DATA_LEN);
	    memcpy(buf,(uint8_t*)pkt+sizeof(struct PACKET_HEADER),XBEE_DATA_LEN);
	}
}
void pkth2str(struct PACKET_HEADER *pkt,void *str){
	memcpy(str,(uint8_t*)pkt,sizeof(struct PACKET_HEADER));
}
void pktCombine(void *pkt,void *head,void *body,uint32_t opt){
	memcpy((uint8_t*)pkt,head,sizeof(struct PACKET_HEADER));
	if(opt){memcpy((uint8_t*)pkt+sizeof(struct PACKET_HEADER),body,DATA_LENGTH);}
	else{memcpy((uint8_t*)pkt+sizeof(struct PACKET_HEADER),body,XBEE_DATA_LEN);}
}
void initArea(void *pkt,uint32_t size){memset(pkt,0,size);}

