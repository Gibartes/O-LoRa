package com.team_olora.olora_beta;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ListRoomItem {
    private Drawable iconDrawable ;
    private String titleStr ;
    private String descStr ;
    private int roomnum;
    private int userkey;
    private int nonRead;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setDesc(String desc) {
        descStr = desc ;
    }
    public void setRoomnum(int num){roomnum = num;}
    public void setUserkey(int num){userkey = num;}
    public void setNonRead(int num){nonRead = num;}

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getDesc() {
        return this.descStr ;
    }
    public int getRoomnum(){return this.roomnum;}
    public int getUserkey(){return this.userkey;}
    public int getNonRead(){return this.nonRead;}
}
