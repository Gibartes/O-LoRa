#pragma once

#include <fcntl.h>
#include <errno.h>
#include <memory.h>
#include <signal.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <linux/types.h>
#include <semaphore.h>
#include <pthread.h>

#include <sys/time.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/poll.h>
#include <sys/ioctl.h>

#include <list.h>

/* ENCRYPTION MODULE */
#include <openssl/md5.h>
#include <openssl/sha.h>
#include <openssl/rsa.h>
#include <openssl/conf.h>
#include <openssl/evp.h>
#include <openssl/err.h>
#include <openssl/pem.h>
#include <openssl/rand.h>

#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <bluetooth/rfcomm.h>

/* Packet */
#include <endian.h>
#include <byteswap.h>

#define OLORA_DAEMON_NAME        "OLoRa Bluetooth Network Daemon"
#define OLORA_DAEMON_VERSION     "1.1.0"

#define OLORA_DEBUG_FLAG            1
#define OLORA_BETA_FLAG             0

/* Encryption Definitions */
#define AES256_KEYSIZE            256
#define AES256_IVSIZE             128
#define AES256_BLKSIZE             16

#define RSA_KEY_LEN              4096
#define RSA_BYTE_SIZE             512
#define RSA_KEY_2048             2048
#define RSA_KEY_2048_LEN          256
#define RSA_KEY_2048_KEY_LEN      214
#define RSA_KEY_4096             4096
#define RSA_KEY_4096_LEN          512
#define RSA_KEY_4096_KEY_LEN      478

#define ENC_MASK_AES                1
#define ENC_MASK_RSA                2
#define ENC_MASK_NON                3
    
#define EXT_PIPE_OUT             "/tmp/.olora.out"
#define EXT_PIPE_IN              "/tmp/.olora.in"
#define BUFFER_SIZE              1008
/* MESSAGE PROTOCOL STACK OFFSET INFO */
#ifndef __OLORA_PACKET_HEADER__
    #define __OLORA_PACKET_HEADER__
    #define likely(x)            __builtin_expect((x),1)
    #define unlikely(x)          __builtin_expect((x),0)
    #define MAX_BUFFER_SIZE      1008
    /* I/O Direction in unnamed pipe */
    #define private	             static
    #define READ                  0
    #define WRITE                 1

    /* PACKET OFFSET */
    #define MASK_SRC              0
    #define MASK_DST              8
    #define MASK_CM              16
    #define MASK_HP              24
    #define MASK_PROTO           25
    #define MASK_ID              26
    #define MASK_FLAGS           28
    #define MASK_FRAG            29
    #define MASK_SEQ             30
    #define MASK_TMS             32
    #define MASK_LEN             36
    #define MASK_TTL             38
    #define MASK_PARAM           39
    #define MASK_DC              40
    #define MASK_DATA            (MASK_DC + MD5_DIGEST_LENGTH)

	/* PACKET STRUCT */
    #define DATA_LENGTH          (MAX_BUFFER_SIZE-MASK_DATA)
    #define XBEE_DATA_LEN        (256-MASK_DATA)
    #define LOCAL_BACK           0xFFFFFFFF

	/* FLAGS */
    #define FLAG_URGENT          (uint8_t)0b10000000
    #define FLAG_ACK             (uint8_t)0b01000000
    #define FLAG_FIN             (uint8_t)0b00100000
    #define FLAG_ENCRYPT         (uint8_t)0b00010000
    #define FLAG_QUERY           (uint8_t)0b00001000
    #define FLAG_BROKEN          (uint8_t)0b00000100
    #define FLAG_ERROR           (uint8_t)0b00000010
    #define FLAG_RESP            (uint8_t)0b00000001

    /* PROTOCOL */
    #define PROT_TCP             (uint8_t)0b00100000
    #define PROT_UDP             (uint8_t)0b00010000
    #define PROT_VOICE           (uint8_t)0b00001000
    #define PROT_RT              (uint8_t)0b00000100
    #define PROT_GRAPHICS        (uint8_t)0b00000010
    #define PROT_TEXT            (uint8_t)0b00000001

    /* PACKET FLAGS */
    #define PACKET_USED            1
    #define PACKET_UNUSED          2
    #define PACKET_SUSPEND         4
    #define PACKET_PROTECTION      8
    #define PACKET_REMOVE         16

	/* RESULT CODE */
    #define SUCCESS                0
    #define ERR_SOCKET_IO_FAIL    -1
    #define ERR_AUTH_FAIL         -2
    #define ERR_GEN_KEY_FAIL      -3
    #define ERR_RUNNING_TASK      -4
    #define ERR_FALSE_ADDR        -5
    #define ERR_ECRYPT_FAILURE    -6
    #define ERR_CHILD_PROCESS     -7
    #define ERR_HASH_FAIL         -8
    #define ERR_SESSION_CERT      -9
    #define ERR_ECRYPT_NONE      -10
    #define ERR_LAND_STATE       -11
    #define ERR_INTERNAL_PKT     -12
    #define ERR_ZEROFILL_PKT     -13

    /* HCI SCAN CODE */
    #define NOSCAN                 0
    #define ISCAN                  1
    #define PSCAN                  2
    #define PISCAN                 3

    #define LOGPATH              (uint8_t*)"/var/log/olora.service.log"

    /* STATUS MASK */
    #define STATUS_KILL          128
    #define STATUS_EXIT           64
    #define STATUS_TIMO           32
    #define STATUS_INTERR         16
    #define STATUS_TIMO_M          8
    #define STATUS_RUNNING         4
    #define STATUS_TIMO_L          1
	
    /* SESSION STATE */
    #define SESSION_NONE           1
    #define SESSION_WAIT           2
    #define SESSION_CRAT           4
    #define SESSION_PEND           8

    #define RANDOM_SEED          256
