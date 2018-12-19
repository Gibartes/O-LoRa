package OloraPacket;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.lang.StringBuilder;
import java.lang.Long;

/* @ __Gibartes__ @ */

/* 
Usage Example :
    byte[] packet = new byte[56];
    byte[] value  = setValue(9987654321L,8);
    setHeaderOffset(packet,value,MASK_DST,8);
    byte[] dst    = getHeaderOffset(packet,MASK_DST,8);
    Long impress  = getValue(dst);      
    String str    = byteArrayToHexString(dst);
    System.out.println(str);
    System.out.println(Long.toUnsignedString(impress)); 
*/

public static class OloraPacket{
    public static final int MASK_SRC        =   0;
    public static final int MASK_DST        =   8;
    public static final int MASK_CM         =  16;
    public static final int MASK_HP         =  24;
    public static final int MASK_PROTO      =  25;
    public static final int MASK_ID         =  26;
    public static final int MASK_FLAGS      =  28;
    public static final int MASK_FRAG       =  29;
    public static final int MASK_SEQ        =  30;
    public static final int MASK_TMS        =  32;
    public static final int MASK_LEN        =  36;
    public static final int MASK_TTL        =  38;
    public static final int MASK_PARAM      =  39;
    public static final int MASK_DC         =  40;
    public static final int MASK_DATA       =  56;

    public static final int FLAG_URGENT     = 128; 
    public static final int FLAG_ACK        =  64;
    public static final int FLAG_FIN        =  32;
    public static final int FLAG_ENCRYPT    =  16;
    public static final int FLAG_QUERY      =   8;
    public static final int FLAG_BROKEN     =   4;
    public static final int FLAG_ERROR      =   2;
    public static final int FLAG_RESP       =   1;

    public static final int PROT_TCP        =  32;
    public static final int PROT_UDP        =  16;
    public static final int PROT_VOICE      =   8;
    public static final int PROT_RT         =   4;
    public static final int PROT_GRAPHICS   =   2;
    public static final int PROT_TEXT       =   1;
   
    public static final int PACKET_FULL     = 1008;
    public static final int PACKET_MINI     =  256;
    public static final int DATA_LENGTH     = PACKET_FULL - MASK_DATA;
    public static final int XBEE_DATA_LEN   = PACKET_MINI - MASK_DATA;
    
    // Hex ���
    public static String byteArrayToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){sb.append(String.format("%02X ", b&0xff));}
        return sb.toString();
    }
    // ��� Offset���κ��� Size��ŭ ������ Set
    public static void   setHeaderOffset(byte[] packet,byte[] value,int offset,int size){
        for(int i=0;i<size;i++){packet[offset+i] = value[i];}
    }
    // ��� Offset���κ��� Size��ŭ ������ Get
    public static byte[] getHeaderOffset(byte[] packet,int offset,int size){
        return Arrays.copyOfRange(packet,offset,offset+size-1);
    }   
    // ����� ���� ���� ����
    public static byte[] setValue(long value,int unit){
        return ByteBuffer.allocate(unit).putLong(value).array();
    }
    // ������� ���� Byte[]�� ���ڷ� ����. (��) Java�� native�� unsigned ��������
    // ����Ʈ ������ 0      
    public static long   getValue(byte[] value,int unit){
        ByteBuffer buffer = ByteBuffer.wrap(value);
        switch(unit){
            case(1):return (long)buffer.getChar();
            case(2):return (long)buffer.getShort();
            case(4):return (long)buffer.getInt();
            case(8):return buffer.getLong();
            default:return 0;
        }
    }
    // @Overloading. �ִ� 8����Ʈ �� Ư�� ��ġ�� ����Ʈ �� ��� �ٶ� ��  
    public static long   getValue(byte[] value,int offset,int unit){
        ByteBuffer buffer = ByteBuffer.wrap(value);
        switch(unit){
            case(1):return (long)buffer.getChar(offset);
            case(2):return (long)buffer.getShort(offset);
            case(4):return (long)buffer.getInt(offset);
            case(8):return buffer.getLong();
            default:return 0;
        }
    }
    // @Overloading. ������� ���� Byte[]�� Unsigned Long ��ü�� ��ȯ
    public static Long   getValue(byte[] value){
        ByteBuffer buffer = ByteBuffer.wrap(value);    
        return Long.parseUnsignedLong(String.valueOf(buffer.getLong()));
    }
    // Byte[] ��
    public static boolean compare(byte[] value0,byte[] value1){
        return Arrays.equals(value0,value1);
    }
}
