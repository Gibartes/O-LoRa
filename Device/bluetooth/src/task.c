#include <olorastd.h>
#include <sess.h>

#define MAX_TASK_COUNT      3
#define OUTPUT_TASK         0
#define INPUT_TASK          1
#define WATCHDOG_TASK       2

#define TASK_NONE           0
#define TASK_WATCHDOG       1
#define TASK_OUTPUT         2
#define TASK_INPUT          4
#define TASK_SPIN           8
#define TASK_SUM           15

/* This process controlls bluetooth terminal point amongs apps */
    
static pthread_mutex_t      input;
static pthread_mutex_t      output;
static pthread_mutex_t      spinner;
static pthread_cond_t       input_cond;
static pthread_cond_t       output_cond;
static pthread_cond_t       spinner_cond;
static pthread_barrier_t    barrier;
static pthread_barrier_t    hbarrier;

		    
static inline void setMask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema,uint8_t mask){
    sem_wait(sema);
    tcb->mask |= mask;
    sem_post(sema);
}

static inline void setMaskNaive(struct THREAD_CONTROL_BOX *tcb,uint8_t mask){
    tcb->mask |= mask;
}

static inline uint8_t getMask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema,uint8_t val){
    uint8_t ret;
    sem_wait(sema);
    ret = tcb->mask&val;
    sem_post(sema);
    return ret;
}
static inline uint8_t takeMask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema,uint8_t val){
    uint8_t ret;
    sem_wait(sema);
    ret = tcb->mask&val;
    tcb->mask&=(~val);
    sem_post(sema);
    return ret;
}

static inline void setTask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema,uint8_t task){
    sem_wait(sema);
    tcb->task |= task;
    sem_post(sema);
}

static inline uint8_t getTask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema){
    uint8_t ret;
    sem_wait(sema);
    ret = tcb->task;
    sem_post(sema);
    return ret;
}
static inline uint8_t takeTask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema,uint8_t val){
    uint8_t ret;
    sem_wait(sema);
    ret = tcb->task&val;
    tcb->task&=(~val);
    sem_post(sema);
    return ret;
}
static inline void initTask(struct THREAD_CONTROL_BOX *tcb,sem_t *sema){
    sem_wait(sema);
    tcb->task = 0;
    sem_post(sema);
}

static inline void wakeUpTask(void){
    wakeMutex2(spinner,spinner_cond);
    wakeMutex2(output,output_cond);
    wakeMutex2(input,input_cond);
}

static inline void wakeUpTaskBroadcast(void){
    pthread_cond_broadcast(&input_cond);
    pthread_cond_broadcast(&output_cond);
    pthread_cond_broadcast(&spinner_cond);
}

void print_version(void){
    printf("[ %s ]\n",OLORA_DAEMON_NAME);
    printf("\tversion : %s\n",OLORA_DAEMON_VERSION);
    printf("\tauthor  : Gibartes\n");
    fflush(stdout);
}


/* 
 * TASK SYNC TIMER
 * Monitoring Target : Host Socket Listener FD. & Client Socket Listener FD. 
 * Automaton : Phase 1 (Connection StandBy), Phase 2 (On-Connection)
 */
 
