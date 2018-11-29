.SUFFIXES: .c .o
CC          = gcc

ifeq ($(shell arch),x86_64)
CFLAGS      = -O2 -c -Werror -Wall -Wstrict-overflow
LFLAGS      = -O2 -Werror -Wall -Wstrict-overflow
#CFLAGS     = -O2 -c -Werror -Walloca -Wstringop-overflow
#LFLAGS     = -O2 -Werror -Walloca -Wstringop-overflow
else
CFLAGS      = -O2 -c -Werror
LFLAGS      = -O2 -Werror
endif

# -Walloca				 (for debug)
# -Wstringop-overflow 	 (for debug)
# -> type option detects buffer overflow in calls to string handling functions like memcpy 

INCLUDE     = -I../include
LDFLAGS     = -lbluetooth -lpthread -lrt -lcrypto -lpam -lpam_misc
PROJECT     = oloraNT

LIBNAME     = olora
LIBTARGET   = olorapkt.so
SERVICE_FL  = olorant.service

MAIN      = task.o
SRCS      = blue.c hash.c pkt.c sess.c log.c
OBJS      = $(SRCS: .c = .o) 
LIBSRCS   = pkt.c hash.c
PYTHON3FL = ../src/packet.py

.c.o:
	$(CC) $(CFLAGS) $(LDFLAGS) $(INCLUDE) $<

all: $(PROJECT) $(LIBTARGET) MVARCH SHELL
	
$(PROJECT): $(MAIN) $(OBJS) 
	$(CC) task.c -o $@ $(OBJS) $(LDFLAGS) $(INCLUDE)

$(LIBTARGET): $(LIBSRCS)
	$(CC) -shared -Wl,-soname,$(LIBNAME) -o $@ $(LFLAGS) $(INCLUDE) -Wall -Werror -fpic $(LIBSRCS) $(LDFLAGS)

SHELL: test.c $(OBJS)
	$(CC) test.c -o shell $(OBJS) $(LDFLAGS) -lncurses $(INCLUDE) 


MVARCH: $(PROJECT) $(LIBTARGET)
ifeq ($(shell arch),x86_64)
	mkdir -p ~/lib/amd64   2>/dev/null
	cp $(PROJECT) $(LIBTARGET) ~/lib/amd64/
else
	mkdir -p ~/lib/arm   2>/dev/null
	cp $(PROJECT) $(LIBTARGET) ~/lib/arm/
endif


ifeq ($(PREFIX),)
PREFIX := /usr/local
endif

DESTDIR ?=
DEAMONDIR   = /etc/systemd/system/
PYTHON3PATH = /usr/lib/python3/dist-packages/

install: $(PROJECT) $(LIBTARGET)
	install -d $(DESTDIR)$(PREFIX)/bin
	install -m 750 $(PROJECT) $(DESTDIR)$(PREFIX)/bin
	install -d $(DESTDIR)$(PREFIX)/lib/
	install -m 644 $(LIBTARGET) $(DESTDIR)$(PREFIX)/lib
	install -m 750 $(SERVICE_FL) $(DEAMONDIR)
	sudo systemctl enable olorant
	sudo service olorant start
	install -d $(DESTDIR)$(PYTHON3PATH)
	install -m 755 $(PYTHON3FL) $(PYTHON3PATH)
uninstall:
	sudo systemctl disable olorant
	sudo service olorant stop
	rm -f $(DESTDIR)$(PREFIX)/bin/$(PROJECT)
	rm -f $(DESTDIR)$(PREFIX)/lib/$(LIBTARGET)
	rm -f $(DEAMONDIR)$(SERVICE_FL)
	rm -f $(PYTHON3PATH)$(PYTHON3FL)
clean:
	rm -rf *.o $(PROJECT) $(LIBTARGET) shell
