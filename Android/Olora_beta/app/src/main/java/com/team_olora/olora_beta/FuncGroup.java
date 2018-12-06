package com.team_olora.olora_beta;

import java.lang.Math;

public class FuncGroup {

    static String byteArrayToHexString(byte[] b) {

        // String Buffer can be used instead

        String hs = "";
        String stmp = "";

        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));

            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }

            if (n < b.length - 1) {
                hs = hs + "";
            }
        }

        return hs;
    }
    static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    static String intToBinary(int number)
    {
        return Integer.toBinaryString(number);
    }

    static byte[] getHPbyte(int ch){
        int HP = ch & 0x00000007;
        byte[] HPbyte = new byte[1];
        HPbyte[0] = (byte) HP;
        return HPbyte;
    }

    static byte[] getIDbyte(int ch) {
        byte[] IDbyte = new byte[2];
        int ID = ch & 0x0003FFF8;
        ID >>= 3;

        IDbyte[0] = (byte) (ID >> 8);
        IDbyte[1] = (byte) ID;
        return IDbyte;
    }

    static byte[] getCHbyte(int ch){
        byte[] CH = new byte[3];
        int HP = ch & 0x00000007;
        int ID = ch & 0x0003FFF8;
        CH[0] = (byte) HP;
        CH[1] = (byte) (ID >> 8);
        CH[2] = (byte) ID;

        return CH;
    }



}