static void *watchDog(void *param){
    struct THREAD_CONTROL_BOX *tcb 	= (struct THREAD_CONTROL_BOX *)param;
    struct pollfd pf[3];    // socket listen

    uint8_t status = 0;
    int32_t phase     = 1;
    int32_t   err     = 0;
    int32_t   tout    = tcb->ticks; // timeout

    pf[0].fd = tcb->in;           /* H -> C (STATIC) TIMEOUT_L */
    pf[0].events = POLLIN|POLLPRI|POLLHUP|POLLERR;
    pf[2].fd = tcb->sess->id;     /* H -> C (STATIC) TIMEOUT_M */
    pf[2].events = POLLIN|POLLPRI|POLLHUP|POLLERR;

    phase1:
        phase = 1;      
        pf[1].fd = tcb->sess->temp;    /* Dummy Descriptor */
        pf[1].events = POLLIN|POLLPRI|POLLHUP;
        logWrite(tcb->Log,tcb->log,"[*] [WatchDog] phase down.");
        goto loop;

    phase2:
        phase = 2;
        pf[1].fd = tcb->sess->sock;    /* Notification Channel Descriptor */
        pf[1].events = POLLIN|POLLPRI|POLLHUP;
        logWrite(tcb->Log,tcb->log,"[*] [WatchDog] phase up.");
        goto loop;

    /* Monitor */
    loop:
        while(1){
            if(getMask(tcb,tcb->sig,STATUS_EXIT)){goto exit;}
            if(getMask(tcb,tcb->sig,STATUS_KILL)){goto reset;}
            err = poll(pf,3,tout);
            if(err<0){
                logWrite(tcb->Log,tcb->log,"[*] [WatchDog] exit : ENO:[%d]-EC:[%d]-Polling.",errno,err);
                setMask(tcb,tcb->sig,STATUS_EXIT);
                goto exit;
            }else if(err==0){
                setMask(tcb,tcb->sig,STATUS_TIMO|STATUS_TIMO_L|STATUS_TIMO_M);
            }else{
                sem_wait(tcb->sig);
                if(err==1){
                    if((pf[0].revents&(POLLIN|POLLPRI))){
                        setMaskNaive(tcb,STATUS_TIMO|STATUS_TIMO_M);}
                    if((pf[1].revents&(POLLIN|POLLPRI))){
                        setMaskNaive(tcb,STATUS_TIMO_L|STATUS_TIMO_M);}
                    if((pf[2].revents&(POLLIN|POLLPRI))){
                        setMaskNaive(tcb,STATUS_TIMO|STATUS_TIMO_L);}	        
                }else{
                    if((pf[0].revents&(POLLIN|POLLPRI))&&(pf[1].revents&(POLLIN|POLLPRI))){
                        setMaskNaive(tcb,STATUS_TIMO_M);}
                    if((pf[0].revents&(POLLIN|POLLPRI))&&(pf[2].revents&(POLLIN|POLLPRI))){
                        setMaskNaive(tcb,STATUS_TIMO);}
                    if((pf[1].revents&(POLLIN|POLLPRI))&&(pf[2].revents&(POLLIN|POLLPRI))){
                        setMaskNaive(tcb,STATUS_TIMO_L);}
                }
                sem_post(tcb->sig);
            }
            if(pf[0].revents&(POLLHUP|POLLERR)){setMask(tcb,tcb->sig,STATUS_EXIT);}
            if(pf[2].revents&(POLLHUP|POLLERR)){setMask(tcb,tcb->sig,STATUS_EXIT);}
            if(getMask(tcb,tcb->sig,STATUS_RUNNING)){
                takeMask(tcb,tcb->sig,STATUS_RUNNING);
                if(phase==2){
                    setMask(tcb,tcb->sig,STATUS_EXIT);
                    wakeUpTask();
                    logWrite(tcb->Log,tcb->log,"[*] [WatchDog] phase escalation error");
                    goto exit;}    // This is not allowed.
                wakeUpTask();
                printf("Phase up\n");
                goto phase2;
            }
            if(pf[1].revents&(POLLHUP|POLLERR)){setMask(tcb,tcb->sig,STATUS_KILL);}
            wakeUpTask();
        }

    /* EXIT : goto phase1 */	
    reset:

        logWrite(tcb->Log,tcb->log,"[*] [WatchDog] wait signal to sync...");    
        wakeUpTask();
        setTask(tcb,tcb->sig,TASK_WATCHDOG);
        while(1){
            usleep(tout);		// HARD waiting : waiting for system ticks ms(about 0.1ms)
            status = getTask(tcb,tcb->sig);
            if(status==TASK_SUM){break;}
            // send proper mutex signal when pending per each case.
            if((status&TASK_INPUT)==0){
                wakeMutex2(input,input_cond);}
            if((status&TASK_OUTPUT)==0){
                wakeMutex2(output,output_cond);}
            if((status&TASK_SPIN)==0){
                wakeMutex2(spinner,spinner_cond);}
        }
        initTask(tcb,tcb->sess->slock);
        pthread_barrier_wait(&hbarrier);		/* Task Sync */
        takeMask(tcb,tcb->sig,STATUS_KILL);
        logWrite(tcb->Log,tcb->log,"[*] [WatchDog] barrier pass...");
        printf("Phase down\n");
        goto phase1;
        
    /* EXIT : TASK EXIT */
    exit:
        // send proper mutex signal when pending per each case.
        if(pthread_mutex_destroy(&input)==EBUSY){
            pthread_cond_broadcast(&input_cond);}
        if(pthread_mutex_destroy(&output)==EBUSY){
            pthread_cond_broadcast(&output_cond);}
        if(pthread_mutex_destroy(&spinner)==EBUSY){
            pthread_cond_broadcast(&spinner_cond);}
        logWrite(tcb->Log,tcb->log,"[*] [WatchDog] sync wait.");
        close(tcb->sess->sock);
        close(tcb->in);
        close(tcb->out);
        pthread_barrier_wait(&barrier);		/* Task Sync */
        pthread_exit(NULL);
}


