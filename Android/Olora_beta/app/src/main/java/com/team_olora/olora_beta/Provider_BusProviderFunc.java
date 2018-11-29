package com.team_olora.olora_beta;

import android.content.Intent;

public class Provider_BusProviderFunc {
    private String myNI;
    private long myMac;

    public Provider_BusProviderFunc(String _myNI, long _myMac){
        this.myNI = _myNI;
        this.myMac = _myMac;
    }

    public String getMyNI(){
        return this.myNI;
    }
    public long getMyMac(){return this.myMac;}
}