#endif

#define waitMutex(mutx,cond,err){                         \
    pthread_mutex_lock(&(mutx));                          \
    err = pthread_cond_wait(&(cond),&(mutx));             \
    pthread_mutex_unlock(&(mutx));}
    
#define waitMutex2(mutx,cond){                            \
    pthread_mutex_lock(&(mutx));                          \
    pthread_cond_wait(&(cond),&(mutx));                   \
    pthread_mutex_unlock(&(mutx));  }

#define waitMutexTime(mutx,cond,tick,err){                \
    pthread_mutex_lock(&(mutx));                          \
    err = pthread_cond_timedwait(&(cond),&(mutx),&(tick));\
    pthread_mutex_unlock(&(mutx));  }
      
#define wakeMutex(mutx,cond,err){                         \
    pthread_mutex_lock(&(mutx));                          \
    err = pthread_cond_signal(&(cond));                   \
    pthread_mutex_unlock(&(mutx));}

#define wakeMutex2(mutx,cond){                            \
    pthread_mutex_lock(&(mutx));                          \
    pthread_cond_signal(&(cond));                         \
    pthread_mutex_unlock(&(mutx));}

union bcursor_t {
    uint8_t     u8[8];
    uint16_t    u16[4];
    uint32_t    u32[2];
    uint64_t    u64;
};

struct bcursor_T {
    union bcursor_t bytes;
};

struct PACKET_HEADER {
    uint8_t SRC[8];
    uint8_t DST[8];
    uint8_t CM[8];
    uint8_t HP[1];
    uint8_t PROTO[1];
    uint8_t ID[2];
    uint8_t FLAGS[1];
    uint8_t FRAG[1];
    uint8_t SEQ[2];
    uint8_t TMS[4];
    uint8_t LEN[2];
    uint8_t TTL[1];
    uint8_t PARAM[1];
    uint8_t DC[MD5_DIGEST_LENGTH];
}__attribute__((packed));

