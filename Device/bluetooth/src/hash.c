#include <olorastd.h>

#ifndef __OLORA_PACKET_HEADER__
    #define AES256_KEYSIZE       256
    #define AES256_IVSIZE        128
    #define AES256_BLKSIZE        16
    #define AES256_SUPDLEN       910    // (Based on Packet length Max - 1008)
    #define RSA_KEY_LEN			4096
    #define RSA_BYTE_SIZE		 512
    #define RSA_KEY_2048        2048
    #define RSA_KEY_2048_LEN     256
    #define RSA_KEY_2048_KEY_LEN 214
    #define RSA_KEY_4096        4096
    #define RSA_KEY_4096_LEN     512
    #define RSA_KEY_4096_KEY_LEN 478
#endif

static int32_t padding = RSA_PKCS1_PADDING;

// Hash (innocent)
void hash_md5(uint8_t result[MD5_DIGEST_LENGTH],uint8_t *plain,size_t size){
    MD5(plain,size,result);
}

void hash_sha2(uint8_t result[SHA256_DIGEST_LENGTH],uint8_t *plain,size_t size){
    memcpy(result,SHA256(plain,size,0),SHA256_DIGEST_LENGTH);
}

// AES encrypt && decrypt
int32_t encrypt(uint8_t *plaintext,int32_t plaintext_len,uint8_t *key,uint8_t *iv,uint8_t *ciphertext){
    EVP_CIPHER_CTX *ctx;
    int32_t len;
    int32_t ciphertext_len;
    if(!(ctx = EVP_CIPHER_CTX_new())){return -1;}
    if(1!=EVP_EncryptInit_ex(ctx,EVP_aes_256_cbc(),NULL,key,iv)){return -1;}
    if(1!=EVP_EncryptUpdate(ctx, ciphertext, &len, plaintext, plaintext_len)){return -1;}
    ciphertext_len = len;
    if(1!=EVP_EncryptFinal_ex(ctx,ciphertext+len,&len)){return -1;}
    ciphertext_len += len;
    EVP_CIPHER_CTX_free(ctx);
    return ciphertext_len;
}

int32_t decrypt(uint8_t *ciphertext,int32_t ciphertext_len,uint8_t *key,uint8_t *iv,uint8_t *plaintext){
    EVP_CIPHER_CTX *ctx;
    int32_t len;
    int32_t plaintext_len;
    if(!(ctx = EVP_CIPHER_CTX_new())){return -1;}
    if(1!= EVP_DecryptInit_ex(ctx,EVP_aes_256_cbc(),NULL,key,iv)){return -1;}
    if(1!= EVP_DecryptUpdate(ctx,plaintext,&len,ciphertext,ciphertext_len)){return -1;}
    plaintext_len = len;
    if(1!= EVP_DecryptFinal_ex(ctx,plaintext+len,&len)){return -1;}
    plaintext_len += len;
    EVP_CIPHER_CTX_free(ctx);
    return plaintext_len;
}

int32_t random_bytes(uint8_t *key,const uint32_t len){
    if(!RAND_bytes(key,len)){return 1;}
    return 0;
}

void removePadding(uint8_t plaintext[DATA_LENGTH],uint32_t len){
    int32_t pad;
    pad = AES256_BLKSIZE - (len%AES256_BLKSIZE);
    if(len+pad>DATA_LENGTH){return;}
    for(int32_t i=len+pad-1;i>=len;i--){plaintext[i] = 0x00;}
}

// RSA 
int32_t RSA_createKey(RSA **rsaKey,int32_t bits){
    BIGNUM *bne=NULL;
    bne=BN_new();
    if(BN_set_word(bne,RSA_F4)!=1){return 0;}
	*rsaKey=RSA_new();
	if(RSA_generate_key_ex(*rsaKey,bits,bne,NULL)!=1){
		BN_free(bne);
		return 0;
	}return 1;
}
void RSA_destroyKey(RSA *rsaKey){
    RSA_free(rsaKey);
}

void RSA_dump(FILE *fd,const char *text,size_t size){
    BIO_dump_fp(fd,text,size);
}
int32_t public_encrypt(RSA *pub,uint8_t *data,int32_t data_len,uint8_t *encrypted){
    return RSA_public_encrypt(data_len,data,encrypted,pub,padding);
}
int32_t public_decrypt(RSA *pub,uint8_t *enc_data,int32_t data_len,uint8_t *key,uint8_t *decrypted){
    return RSA_public_decrypt(data_len,enc_data,decrypted,pub,padding);
}
int32_t private_encrypt(RSA *pri,uint8_t *data,int32_t data_len,uint8_t *key,uint8_t *encrypted){
    return RSA_private_encrypt(data_len,data,encrypted,pri,padding);
}
int32_t private_decrypt(RSA *pri,uint8_t *enc_data,int32_t data_len,uint8_t *decrypted){
    return RSA_private_decrypt(data_len,enc_data,decrypted,pri,padding);
}
int32_t hashCompare(struct PACKET_CHAIN *pkt,struct PACKET_DATA *data,size_t len){
    uint8_t hash[MD5_DIGEST_LENGTH];
    uint8_t temp[MD5_DIGEST_LENGTH];
    memset(hash,0,MD5_DIGEST_LENGTH);
    memset(temp,0,MD5_DIGEST_LENGTH);
    getPacketOffset16(pkt,MASK_DC,temp,MD5_DIGEST_LENGTH);
    hash_md5(hash,data->data,len);  
    return strncmp((char *)temp,(char *)hash,MD5_DIGEST_LENGTH)==0?1:0;
}