static void *outputTask(void *param){
    struct THREAD_CONTROL_BOX *tcb 	= (struct THREAD_CONTROL_BOX *)param;
    struct PACKET_CHAIN	msg;
    struct PACKET_DATA data;
    struct PACKET_LINK_LAYER link;

    /*
	uint64_t systemHost = 0;
	uint64_t remoteHost = 0;
	*/
    int32_t  err 	= 0;
    uint64_t len    = 0;
    uint64_t flag   = 0;
    int32_t flags = fcntl(tcb->in,F_GETFL,0);
    fcntl(tcb->in,F_SETFL,flags|O_NONBLOCK);

    __builtin_prefetch(&msg.packet,1,3);    
    while(1){
        waitMutex2(output,output_cond);
        if(getMask(tcb,tcb->sig,STATUS_EXIT)){goto exit;}
        if(getMask(tcb,tcb->sig,STATUS_KILL)){
            logWrite(tcb->Log,tcb->log,"[*] [Output] wait signal to sync...");
            setTask(tcb,tcb->sig,TASK_OUTPUT);
            logWrite(tcb->Log,tcb->log,"[*] [Output] barrier wait...");            
            pthread_barrier_wait(&hbarrier);
            takeMask(tcb,tcb->sig,STATUS_KILL|STATUS_TIMO_L);
            logWrite(tcb->Log,tcb->log,"[*] [Output] barrier pass...");            
            continue;}
        if(getMask(tcb,tcb->sig,STATUS_TIMO_L)){
            takeMask(tcb,tcb->sig,STATUS_TIMO_L);
            continue;}
            
        memset(&msg.packet,0,BUFFER_SIZE);
        memset(&data,0,DATA_LENGTH);
        err = bluetoothRecvInst(tcb->in,&msg,&len);

        if(err<=0 && errno!=EAGAIN){
            setMask(tcb,tcb->sig,STATUS_EXIT);
            logWrite(tcb->Log,tcb->log,"[*] [Output] exit : ENO:[%d]-EC:[%d]-LEN:[%llu].",errno,err,len);
            goto exit;
        }

        getHeaderLinkLayer(&msg,&link);
        err = inputCheckOutside(tcb->sess,&link);
        if(err==ERR_FALSE_ADDR){
            getPacketOffset(&msg,MASK_FLAGS,0,&flag,1);
            if(flag&FLAG_FIN){
                setMask(tcb,tcb->sig,STATUS_KILL);
                logWrite(tcb->Log,tcb->log,"[*] [Output] session exit by system request.");
            }continue;
        }
        else if(err<1){
            if(err!=ERR_INTERNAL_PKT){
                logWrite(tcb->Log,tcb->log,"[*] [Output] packet drop : [%d]-LEN:[%llu]-SRC:[%llu]-DST:[%llu].",err,len,link.src,link.dst);
            }continue;
        }
        //getPacketOffset(&msg,MASK_DST,0,&systemHost,8);
        setPacketOffset(&msg,MASK_DST,0,tcb->sess->clientAddr,8);
        pkt2data(&msg,&data);
        err = hashCompare(&msg,&data,DATA_LENGTH);
        if(err==0){
            /* Notify error to sender.
            getPacketOffset(&msg,MASK_SRC,0,&remoteHost,8);
            setPacketOffset(&msg,MASK_DST,0,remoteHost,8);
            setPacketOffset(&msg,MASK_SRC,0,systemHost,8);
            setPacketOffset(&msg,MASK_FLAGS,0,FLAG_BROKEN|FLAG_ERROR|FLAG_RESP,1);
            sem_wait(tcb,tcb->sess->slock);
            err = write(tcb->out,&msg.packet,BUFFER_SIZE);
            sem_post(tcb,tcb->sess->slock);
            if(err<=0 && errno!=EAGAIN){
                setMask(tcb,tcb->sig,STATUS_EXIT);
                logWrite(tcb->Log,tcb->log,"[*] [Output] exit : ENO:[%d]-EC:[%d].",errno,err);
            }*/
            logWrite(tcb->Log,tcb->log,"[*] [Output] packet drop : Broken Hash");
            continue;
        }
        err = sendPacket(tcb->sess->sock,tcb->sess,&msg,&link,&data,len,tcb->sess->method);
        if(err<=0){
            setMask(tcb,tcb->sig,STATUS_KILL);
            logWrite(tcb->Log,tcb->log,"[*] [Output] client disonnected. : ENO:[%d]-EC:[%d]-LEN:[%llu].",errno,err,len);
            continue;
        }
    }

    exit:
        logWrite(tcb->Log,tcb->log,"[*] [Output] sync wait.");
        pthread_barrier_wait(&barrier);
        pthread_exit(NULL);
}

