#include <sess.h>

static inline void __pkt2data(struct PACKET_CHAIN *pkt,struct PACKET_DATA *data){
	memcpy(data->data, pkt->packet.data + MASK_DATA, DATA_LENGTH);
}

static inline void __data2pkt(struct PACKET_CHAIN *pkt,struct PACKET_DATA *data){
	memcpy(pkt->packet.data + MASK_DATA, data->data, DATA_LENGTH);
}

/* AES */

/*
    ....
    setHeaderLinkLayer(&pkt,&link);
    encryptAES(root,&pkt,&data,len);
    err = bluetoothSendInst(root->handle,&pkt);
    ...
    ==
    err = sendPacketAES(root,&pkt,&data,len);
    ==
    err = sendPacket(root,&pkt,&link,&data,len,ENC_MASK_AES);
*/


static inline int32_t generate_key_iv_pair(session_t *root){
	// Generate Key and Initial Vector
	if(!RAND_bytes(root->key,sizeof(root->key)) || !RAND_bytes(root->iv,sizeof(root->iv))){
		root->state = STATUS_INTERR;
		return ERR_GEN_KEY_FAIL;
	}root->state = STATUS_RUNNING;
	return SUCCESS;
}

int32_t generatePairAES(session_t *root){
	return generate_key_iv_pair(root);
}

/* Compare hash value for checking data integrity */
int32_t decryptAES(session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,uint64_t size){
    int32_t len;
    struct PACKET_DATA dl;
    __pkt2data(pkt,&dl);
    if(!compareMD5(pkt,&dl,size)){return ERR_HASH_FAIL;}
	len = decrypt(dl.data,(int32_t)size,sess->key,sess->iv,data->data);
    removePadding(data->data,len);
    return len;
}
/* Set MD5 hash value for data integrity and set packet data area with AES encryption */
int32_t encryptAES(session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,uint64_t size){
	int32_t len = 0;
	struct PACKET_DATA temp;
    uint8_t hash[MD5_DIGEST_LENGTH];
	memset(&temp,0,sizeof(struct PACKET_DATA));
	len = encrypt(data->data,(int32_t)size,sess->key,sess->iv,temp.data);
	if(len>0){
        hash_md5(hash,temp.data,len);
        setPacketOffset(pkt,MASK_LEN,0,len,2);
        setPacketOffset16(pkt,MASK_DC,hash,MD5_DIGEST_LENGTH);
        __data2pkt(pkt,&temp);
	}return len;
}

int32_t sendPacketAES(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,uint64_t len){
    int32_t size;
    setHeaderLinkLayer(pkt,link);
    setPacketOffset(pkt,MASK_TTL,0,sess->pnc.ttl,2);
    size = encryptAES(sess,pkt,data,len);
    if(size<=0){return size;}
    return bluetoothSendInst(fd,pkt);
}

int32_t recvPacketAES(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data){
    uint64_t src;
    uint64_t dst;
    uint64_t len;
    int32_t size;
    size = bluetoothRecvInst(fd,pkt,&len);
    if(size<=0){return size;}
    getPacketOffset(pkt,MASK_SRC,0,&src,8);
    getPacketOffset(pkt,MASK_DST,0,&dst,8);
    if(src!=sess->clientAddr||dst!=sess->hostAddr){return ERR_FALSE_ADDR;}
    return decryptAES(sess,pkt,data,size);
}

/* RSA */

