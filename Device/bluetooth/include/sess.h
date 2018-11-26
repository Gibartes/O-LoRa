#include <olorastd.h>
#include <blue.h>

int32_t generatePairAES(session_t *root);
int32_t decryptAES(session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,uint64_t size);
int32_t encryptAES(session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,uint64_t size);
int32_t sendPacketAES(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,uint64_t len);
int32_t recvPacketAES(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data);
int32_t RSA_readySession(session_t *sess);
int32_t decryptRSA(session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,uint64_t size);
int32_t encryptRSA(session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,uint64_t size);
int32_t sendPacketRSA(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,uint64_t len);
int32_t recvPacketRSA(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data);
int32_t sendPacket(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,uint64_t len,int32_t method);
int32_t recvPacket(int32_t fd,session_t *sess,struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,int32_t method);
int32_t inputCheckInside(session_t *sess,struct PACKET_LINK_LAYER *link);
int32_t inputCheckOutside(session_t *sess,struct PACKET_LINK_LAYER *link);
int32_t RSA_pubkeyInquiry(int32_t fd,session_t *sess);
int32_t RSA_pubkeyResponse(int32_t fd,session_t *sess);
int32_t RSA_pubkeyInquiryClient(int32_t fd,session_t *sess);
int32_t RSA_pubkeyResponseClient(int32_t fd,session_t *sess);
int32_t HostParingProcess(int32_t fd,session_t *root,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,int32_t dlen,int32_t level,uint8_t test[DATA_LENGTH]);
int32_t ClientParingProcess(int32_t fd,session_t *root,struct PACKET_CHAIN *pkt,struct PACKET_LINK_LAYER *link,struct PACKET_DATA *data,int32_t dlen,int32_t level,uint8_t test[DATA_LENGTH]);