// C --> H

static void *inputTask(void *param){
    struct THREAD_CONTROL_BOX *tcb 	= (struct THREAD_CONTROL_BOX *)param;
    struct PACKET_CHAIN	msg;
    struct PACKET_DATA data;
    struct PACKET_LINK_LAYER link;


    int32_t  res    = 0;
    int32_t  err 	= 0;
    uint64_t len    = 0;

    __builtin_prefetch(&msg.packet,1,3);
    while(1){
        waitMutex2(input,input_cond);

        if(getMask(tcb,tcb->sig,STATUS_EXIT)){goto exit;}
        if(getMask(tcb,tcb->sig,STATUS_KILL)){
            logWrite(tcb->Log,tcb->log,"[*] [Input] wait signal to sync...");
            setTask(tcb,tcb->sig,TASK_INPUT);
            /* Notify session down to system
            setPacketOffset(&msg,MASK_FLAGS,0,FLAG_FIN|FLAG_RESP,1);
            setPacketOffset(&msg,MASK_DST,0,0,8);
            sem_wait(tcb,tcb->sess->slock);
            err = write(tcb->out,&msg.packet,BUFFER_SIZE);
            sem_post(tcb,tcb->sess->slock);
            */
            pthread_barrier_wait(&hbarrier);
            takeMask(tcb,tcb->sig,STATUS_KILL|STATUS_TIMO);            
            logWrite(tcb->Log,tcb->log,"[*] [Input] barrier pass...");
            continue;}	    
        if(getMask(tcb,tcb->sig,STATUS_TIMO)){
            takeMask(tcb,tcb->sig,STATUS_TIMO);
            continue;}
          
        memset(&msg.packet,0,BUFFER_SIZE);
        memset(&data,0,DATA_LENGTH);
        err = recvPacket(tcb->sess->sock,tcb->sess,&msg,&data,tcb->sess->method);
        if(err<=0 && errno!=EAGAIN){
            setMask(tcb,tcb->sig,STATUS_KILL);
            logWrite(tcb->Log,tcb->log,"[*] [Input] client disconnected : ENO:[%d]-EC:[%d]-LEN:[%llu].",errno,err,len);
            continue;}

        getHeaderLinkLayer(&msg,&link);
        getPacketOffset(&msg,MASK_LEN,0,&len,2);
        res = inputCheckInside(tcb->sess,&link);
        if(len>DATA_LENGTH || res<0){
            logWrite(tcb->Log,tcb->log,"[*] [Input] packet drop : [%d]-LEN:[%llu]-SRC:[%llu]-DST:[%llu].",res,len,link.src,link.dst);
            continue;}    // IGNORE
        err = hashCompare(&msg,&data,DATA_LENGTH);
        if(err==0){
            logWrite(tcb->Log,tcb->log,"[*] [Input] packet drop : Broken Hash");
            continue;}
        //sem_wait(tcb,tcb->sess->slock);
        err = write(tcb->out,&msg.packet,BUFFER_SIZE);
        //sem_post(tcb,tcb->sess->slock);
        if(err<=0 && errno!=EAGAIN){
            setMask(tcb,tcb->sig,STATUS_EXIT);
            logWrite(tcb->Log,tcb->log,"[*] [MainTask] exit : ENO:[%d]-EC:[%d].",errno,err);
        }
    }
    
    exit:
        logWrite(tcb->Log,tcb->log,"[*] [Input] sync wait.");
        pthread_barrier_wait(&barrier);
        pthread_exit(NULL);	
}