int32_t RSA_readySession(session_t *sess){
	RSA *rsa = NULL;
	if(!RSA_createKey(&rsa,RSA_KEY_LEN)){
		sess->hostKey = NULL;
		return -1;
	}
	sess->hostKey = rsa;
	return SUCCESS;
}
int32_t decryptRSA(session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,uint64_t size){
    struct PACKET_DATA dl;
    __pkt2data(pkt,&dl);
    if(!compareMD5(pkt,&dl,size)){return ERR_HASH_FAIL;}
    if(sess->hostKey==NULL){return ERR_ECRYPT_FAILURE;}   // preventing segmentation fault
	return private_decrypt(sess->hostKey,dl.data,size,data->data);
}
int32_t encryptRSA(session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,uint64_t size){
    int32_t len = 0;
    struct  PACKET_DATA temp;
    uint8_t hash[MD5_DIGEST_LENGTH];
	memset(&temp,0,sizeof(struct PACKET_DATA));
    if(sess->clientKey==NULL){return ERR_ECRYPT_FAILURE;} // preventing segmentation fault
    len = public_encrypt(sess->clientKey,data->data,size,temp.data);
    if(len>0){
        hash_md5(hash,temp.data,len);
        setPacketOffset(pkt,MASK_LEN,0,len,2);
        setPacketOffset16(pkt,MASK_DC,hash,MD5_DIGEST_LENGTH);
        __data2pkt(pkt,&temp);
    }return len;
}
int32_t sendPacketRSA(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,uint64_t len){
    int32_t size;
    setHeaderLinkLayer(pkt,link);
    setPacketOffset(pkt,MASK_TTL,0,sess->pnc.ttl,2);
    size = encryptRSA(sess,pkt,data,len);
    if(size<=0){return size;}
    return bluetoothSendInst(fd,pkt);
}
int32_t recvPacketRSA(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data){
    uint64_t src;
    uint64_t dst;
    uint64_t len;
    int32_t size;
    size = bluetoothRecvInst(fd,pkt,&len);
    if(size<=0){return size;}
    getPacketOffset(pkt,MASK_SRC,0,&src,8);
    getPacketOffset(pkt,MASK_DST,0,&dst,8);
    if(src!=sess->clientAddr||dst!=sess->hostAddr){return ERR_FALSE_ADDR;}
    return decryptRSA(sess,pkt,data,size);
}

/* SEND & RECV wrapper function */
int32_t sendPacket(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,uint64_t len,int32_t method){
    switch(method){
        case(ENC_MASK_AES):return sendPacketAES(fd,sess,pkt,link,data,len);
        case(ENC_MASK_RSA):return sendPacketRSA(fd,sess,pkt,link,data,len);
        case(ENC_MASK_NON):return bluetoothSendInst(fd,pkt);
        default:return ERR_ECRYPT_FAILURE;
    }
}
int32_t recvPacket(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,int32_t method){
    uint64_t dlen = 0;
    int32_t rcnt  = 0;
    switch(method){
        case(ENC_MASK_AES):return recvPacketAES(fd,sess,pkt,data);
        case(ENC_MASK_RSA):return recvPacketRSA(fd,sess,pkt,data);
        case(ENC_MASK_NON):
            rcnt = bluetoothRecvInst(fd,pkt,&dlen);
            __pkt2data(pkt,data);
            return rcnt;
        default:return ERR_ECRYPT_FAILURE;
    }
}

int32_t inputCheckInside(session_t *sess,struct PACKET_LINK_LAYER *link){
    if(link->src==link->dst){return ERR_LAND_STATE;}            // LAND
    if(link->src==0||link->dst==0) {return ERR_INTERNAL_PKT;}   // This is an internal packet
    if(link->src!=sess->clientAddr){return ERR_FALSE_ADDR;}
    if(link->dst==sess->hostAddr){return 0;}
    return 1;
}

int32_t inputCheckOutside(session_t *sess,struct PACKET_LINK_LAYER *link){
    if(link->src==0&&link->dst==0) {return ERR_INTERNAL_PKT;}   // This is an internal packet   
    if(link->src==link->dst){return ERR_LAND_STATE;}            // LAND
    if(link->src==0||link->dst==0) {return ERR_FALSE_ADDR;}
    return 1;
}


/* Session Management */
static inline int32_t __recv(int32_t fd,struct PACKET_CHAIN *pkt){
	int32_t rcnt = read(fd,&(pkt->packet),BUFFER_SIZE);
	uint64_t len = 0;
	if(rcnt > 0){
		getPacketOffset(pkt,MASK_LEN,0,&len,2);	// Boundary Length Check (Detect Overflow Attack)
		if(len < DATA_LENGTH){
			return rcnt;
		}
	}return ERR_SOCKET_IO_FAIL;
}


static inline int32_t __send(int32_t fd,struct PACKET_CHAIN *pkt){
	return write(fd,&(pkt->packet),BUFFER_SIZE);
}

