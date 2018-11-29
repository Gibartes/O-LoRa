package com.team_olora.olora_beta;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Service_packet {

    public static long macaddr;

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
        byte[] ret_addr = new byte[8];
        String[] splited = addr.split(":");
        //Toast.makeText(getApplicationContext(), "byte" + splited[5], Toast.LENGTH_LONG).show();

        for (int i = 0; i < 6; i++) {
            byte[] s = FuncGroup.hexStringToByteArray(splited[i].toString());
            ret_addr[i + 2] = s[0];
        }

        ret_addr[0] = 0;
        ret_addr[1] = 0;

        return ret_addr;
    }



    // 패킷 패킹. 바로 보낼 수 있는 형태
    // id, hp 따로 - command 가 setId, setHp 아니면 hp, id 인자는 상관없음
    public byte[] converted_packet(String s, byte[] t_a, String command, byte hp, byte[] id, byte[] msg) {
        byte[] s_a = converted_addr(s);

        // 풀 패킷
        byte[] packet = new byte[1024];

        for (int i = 0; i < 8; i++) {
            packet[i] = s_a[i];
        }
        // reset command
        switch (command) {
            case "ping":
                packet[30] = (byte) 0xFF;
                packet[31] = (byte) 0xF2;
                break;
            case "FORCE_RESET":
                packet[30] = 0x00;
                packet[31] = 0x00;
                break;
            case "SET_DISCOVERY_TIME":

                break;
            case "SEND_BROADCAST":
                packet[30] = 0x00;
                packet[31] = 0x12;
                break;
            case "SEND_UNICAST":
                packet[30] = 0x00;
                packet[31] = 0x10;
                break;
            case "SET_NODEIDENTIFIER":  // NID
                packet[30] = 0x00;
                packet[31] = 0x0b;
                break;
            case "SET_NETWORK_ID":      // ID
                packet[30] = 0x00;
                packet[31] = 0x09;
                // id 셋팅
                packet[25] = id[0];
                packet[26] = id[1];

                break;
            case "SET_PREAMIBLE_ID":    // HP
                packet[30] = 0x00;
                packet[31] = 0x07;
                // hp 셋팅
                packet[24] = hp;
                break;

            case "START_DISCOVERY":
                packet[30] = 0x00;
                packet[31] = 0x0f;
                break;
            case "SET_CH":
                packet[30] = 0x00;
                packet[31] = 0x13;
                Log.d("SET_CH:::", "SET_Ch 커맨드 =" + FuncGroup.byteArrayToHexString(Arrays.copyOfRange(packet, 30, 32)));
                break;

            default:
                break;
        }


        for (int i = 8; i < 16; i++) {
            packet[i] = t_a[i - 8];
        }

        // 메시지
        int msgLen = 0;
        try {
            msgLen=msg.length;
        }catch (Exception e){}
        for (int i = 0; i < msgLen; i++) {
            packet[i + 36] = msg[i];
            if (i == msgLen - 1)
                packet[36 + i + 1] = 0x03;
        }

        return packet;
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
        byte[] command = new byte[2];
        byte[] source_addr = new byte[8];
        byte[] datafield = null;

        command[0] = packet[30];
        command[1] = packet[31];
        int i=0;
        for (i = 0; i < 8; i++)
            source_addr[i] = packet[i];
        Log.d("get user", "source_addr: "+FuncGroup.byteArrayToHexString(source_addr));
        ByteBuffer Lbuf = ByteBuffer.wrap(source_addr);
        macaddr = Lbuf.getLong();
        Log.d("get user", "macaddr: "+macaddr);

        if (command[0] == 0x00 && command[1] == 0x00)
            packet = packet;
        else {

            try {
                datafield = Arrays.copyOfRange(packet, 36, indexOfEOT(packet));
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
        int command = (((cmd[0] & 0xffff) << 8) | (cmd[1] & 0xffff));
        int R = -1;
        /*
        if ((opt & 0x40) == 0) {
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
            }
        }
        */
        if((opt&01000000) == 64)
            return R;
        if((opt&0x08) == 0)
        {
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
            }
        }
        else if((opt&01000000) == 8)
        {
            R = 7;
        }


        /*if(cmd[0] == 0x00 && cmd[1] == 0x07  ||  cmd[0] == 0x00 && cmd[1] == 0x0f)
        { //
            return 2;
        }

        if((opt & 0b0100000) == 64) {
            return 0;
        }
        else {
            return 1;
        }*/
        return R;
    }


}

