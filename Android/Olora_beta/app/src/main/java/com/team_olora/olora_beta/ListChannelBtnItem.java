package com.team_olora.olora_beta;

public class ListChannelBtnItem {
    private int key;
    private int addrSTR;
    private String nameSTR ;

    public void setKey(int KEY) {
        key = KEY ;
    }

    public void setAddr(int addr) {
        addrSTR = addr ;
    }

    public void setName(String name) {
        nameSTR = name ;
    }

    public int getKey() {
        return this.key ;
    }

    public int getAddr() {
        return this.addrSTR ;
    }

    public String getName() {
        return this.nameSTR ;
    }
}