int32_t RSA_pubkeyInquiry(int32_t fd,session_t *sess){
	struct PACKET_LINK_LAYER link;
	struct PACKET_CHAIN pkt;
	struct PACKET_DATA data;
	uint64_t flags = 0;
	uint64_t len   = 0;

    memset(&link,0,sizeof(struct PACKET_LINK_LAYER));	
    memset(&pkt, 0,sizeof(struct PACKET_CHAIN));
    memset(&data,0,sizeof(struct PACKET_DATA));
	link.src   = sess->hostAddr;
	link.dst   = sess->clientAddr;
	link.flags = FLAG_QUERY | FLAG_ENCRYPT;    
	setHeaderLinkLayer(&pkt,&link);
	
	if(__send(fd,&pkt)<=0){return ERR_SOCKET_IO_FAIL; }
	if(__recv(fd,&pkt)<=0){return ERR_SOCKET_IO_FAIL; }

	getPacketOffset(&pkt,MASK_FLAGS,0,&flags,1);
	getPacketOffset(&pkt,MASK_LEN,0,&len,2);

	if(!(flags&(FLAG_QUERY | FLAG_ENCRYPT | FLAG_ACK))){return ERR_AUTH_FAIL; }
    if(len!=RSA_BYTE_SIZE){return ERR_AUTH_FAIL;}

	getPacketOffset16(&pkt,MASK_DATA,data.data,RSA_BYTE_SIZE);
	if(hashCompare(&pkt,&data,len)){
        if(sess->clientKey!=NULL){ RSA_free(sess->clientKey); }
	    sess->clientKey = malloc(RSA_BYTE_SIZE);
	    memcpy(sess->clientKey,&data,RSA_BYTE_SIZE);
        return SUCCESS;	     
	}return ERR_HASH_FAIL;
}

int32_t RSA_pubkeyResponse(int32_t fd,session_t *sess){
	struct PACKET_LINK_LAYER link;
	struct PACKET_CHAIN pkt;
	struct PACKET_DATA data;
	RSA *pubkey = NULL;

	uint8_t hash[MD5_DIGEST_LENGTH];
    memset(&link,0,sizeof(struct PACKET_LINK_LAYER));	
    memset(&pkt, 0,sizeof(struct PACKET_CHAIN));
    memset(&data,0,sizeof(struct PACKET_DATA));	
    	
	link.src = sess->hostAddr;
	link.dst = sess->clientAddr;
	link.flags = FLAG_QUERY | FLAG_ENCRYPT | FLAG_ACK;
	setHeaderLinkLayer(&pkt,&link);
	
	pubkey = RSAPublicKey_dup(sess->hostKey);
	memcpy(&data,pubkey,RSA_BYTE_SIZE);

	setPacketOffset16(&pkt,MASK_DATA,data.data,RSA_BYTE_SIZE);
	hash_md5(hash,data.data,RSA_BYTE_SIZE);
	setPacketOffset16(&pkt,MASK_DATA,hash,MD5_DIGEST_LENGTH);
	return __send(fd,&pkt);
}

int32_t RSA_pubkeyInquiryClient(int32_t fd,session_t *sess){
	struct PACKET_LINK_LAYER link;
	struct PACKET_CHAIN pkt;
	struct PACKET_DATA data;
	uint64_t flags = 0;
	uint64_t len   = 0;

    memset(&link,0,sizeof(struct PACKET_LINK_LAYER));	
    memset(&pkt, 0,sizeof(struct PACKET_CHAIN));
    memset(&data,0,sizeof(struct PACKET_DATA));
	link.dst   = sess->hostAddr;
	link.src   = sess->clientAddr;
	link.flags = FLAG_QUERY | FLAG_ENCRYPT;    
	setHeaderLinkLayer(&pkt,&link);
	
	if(__send(fd,&pkt)<=0){return ERR_SOCKET_IO_FAIL; }
	if(__recv(fd,&pkt)<=0){return ERR_SOCKET_IO_FAIL; }

	getPacketOffset(&pkt,MASK_FLAGS,0,&flags,1);
	getPacketOffset(&pkt,MASK_LEN,0,&len,2);

	if(!(flags&(FLAG_QUERY | FLAG_ENCRYPT | FLAG_ACK))){return ERR_AUTH_FAIL; }
    if(len!=RSA_BYTE_SIZE){return ERR_AUTH_FAIL;}

	getPacketOffset16(&pkt,MASK_DATA,data.data,RSA_BYTE_SIZE);
	if(hashCompare(&pkt,&data,len)){
        if(sess->clientKey!=NULL){ RSA_free(sess->clientKey); }
	    sess->clientKey = malloc(RSA_BYTE_SIZE);
	    memcpy(sess->clientKey,&data,RSA_BYTE_SIZE);
        return SUCCESS;	     
	}return ERR_HASH_FAIL;
}

