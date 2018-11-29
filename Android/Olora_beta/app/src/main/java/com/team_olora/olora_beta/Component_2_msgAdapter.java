package com.team_olora.olora_beta;

import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class Component_2_msgAdapter {
    String msg;
    byte bytearry[];
    public static int Send;
    public static int Receive;

    public Component_2_msgAdapter(){
    }

    /**send와 room키 조합해줌**/
    public byte[] encoding_msg(String str,int _send, int _receive) {
        // str : msg
        byte[] STR = str.getBytes();
        int arraysize = STR.length;
        Log.d("byte legth : ", "encoding_msg : " + arraysize);
        byte[] RSTR = new byte[arraysize+8];

        int i = 0;
        while (i < arraysize) {
            RSTR[i+8] = STR[i];
            i++;
        }

        int value = _send; // send
        RSTR[0] = (byte) (value >> 24);
        RSTR[1] = (byte) (value >> 16);
        RSTR[2] = (byte) (value >> 8);
        RSTR[3] = (byte) (value);
        value = _receive; // receive
        RSTR[4] = (byte) (value >> 24);
        RSTR[5] = (byte) (value >> 16);
        RSTR[6] = (byte) (value >> 8);
        RSTR[7] = (byte) (value);

        return RSTR;
    }
    /**send와 roomkey 파싱해줌**/
    public String parse_msg(byte[] str) {
        byte[] array = str;
        int arraysize = array.length;
        Send = (array[0]<<24 | array[1]<<16 | array[2]<<8 | array[3]);
        Log.d("pars_msg1",String.valueOf(Send));
        Receive = array[4]<<24 | array[5]<<16 | array[6]<<8 | array[7];
        Log.d("pars_msg2",String.valueOf(Receive));
        String RS = new String(array, 8, arraysize-8);
        return RS;
    }

}


