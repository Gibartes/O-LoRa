import os,sys

class PIPE_LIST(object):
    NT_IN      = "/tmp/.olora.out"
    NT_OUT     = "/tmp/.olora.in"
    XB_IN      = "/tmp/.xbolora.out"
    XB_OUT     = "/tmp/.xbolora.in"

class PRSS_LIST(object):
    UNSELECTED = 0
    OLORANT    = 1
    OLORAXB    = 3