int32_t RSA_pubkeyResponseClient(int32_t fd,session_t *sess){
	struct PACKET_LINK_LAYER link;
	struct PACKET_CHAIN pkt;
	struct PACKET_DATA data;
	RSA *pubkey = NULL;

	uint8_t hash[MD5_DIGEST_LENGTH];
    memset(&link,0,sizeof(struct PACKET_LINK_LAYER));	
    memset(&pkt, 0,sizeof(struct PACKET_CHAIN));
    memset(&data,0,sizeof(struct PACKET_DATA));	
    	
	link.dst = sess->hostAddr;
	link.src = sess->clientAddr;
	link.flags = FLAG_QUERY | FLAG_ENCRYPT | FLAG_ACK;
	setHeaderLinkLayer(&pkt,&link);
	
	pubkey = RSAPublicKey_dup(sess->hostKey);
	memcpy(&data,pubkey,RSA_BYTE_SIZE);

	setPacketOffset16(&pkt,MASK_DATA,data.data,RSA_BYTE_SIZE);
	hash_md5(hash,data.data,RSA_BYTE_SIZE);
	setPacketOffset16(&pkt,MASK_DATA,hash,MD5_DIGEST_LENGTH);
	return __send(fd,&pkt);
}

int32_t HostParingProcess(int32_t fd,session_t *root,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,int32_t dlen,int32_t level,uint8_t test[DATA_LENGTH]){
    int32_t err;
    link->flags = FLAG_ENCRYPT;

    switch(level){
        // PHASE 1 : SEND AES PAIRING KEY WITH RSA
        case(1):   
	        if(generate_key_iv_pair(root)){return ERR_GEN_KEY_FAIL; }       // generate session key
	        memcpy(test,root->key,AES256_KEYSIZE);
	        memcpy(test + AES256_KEYSIZE,root->iv,AES256_IVSIZE);
            // send AES Session Key and AES Initial Vector
            root->method = ENC_MASK_NON;
            err = sendPacketRSA(fd,root,pkt,link,data,AES256_KEYSIZE+AES256_IVSIZE);
            if(err<0){
                root->method = ENC_MASK_RSA;
                return err;}
            break;
        // PHASE 2 : SEND AES TEST BYTES WITH AES      
        case(2):
            random_bytes(test,RANDOM_SEED);
            memcpy(data,test,RANDOM_SEED);
            err = sendPacketAES(fd,root,pkt,link,data,RANDOM_SEED);
            if(err<0){
                root->method = ENC_MASK_RSA;
                return err;}            
            break;
        // PHASE 3 : TEST AES TEST BYTES        
        case(3):
            err = decryptRSA(root,pkt,data,dlen);
            if(err<=0){return err;}
            err = strncmp((char *)test,(char *)data->data,RANDOM_SEED);
            memset(data,0,sizeof(struct PACKET_DATA));
            sendPacketAES(fd,root,pkt,link,data,0); 
            if(err==0){
                link->flags = FLAG_ENCRYPT;
            }else{
                link->flags = FLAG_ENCRYPT | FLAG_FIN;
            }return err;
            break;
        default:
            root->method = ENC_MASK_RSA;
            return ERR_AUTH_FAIL;
            break;
    }return SUCCESS;
}

int32_t ClientParingProcess(int32_t fd,session_t *root,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,int32_t dlen,int32_t level,uint8_t test[DATA_LENGTH]){
    int32_t err;
    link->flags = FLAG_ENCRYPT;
    
    switch(level){
        // PHASE 1 : RECV AES KEY WITH RSA 
        case(1):   
            root->method = ENC_MASK_NON;
            random_bytes(test,RANDOM_SEED);            
            memcpy(root->key,data->data,AES256_KEYSIZE);
            memcpy(root->iv,data->data+AES256_KEYSIZE,AES256_IVSIZE);
	        link->flags = FLAG_QUERY | FLAG_ENCRYPT | FLAG_ACK;
	        memcpy(data,test,RANDOM_SEED);
            err = sendPacketAES(fd,root,pkt,link,data,RANDOM_SEED);
            if(err<0){
                root->method = ENC_MASK_RSA;
                return err;}
            break;
        // PHASE 2 : RECV AND SEND AES TEST BYTES WITH AES      
        case(2):
	        err = decryptAES(root,pkt,data,dlen);
	        if(err<=0){
                root->method = ENC_MASK_RSA;
                return err;}
            err = sendPacketRSA(fd,root,pkt,link,data,RANDOM_SEED);
            if(err<0){
                root->method = ENC_MASK_RSA;
                return err;}       
            break;
        // PHASE 3 : POST PROCESSING        
        case(3):
            root->method = ENC_MASK_RSA;
            break;
        default:
            root->method = ENC_MASK_RSA;
            return ERR_AUTH_FAIL;
            break;
    }return SUCCESS;
}

