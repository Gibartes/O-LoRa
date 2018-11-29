package com.team_olora.olora_beta;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class ListFriendItem {
    private Drawable iconDrawable;
    private int key;
    private String nameSTR;
    private String descSTR;

    public void setIcon(Drawable icon) {
        iconDrawable = icon;
    }
    public void setKey(int KEY) {
        key = KEY;
    }
    public void setName(String addr) {nameSTR = addr;}
    public void setDesc(String name) {
        descSTR = name;
    }

    public Drawable getIcon() {
        return this.iconDrawable;
    }
    public int getKey() {return this.key; }
    public String getName() {
        return this.nameSTR;
    }
    public String getDesc() {return this.descSTR;}
}