struct PACKET_LINK_LAYER {
    uint64_t src;                       // (8 byte) Source Address
    uint64_t dst;                       // (8 byte) Destination Address
    uint64_t cm;                        // (8 byte) Channel Mask
    uint8_t  hp;                        // (1 byte) HP
    uint8_t  proto;                     // (1 byte) PROTOCOL
    uint16_t id;                        // (2 byte) Network ID
    uint8_t  flags;                     // (1 byte) Packet Flag Info
    uint8_t  frag;                      // (1 byte) Fragmented Packet
    uint16_t seq;                       // (4 byte) Packet Sequential Number
}__attribute__((packed));

struct PACKET_SECURITY_LAYER {
    uint8_t DC[MD5_DIGEST_LENGTH];      // Data Checksum
}__attribute__((packed));

struct PACKET_NETWORK_CURRENT_CONFIG {
    uint8_t  ttl;
    uint8_t  param;
    uint32_t sec;
    uint32_t usec;
}__attribute__((packed));

union PACKET {
    struct PACKET_HEADER header;
    uint8_t data[MAX_BUFFER_SIZE];
};

struct PACKET_DATA {
    uint8_t data[DATA_LENGTH];
};

struct PACKET_CHAIN {
    uint8_t flag;
    struct list_head list;
    union PACKET   packet;
};

typedef struct __session_t {
    /* Session Info */
    sem_t    *slock;
    int32_t  id;            // session ID
    int32_t  state;         // session State
    int32_t  method;        // encryption method;
    /* Descriptors */
    int32_t  hci;           // hci socket       ( defaut -1 )
    int32_t  sock;          // bluetooth socket ( default -1 )
    int32_t  temp;          // temp descriptor
    /* Address */ 
    uint64_t hostAddr;      // host Address
    uint64_t clientAddr;    // client Address
    /* AES Key */
    uint8_t  key[AES256_KEYSIZE];
    uint8_t  iv[AES256_IVSIZE];
    /* RSA Key */
    RSA      *hostKey;
    RSA      *clientKey;
    /* Session Packet Chain */
    struct PACKET_CHAIN *pkt;
    struct PACKET_NETWORK_CURRENT_CONFIG pnc;
    struct list_head *streamIn;
    struct list_head *streamOut;
} session_t;

struct THREAD_CONTROL_BOX{
    /* THREAD CONTROL */
    sem_t   *sig;
    uint8_t mask;
    uint8_t task;
    uint8_t flag;
    /* Descriptors */
	int32_t  in;            // descriptor for task in
	int32_t  out;           // descriptor as  task out
    /* Timeout ticks */    
    int64_t	ticks;
    /* Log */
    FILE 	*Log;
    sem_t   *log;
    /* Session */
    session_t *sess;
};


/* LOG HANDLE */
FILE    *logOpen(const uint8_t *filepath);
int32_t logClean(FILE *fp,const uint8_t *filepath);
void    logClose(FILE *fp);
void    logWrite(FILE *fp,sem_t *lock,char *fmt,...);

/* ENCRYPTION & DECRYPTION */
void    hash_md5(uint8_t result[MD5_DIGEST_LENGTH],uint8_t *plain,size_t size);
void    hash_sha2(uint8_t result[SHA256_DIGEST_LENGTH],uint8_t *plain,size_t size);
int32_t encrypt(uint8_t *plaintext,int32_t plaintext_len,uint8_t *key,uint8_t *iv,uint8_t *ciphertext);
int32_t decrypt(uint8_t *ciphertext,int32_t ciphertext_len,uint8_t *key,uint8_t *iv,uint8_t *plaintext);
void    removePadding(uint8_t plaintext[DATA_LENGTH],uint32_t len);
int32_t random_bytes(uint8_t *key,const uint32_t len);

int32_t RSA_createKey(RSA **rsaKey,int32_t bits);
void    RSA_destroyKey(RSA *rsaKey);
void    RSA_dump(FILE *fd,const char *text,size_t size);
int32_t public_encrypt(RSA *pub,uint8_t *data,int32_t data_len,uint8_t *encrypted);
int32_t public_decrypt(RSA *pub,uint8_t *enc_data,int32_t data_len,uint8_t *key,uint8_t *decrypted);
int32_t private_encrypt(RSA *pri,uint8_t *data,int32_t data_len,uint8_t *key,uint8_t *encrypted);
int32_t private_decrypt(RSA *pri,uint8_t *enc_data,int32_t data_len,uint8_t *decrypted);
int32_t hashCompare(struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,size_t len);

