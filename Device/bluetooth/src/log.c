#include <olorastd.h>

#define MAX_SIZE    10240000        // 10MiB
static int32_t beCreated = 0;

FILE *logOpen(const uint8_t *filepath){
    if(beCreated){return NULL;}
    FILE *file = fopen(filepath,"a");
    if(file){
        /* Turn buffering off */
        /* if(setvbuf(file,NULL,_IONBF,0)!=0){return NULL;} */
        beCreated = 1;
    }return file;
}

int32_t logClean(FILE *fp,const uint8_t *filepath){
    int32_t size = 0;
    if(fp!=NULL){
        fseek(fp,0,SEEK_END);
        size = ftell(fp);
        if(size>MAX_SIZE){
            fclose(fp);
            size = remove(filepath);
            if(size<0){
                beCreated = 0;
                fp = NULL;
                return -1;}
            fp = logOpen(filepath);
            /* setvbuf(fp,NULL,_IONBF,0); */
			return 1;
        }return 0
    }return -1;
}

void logClose(FILE *fp){
    if(beCreated && fp!=NULL){
        fclose(fp);
        beCreated = 0;
    }
}

void logWrite(FILE *fp,sem_t *lock,char *fmt,...){
    if(fp!=NULL && beCreated){
        sem_wait(lock);
        /* GET CURRENT TIME */
        time_t t	   = time(NULL);     /* get current calendar time */
        uint8_t  *date = asctime(localtime(&t));
        date[strlen(date)-1] = 0;        /* remove NULL character */
        fprintf(fp,"[%s] ",date);
        /*  VARIABLE ARGUMENTS  */
        /* va_list is a type to hold information about variable arguments */
        va_list list;
        /* va_start must be called before accessing variable argument list */
        va_start(list,fmt);
        /* Arguments can be accessed one by one using va_arg macro */
        vfprintf(fp,fmt,list);
        va_end(list);
        fputc('\n',fp);
        fflush(fp);
        sem_post(lock);
    }
}