static int32_t spin(void *param){
    struct THREAD_CONTROL_BOX *tcb 	= (struct THREAD_CONTROL_BOX *)param;
    int32_t err = 0;

    while(1){   
        waitMutex2(spinner,spinner_cond);
        if(getMask(tcb,tcb->sig,STATUS_EXIT)){
            logWrite(tcb->Log,tcb->log,"[*] [MainTask] exit signal...");
            takeMask(tcb,tcb->sig,STATUS_RUNNING);
            pthread_barrier_wait(&barrier);	
            return 0;}
        if(getMask(tcb,tcb->sig,STATUS_KILL)){
            logWrite(tcb->Log,tcb->log,"[*] [MainTask] wait signal to sync...");
            takeMask(tcb,tcb->sig,STATUS_RUNNING);
            setTask(tcb,tcb->sig,TASK_SPIN);
            pthread_barrier_wait(&hbarrier);
            takeMask(tcb,tcb->sig,STATUS_KILL);
            logWrite(tcb->Log,tcb->log,"[*] [MainTask] barrier pass...");
            return 1;}
    }return err;
}


static int32_t mainTask(uint8_t channel){
/* 	struct timeval timeout  = {
			.tv_sec 	= 1,
			.tv_usec 	= 0};
*/
    int32_t  hciSock    = -1;
    int32_t      err    = -1;
    int32_t     sock    = -1;
    int32_t    ticks    = sysconf(_SC_CLK_TCK);
    uint64_t locAddr    = 0;
    uint64_t remAddr    = 0;
    int32_t ipipe[2];
    FILE 	   *Log;
    sem_t       sig;
    sem_t       log;
    sem_t     slock;
    session_t  sess;
    pthread_t service_thread[MAX_TASK_COUNT];
    pid_t pid[MAX_TASK_COUNT];
    //struct timeval  now;
    //struct timespec timeout;    
    signal(SIGPIPE,SIG_IGN);
    if(pipe(ipipe)==-1){
        printf("[-] Cannot create unnamed pipe.");
        exit(1);}
    memset(&sess,0,sizeof(session_t));
    sem_init(&sig,  0,1);
    sem_init(&log,  0,1);
    sem_init(&slock,0,1);
    struct THREAD_CONTROL_BOX *tcb  = malloc(sizeof(struct THREAD_CONTROL_BOX));
    memset(tcb,0,sizeof(struct THREAD_CONTROL_BOX));
           
    Log = logOpen(LOGPATH);
    if(Log==NULL){printf("[*] logging disabled.\n");}
 
    struct hci_dev_info di;
    hciSock = createHciSocket();
    if(hciSock<0){
        free(tcb);
        return -1;
    }
    locAddr = getPrimaryLocalBluetoothAddress(hciSock,&di);
    if(locAddr==0){
        printf("[-] This device doesn't have any bluetooth components.\n");
        logWrite(Log,&log,"[-] This device doesn't have any bluetooth components.");
    }

    upstartBluetooth(hciSock,di.dev_id,0);	
    err = enableScan(hciSock,di.dev_id,PISCAN);

    if(err<0){
        printf("[*] Unable to control bluetooth device. [Are you running with sudo?]\n");
        logWrite(Log,&log,"[*] Unable to control bluetooth device. [Are you running with sudo?]");
        free(tcb);
        return -1;
    }

    sock    = createBluetoothSocket(channel);
    if(sock<0){free(tcb);return -1;}

    #if defined __amd64__ || defined __x86_64__
    logWrite(Log,&log,"[*] Local Device Address : %016lx on AMD64",locAddr);
    #elif defined __arm__ && defined __thumb__
    logWrite(Log,&log,"[*] Local Device Address : %016llx on ARM_Thumb",locAddr);
    #elif defined __arm__ && defined __aarch64__
    logWrite(Log,&log,"[*] Local Device Address : %016llx on ARM_64",locAddr);	
    #else
    logWrite(Log,&log,"[*] Local Device Address : %016llx on 32bit Machine",locAddr);	
    #endif
    
    /* Initialize Session */
    list_head(headIn);
    list_head(headOut);    
    memset(&sess,0,sizeof(session_t));
    sess.hostKey     = NULL;
    sess.clientKey   = NULL;
    sess.id          = sock;
    sess.state       = SESSION_NONE;
    sess.method      = ENC_MASK_NON;
    sess.hci         = hciSock;
    sess.slock       = &slock;
    sess.hostAddr    = locAddr;
    sess.streamIn    = &headIn;
    sess.streamOut   = &headOut;
    sess.temp        = ipipe[READ];
    /* Initialize TCB */
    tcb->sig         = &sig;
    tcb->mask        = 0;
    tcb->task        = 0;
    tcb->flag        = 0;
    tcb->ticks       = ticks;
    tcb->Log         = Log;
    tcb->log         = &log;
    tcb->sess        = &sess;

    printf("[*] Waiting for capturing pipe...\n");
    tcb->in          = createNamedPipe(EXT_PIPE_IN,O_RDONLY,0660);
    tcb->out         = createNamedPipe(EXT_PIPE_OUT,O_WRONLY,0660);
    if(tcb->in<=0 || tcb->out<=0){
        logWrite(Log,&log,"[*] pipe occupied.");
        free(tcb);
        return -1;
    }

    pthread_mutex_init(&input,NULL);
    pthread_mutex_init(&output,NULL);
    pthread_mutex_init(&spinner,NULL);
    pthread_cond_init(&input_cond,NULL);
    pthread_cond_init(&output_cond,NULL);
    pthread_cond_init(&spinner_cond,NULL);
    pthread_barrier_init(&barrier,NULL,MAX_TASK_COUNT);
    pthread_barrier_init(&hbarrier,NULL,MAX_TASK_COUNT+1);        // marking

    printf("[*] Start Tasks...\n");	
    pid[WATCHDOG_TASK] = pthread_create(&service_thread[WATCHDOG_TASK], \
								NULL,watchDog,(void *)tcb);
    if(pid[WATCHDOG_TASK]){
        logWrite(Log,&log,"[*] cannot execute watchdog task for this client.");
        goto exit;
    }

    pid[OUTPUT_TASK] = pthread_create(&service_thread[OUTPUT_TASK], \
								NULL,outputTask,(void *)tcb);
    if(pid[OUTPUT_TASK]){
        setMask(tcb,tcb->sig,STATUS_EXIT);
        logWrite(Log,&log,"[*] cannot execute watchdog task for this client.");
        goto exit;
    }

    pid[INPUT_TASK] = pthread_create(&service_thread[INPUT_TASK], \
								NULL,inputTask,(void *)tcb);
    if(pid[INPUT_TASK]){
        setMask(tcb,tcb->sig,STATUS_EXIT);
        logWrite(Log,&log,"[*] cannot execute watchdog task for this client.");
        goto exit;
    }

    pthread_barrier_init(&barrier,NULL,MAX_TASK_COUNT+1);

    printf("[*] Start Session.\n");
    logWrite(Log,&log,"[*] start session...");  
    while(1){
        int32_t clientfd;
        bdaddr_t src;
        sem_wait(tcb->sig);
        tcb->mask = 0;
        sem_post(tcb->sig);
        sem_wait(tcb->log);
        err = logClean(Log,LOGPATH);
        if(err==1){tcb->Log = Log;}
        sem_post(tcb->log);
        logWrite(Log,&log,"[*] session wait\n");

        enableScan(hciSock,di.dev_id,PISCAN);  /* Discoverable */
        printf("[*] Accept wait...\n");
        
        while(1){
            /*
            gettimeofday(&now,NULL);
            timeout.tv_sec  = now.tv_sec;
            timeout.tv_nsec = now.tv_usec*10000;
            waitMutexTime(spinner,spinner_cond,timeout,err);
            */
            waitMutex2(spinner,spinner_cond); 
            if(getMask(tcb,tcb->sig,STATUS_EXIT)){
                logWrite(tcb->Log,tcb->log,"[*] [MainTask] exit signal...");
                pthread_barrier_wait(&barrier);
                goto exit;}
            if(getMask(tcb,tcb->sig,STATUS_KILL)){
                logWrite(tcb->Log,tcb->log,"[*] [MainTask] wait signal to sync...");
                setTask(tcb,tcb->sig,TASK_SPIN);
                sem_wait(tcb->sess->slock);
                init_list_head(tcb->sess->streamIn);
                sem_post(tcb->sess->slock);
                pthread_barrier_wait(&hbarrier);
                takeMask(tcb,tcb->sig,STATUS_KILL);
                logWrite(tcb->Log,tcb->log,"[*] [MainTask] barrier pass...");
                continue;}
            if(getMask(tcb,tcb->sig,STATUS_TIMO_M)){
                takeMask(tcb,tcb->sig,STATUS_TIMO_M);
                continue;}
            clientfd = acceptBluetoothSocket(sock,&src);
            if(clientfd<0){
                logWrite(Log,&log,"[*] [MainTask] accept failure Code [%d]\n",errno);
                close(clientfd);
                setMask(tcb,tcb->sig,STATUS_EXIT);
                pthread_barrier_wait(&barrier);
                goto exit;}
            break;
        }

        remAddr = batoui64(src);
        sem_wait(tcb->sess->slock);
        sess.clientAddr = remAddr;
        sess.sock = clientfd;
        sem_post(tcb->sess->slock);	    
        enableScan(hciSock,di.dev_id,NOSCAN);  /* Hide */
    	
        #if defined __amd64__ || defined __x86_64__
        printf("[*] Accepted from %016lx on AMD64\n",remAddr);
        logWrite(Log,&log,"[*] Remote Device Address : %016lx on AMD64",remAddr);
        #elif defined __arm__ && defined __thumb__
        printf("[*] Accepted from %016llx on ARM\n",remAddr);
        logWrite(Log,&log,"[*] Remote Device Address : %016llx on ARM_Thumb",remAddr);
        #else
        printf("[*] Accepted from %016llx on 32Bit Machine\n",remAddr);	    
        logWrite(Log,&log,"[*] Remote Device Address : %016llx on 32bit Machine",remAddr);
        #endif

        setMask(tcb,tcb->sig,STATUS_RUNNING);
        err = spin((void *)tcb);
        close(clientfd);
        if(err){continue;}
        else{
            takeMask(tcb,tcb->sig,STATUS_EXIT);
            goto exit;}
    }
    exit:
        close(sock);
        logWrite(Log,&log,"[*] [MainTask] shutdown session.");
        close(hciSock);
        close(ipipe[READ]);
        free(tcb);
        if(sess.hostKey!= NULL){
            RSA_free(sess.hostKey);
            sess.hostKey = NULL;
        };logClose(Log);
        return 0;
}

int32_t main(void){
    int32_t err = 0;
    printf("[*]_____________________________________________________[*]\n");
    printf("[*]                    __OloraNT__                      [*]\n");
    printf("[*]     Version : %s                                 [*]\n",OLORA_DAEMON_VERSION);
    printf("[*]     Author  : Gibartes                              [*]\n");
    printf("[*]_____________________________________________________[*]\n");
    printf("[*] Start Olora Bluetooth Lower Network Bridge Service. [*]\n");
    while(1){
    	err = mainTask(1);
    	if(err!=0){break;}
    }return 0;
}
