package com.team_olora.olora_beta;

import android.util.Log;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Service_packet {

    public static long macaddr;
    public static int isPublic;
    // dumy echo no - mklee
    public static int isEcho;

    public Service_packet() {

    }

    byte[] s_addr = new byte[8];
    byte[] d_addr = new byte[8];
    byte[] cm = new byte[16];
    byte hp;
    byte id;
    byte opt;
    byte seq;
    byte[] cmd;
    byte ts;
    //byte[] data = new byte[msg.length];

    private void setPacket() {

    }

    // 주소 변환 함수
    // string -> byte
    public byte[] converted_addr(String addr) {

        Log.d("loglog", "no addr"+addr.length());
        if(addr.length()==0){
            byte[] ret_addr = new byte[6];
            Log.d("loglog", "no addr no addr");
            Arrays.fill(ret_addr,(byte)0xFF);
            Log.d("loglog", "no addr no addr"+ret_addr);
            return ret_addr;
        }

        byte[] ret_addr = new byte[8];
        String[] splited = addr.split(":");
        //Toast.makeText(getApplicationContext(), "byte" + splited[5], Toast.LENGTH_LONG).show();

        for (int i = 0; i < 6; i++) {
            byte[] s = hex2Byte(splited[i].toString());
            ret_addr[i + 2] = s[0];
        }

        ret_addr[0] = 0;
        ret_addr[1] = 0;

        return ret_addr;
    }

    // hex string -> byte 변환
    public byte[] hex2Byte(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer
                    .parseInt(str.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    /////////////////////////////////////////////
    /////////////////////////////////////////////
    public String byte2hex(byte[] b) {

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


    // 패킷 패킹. 바로 보낼 수 있는 형태
    // id, hp 따로 - command 가 setId, setHp 아니면 hp, id 인자는 상관없음
    public byte[] converted_packet(String s, byte[] t_a, String param, byte hp, byte[] id, byte[] hash, byte[] msg) {
        byte[] s_a = converted_addr(s);
        int s_a_len = s_a.length;

        // for header
        byte[] packet = new byte[56];

        for (int i = 0; i < s_a_len; i++) {
            packet[i] = s_a[i];
        }

        for (int i = 8; i < 16; i++) {
            packet[i] = t_a[i - 8];
        }

        // data length
        Log.d("finalTest", "msg len : "+msg.length);
        if(msg.length < 256)
        {
            packet[37] = (byte)msg.length;
        }
        else
        {
            int length = msg.length;
            packet[36] = (byte)(length >> 8);
            //packet[37] = (byte)(packet[36]*256);
            packet[37] = (byte)(length);
        }

        // reset command
        switch (param) {
            case "ping":
                packet[39] = (byte) 0xFF;
                break;
            case "FORCE_RESET":
                packet[39] = (byte) 0x00;
                break;
            case "SET_DISCOVERY_TIME":
                packet[39] = (byte) 0x0E;
                break;
            case "SEND_BROADCAST":
                packet[39] = (byte) 0x12;
                break;
            case "SEND_UNICAST":
                packet[39] = (byte) 0x10;
                break;
            case "SET_NODEIDENTIFIER":  // NID
                packet[39] = (byte) 0x0b;
                break;
            case "SET_NETWORK_ID":      // ID
                packet[39] = (byte) 0x09;

                // id 셋팅
                packet[26] = id[0];
                packet[27] = id[1];

                break;
            case "SET_PREAMIBLE_ID":    // HP
                packet[39] = (byte) 0x07;
                // hp 셋팅
                packet[24] = hp;
                break;

            case "START_DISCOVERY":
                packet[39] = (byte) 0x0F;
                break;

            case "SET_CH":
                packet[39] = (byte) 0x13;
                break;

            default:
                break;
        }


        /*
        byte [] MD5 = new byte[16];


        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            MD5 = md.digest(msg);
        }
        catch(NoSuchAlgorithmException e){

            e.printStackTrace();
            MD5 = null;
        }

        // endian swap for MD5
        byte[] temp_f = new byte[8];
        byte[] temp_b = new byte[8];
        for(int i = 0; i < 8; i ++)
        {
            temp_f[7 - i] = MD5[i];
        }

        for(int i = 0; i < 8; i ++)
        {
            temp_b[7 - i] = MD5[8 + i];
        }

        for(int i = 0; i < 16; i ++)
        {
            if(i < 8)
                MD5[i] = temp_f[i];
            else
                MD5[i] = temp_b[i - 8];
        }
        */

        // md5
        for(int i = 0; i < 16; i++)
        {
            //packet[40 + i] = MD5[i];
            packet[40 + i] = hash[i];
        }


        // 메시지
        byte[] packet_send = new byte[packet.length + msg.length];
        System.arraycopy(packet, 0, packet_send, 0, packet.length);
        System.arraycopy(msg, 0, packet_send, packet.length, msg.length);

        return packet_send;
    }


    // end of text index 반환
    int indexOfEOT(byte[] msg) {
        int index;
        for (index = 0; index < msg.length; index++) {
            if (msg[index] == 0x03)
                break;
        }

        return index;
    }


    // read 한것 분석 함수
    byte[] handleRead(byte[] packet) {
        byte[] command = new byte[1];
        byte[] dest_addr = new byte[8];
        byte[] datafield = null;

        Log.d("finalTest", "--\n\n-----------------Start Recived Packet  -----------------"
                +"\n"+"msg recieved : "+packetHandler.byteArrayToHexString(packet)
                +"\n"+"src : "+packetHandler.getHeaderString(packet,0,packetHandler.LEN_SRC)
                +"\n"+"dest : "+packetHandler.getHeaderString(packet,packetHandler.MASK_DST,packetHandler.LEN_DST)
                +"\n"+"cm : "+packetHandler.getHeaderString(packet,packetHandler.MASK_CM,packetHandler.LEN_CM)
                +"\n"+"hp : "+packetHandler.getHeaderString(packet,packetHandler.MASK_HP,packetHandler.LEN_HP)
                +"\n"+"proto : "+packetHandler.getHeaderString(packet,packetHandler.MASK_PROTO,packetHandler.LEN_PROTO)
                +"\n"+ "id : "+packetHandler.getHeaderString(packet,packetHandler.MASK_ID,packetHandler.LEN_ID)
                +"\n"+ "flags : "+packetHandler.getHeaderString(packet,packetHandler.MASK_FLAGS,packetHandler.LEN_FLAGS)
                +"\n"+ "frag : "+packetHandler.getHeaderString(packet,packetHandler.MASK_FRAG,packetHandler.LEN_FRAG)
                +"\n"+ "seq : "+packetHandler.getHeaderString(packet,packetHandler.MASK_SEQ,packetHandler.LEN_SEQ)
                +"\n"+ "tms : "+packetHandler.getHeaderString(packet,packetHandler.MASK_TMS,packetHandler.LEN_TMS)
                +"\n"+ "len : "+packetHandler.getHeaderString(packet,packetHandler.MASK_LEN,packetHandler.LEN_LEN)
                +"\n"+ "ttl : "+packetHandler.getHeaderString(packet,packetHandler.MASK_TTL,packetHandler.LEN_TTL)
                +"\n"+ "param : "+packetHandler.getHeaderString(packet,packetHandler.MASK_PARAM,packetHandler.LEN_PARAM)
                +"\n"+ "dc : "+packetHandler.getHeaderString(packet,packetHandler.MASK_DC,packetHandler.LEN_DC)
                +"\n"+ "#####################   END Recived Packet #################### \n\n");

        int dataLen=packetHandler.getMsgLen(packet);
        Log.d("finalTest", "len2 : "+dataLen);
        Log.d("finalTest", "data : "+packetHandler.getHeaderString(packet,packetHandler.MASK_DATA,dataLen));

        if(16==packetHandler.getParam(packet)){
            //is unicast dummy - mklee
            isPublic=0;
        }else
            isPublic=1;
        if(packetHandler.getFlags(packet)!=0){
            //에코와드
            isEcho=1;
        }else
            isEcho=0;


        command[0] = packet[39];

        for (int i = 0; i < 8; i++)
            dest_addr[i] = packet[i];

        ByteBuffer Lbuf = ByteBuffer.wrap(dest_addr);
        macaddr = Lbuf.getLong();

        if (command[0] == 0x00)
            packet = packet;

        else {

            try {
                datafield = Arrays.copyOfRange(packet, 56, 952);
            } catch (Exception e) {

            }
        }

        return datafield;
    }

    // 0 -> 그냥 send
    // 1 -> send result - 걸러내기.
    // 2 -> id, hp

    /**
     * b'\x00\x00'//0 : ForceReset
     * b'\x00\x0a'//10 : GetNodeIdentifier -> 0~20바이트의 utf encoded binary
     * b'\x00\x0b'//11 : SetNodeIdentifier -> 0~20바이트의 utf encoded binary
     * b'\x00\x0c'//12 : GetMyAddress -> 주의! 주소값은 단순 binary로 encoding된값이 아님!
     * <p>
     * b'\x00\x0e'//14 : SetDisocveryTime -> 주의! 0x04~0x23의 값임!
     * b'\x00\x0f'//15 : StartDisocveryProcess -> 주의! 업뎃도중 command를 내릴수는 있으나 discover가 완료될때까지 보낸 명령어는 queue에 그냥 쌓인다.
     * b'\x00\x10'//16 : SendSyncUnicast -> DstAddr, SEQ를 반드시 채워준다.
     * b'\x00\x12'//18 : SendBroadcast -> DstAddr, SEQ를 반드시 채워준다.
     * 19 : set channel -> HP. ID 순서대로 1byte 2byte
     */
    public int getCmd(byte opt, byte[] cmd) {
        //int command = (((cmd[0] & 0xffff) << 8) | (cmd[1] & 0xffff));
        int command = cmd[0];

        int R = -1;

        switch (command) {
            case 10: //getNodeIdentifier
                R = 0;
                break;
            case 11: //setNodeIdentifier
                R = 1;
                break;
            case 14: //setDiscovery time
                R = 2;
                break;
            case 15: //discovery
                R = 3;
                break;
            case 16: // unicast
                R = 4;
                break;
            case 18: // broadcast
                R = 5;
                break;
            case 19: // set channel
                R = 6;
                break;
            default:
                R = -2;
        }
        return R;
    }
}