/* LOW LEVEL PACKET CONTROL FUNCTION */
int32_t createNamedPipe(const char *name,const uint32_t oflags,mode_t mode);
void    byteSwap(union bcursor_t *ori,union bcursor_t *new,const uint32_t subset, const uint8_t bitmode, const uint8_t cond);
int32_t checkDataBlank(uint8_t *data, uint32_t size);
void    initUnit(struct PACKET_CHAIN *pkt);
void    copyAndInitUnit(struct PACKET_CHAIN *pkt, uint8_t *text);
void    hexPrint(const void *pv, size_t len);
uint64_t batoui64(bdaddr_t addr);
bdaddr_t uitoba64(uint64_t baddr);

void    enqueuePacket(struct list_head *head, struct PACKET_CHAIN *pkt);
struct  PACKET_CHAIN *dequeuePacket(struct list_head *head);

void    pkt2data(struct PACKET_CHAIN *pkt, struct PACKET_DATA *data);
void    data2pkt(struct PACKET_CHAIN *pkt, struct PACKET_DATA *data);

void    setPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, const uint64_t opt, const uint8_t bitmode);
void    getPacketOffset(struct PACKET_CHAIN *msg, const uint32_t offset, const uint32_t subset, uint64_t *opt, const uint8_t bitmode);
void    setPacketOffset16(struct PACKET_CHAIN *msg, const uint32_t offset, uint8_t *hash, const uint32_t bitmode);
void    getPacketOffset16(struct PACKET_CHAIN *msg, const uint32_t offset, uint8_t *hash, const uint32_t bitmode);

void    maskPacketOffset(struct PACKET_CHAIN *pkt, const uint32_t offset, const uint32_t subset, const uint64_t opt, const uint8_t bitmode);
void    unmaskPacketOffset(struct PACKET_CHAIN *pkt, const uint32_t offset, const uint32_t subset, const uint64_t opt, const uint8_t bitmode);

int32_t compareMD5(struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,const uint64_t len);

int32_t echoLink(uint64_t host, struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link);
void    setHeaderLinkLayer(struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link);
void    setHeaderDataLayer(struct PACKET_CHAIN *pkt, uint8_t *data, size_t length);
void    setHeader(struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link, struct PACKET_NETWORK_CURRENT_CONFIG *pnc, uint8_t *data, size_t length);
void    getHeaderLinkLayer(struct PACKET_CHAIN *pkt, struct PACKET_LINK_LAYER *link);
void    setReportPacket(uint64_t host,uint64_t client,struct PACKET_CHAIN *pkt,uint8_t *data,size_t length);

/* Python Backend Code */
void    setHeaderOffset(struct PACKET_HEADER *pkt,const uint32_t offset,const uint32_t subset,const uint64_t opt,const uint8_t bitmode);
void    getHeaderOffset(struct PACKET_HEADER *pkt,const uint32_t offset,const uint32_t subset,uint64_t *opt,const uint8_t bitmode);
void    setHeaderOffset16(struct PACKET_HEADER *pkt, const uint32_t offset, uint8_t *hash, const uint32_t bitmode);
void    getHeaderOffset16(struct PACKET_HEADER *pkt, const uint32_t offset, uint8_t *hash, const uint32_t bitmode);
void    initPacketHeader(struct PACKET_HEADER *pkt);
void    str2pkth(struct PACKET_HEADER *pkt,void *buf);
void    str2pktb(void *pkt,void *buf,uint32_t opt);
void    pkth2str(struct PACKET_HEADER *pkt,void *str);
void    pktCombine(void *pkt,void *head,void *body,uint32_t opt);
void    initArea(void *pkt,uint32_t size);
