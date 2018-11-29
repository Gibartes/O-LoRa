package com.team_olora.olora_beta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.sql.SQLInput;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
//이 클래스는 helper로 db를 커넥션 연동 시키기 위해서 꼭 필요한 클래스 이다

public class C_DB extends SQLiteOpenHelper {
    private static final String DBFILE = "chatlist.db";
    private static final int DB_VERSION = 25;
    /************************************* Net table ****************************************/
    private static final String TBL_Net = "Net_T";

    private static final String KEY_netkey = "net_key";
    private static final String KEY_netid = "net_id";    // net address
    private static final String KEY_netname = "net_name";
    private static final String KEY_netcheck = "net_check";

    private static String SQL_CREATE_NET = "CREATE TABLE IF NOT EXISTS " + TBL_Net
            + "(" +
            " " + KEY_netkey + " INTEGER PRIMARY KEY, " +
            " " + KEY_netid + " INTEGER, " +
            " " + KEY_netname + " TEXT, " +
            " " + KEY_netcheck + " INTEGER" +
            ")";
    private static final String SQL_DROP_NET = "DROP TABLE IF EXISTS " + TBL_Net;
    private static final String SQL_SELECT_NET = "SELECT * FROM " + TBL_Net;
    private static final String SQL_DELETE_NET = "DELETE FROM " + TBL_Net;
    private static final String SQL_DELETE_NET_WHERE = "DELETE FROM " + TBL_Net + " WHERE ";
    /************************************** User table ***************************************/
    private static final String TBL_User = "User_T";

    private static final String KEY_userkey = "user_key";
    private static final String KEY_username = "user_name";
    private static final String KEY_useraddr = "user_addr"; // mac address

    private static String SQL_CREATE_USER = "CREATE TABLE IF NOT EXISTS " + TBL_User
            + "(" +
            " " + KEY_userkey + " INTEGER PRIMARY KEY, " +
            " " + KEY_username + " TEXT, " +
            " " + KEY_useraddr + " LONG" +
            ")";
    private static final String SQL_DROP_USER = "DROP TABLE IF EXISTS " + TBL_User;
    public static final String SQL_SELECT_USER = "SELECT * FROM " + TBL_User;
    public static final String SQL_DELETE_USER = "DELETE FROM " + TBL_User;
    public static final String SQL_DELETE_USER_WHERE = "DELETE FROM " + TBL_User + " WHERE ";
    /************************************** List table ***************************************/
    private static final String TBL_List = "List_T";

    private static final String KEY_roomkey = "room_key";
    private static final String KEY_roomname = "room_name";
    private static final String KEY_receivekey = "room_receivekey";

    private static String SQL_CREATE_LIST = "CREATE TABLE IF NOT EXISTS " + TBL_List
            + "(" +
            " " + KEY_netkey + " INTEGER, " +
            " " + KEY_roomkey + " INTEGER PRIMARY KEY, " +
            " " + KEY_roomname + " TEXT, " +
            " " + KEY_userkey + " INTEGER, " +
            " " + KEY_useraddr + " LONG, " +
            " " + KEY_receivekey + " INTEGER" +
            ")";
    private static final String SQL_DROP_LIST = "DROP TABLE IF EXISTS " + TBL_List;
    private static final String SQL_SELECT_LIST = "SELECT * FROM " + TBL_List;
    private static final String SQL_DELETE_LIST = "DELETE FROM " + TBL_List;
    public static final String SQL_DELETE_LIST_WHERE = "DELETE FROM " + TBL_List + " WHERE ";

    /************************************** Chat table ***************************************/
    private static final String TBL_Chat = "Chat_T";

    private static final String KEY_chatmsg = "chat_msg";
    private static final String KEY_chatsORr = "chat_sORr";
    private static final String KEY_chattime = "chat_time";
    private static final String KEY_chatkey = "chat_key";

    private static String SQL_CREATE_CHAT = "CREATE TABLE IF NOT EXISTS " + TBL_Chat
            + "(" +
            " " + KEY_netkey + " INTEGER, " +
            " " + KEY_roomkey + " INTEGER, " +
            " " + KEY_username + " TEXT, " +
            " " + KEY_chatmsg + " TEXT, " +
            " " + KEY_chattime + " DATE," +
            " " + KEY_chatsORr + " BOOLEAN, " +
            " " + KEY_userkey + " INTEGER," +
            " " + KEY_chatkey + " INTEGER PRIMARY KEY " +
            ")";

    private static final String SQL_DROP_CHAT = "DROP TABLE IF EXISTS " + TBL_Chat;
    public static final String SQL_SELECT_CHAT = "SELECT * FROM " + TBL_Chat;
    public static final String SQL_DELETE_CHAT = "DELETE FROM " + TBL_Chat;
    public static final String SQL_DELETE_CHAT_WHERE = "DELETE FROM " + TBL_Chat + " WHERE ";

    /******************************************************************************************/
    /************************************** Black table ***************************************/
    private static final String TBL_Black = "Black_T";

    private static final String KEY_blackkey = "black_key";

    private static String SQL_CREATE_BLACK = "CREATE TABLE IF NOT EXISTS " + TBL_Black
            + "(" +
            " " + KEY_blackkey + " INTEGER PRIMARY KEY ," +
            " " + KEY_useraddr + " LONG," +
            " " + KEY_username + " TEXT" +
            ")";

    private static final String SQL_DROP_BLACK = "DROP TABLE IF EXISTS " + TBL_Black;
    public static final String SQL_SELECT_BLACK = "SELECT * FROM " + TBL_Black;
    public static final String SQL_DELETE_BLACK = "DELETE FROM " + TBL_Black;
    public static final String SQL_DELETE_CHAT_BLACK = "DELETE FROM " + TBL_Black + " WHERE ";

    /******************************************************************************************/
    /************************************** Set table ***************************************/
    private static final String TBL_Set = "Set_T";

    private static final String KEY_set = "set_set";
    private static final String KEY_set2 = "set_set2";

    private static final int pushMSK = 0x00800000;
    private static final int vibeMSK = 0x00400000;
    private static final int soundMSK = 0x00200000;
    private static final int dclvMSK = 0x001E0000;
    private static final int blueonMSK = 0x00010000;
    private static final int bdMSK1 = 0x0000FFFF;
    private static final int bdMSK2 = 0xFFFFFFFF;

    private static String SQL_CREATE_SET = "CREATE TABLE IF NOT EXISTS " + TBL_Set
            + "(" +
            " " + KEY_set + " INTEGER," +
            " " + KEY_set2 + " INTEGER " +
            ")";

    private static final String SQL_DROP_SET = "DROP TABLE IF EXISTS " + TBL_Set;
    private static final String SQL_REPLACE_SET = "INSERT OR REPLACE INTO " + TBL_Set;
    private static final String SQL_SELECT_SET = "SELECT * FROM " + TBL_Set;
    private static final String SQL_DELETE_SET = "DELETE FROM " + TBL_Set;
    private static final String SQL_DELETE_SET_WHERE = "DELETE FROM " + TBL_Set + " WHERE ";

    /******************************************************************************************/
    C_DB(Context context) {
        super(context, DBFILE, null, DB_VERSION);
    }

    //최초 DB를 만들 때 한번만 호출
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NET);
        db.execSQL(SQL_CREATE_LIST);
        db.execSQL(SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_CHAT);
        db.execSQL(SQL_CREATE_BLACK);
        db.execSQL(SQL_CREATE_SET);
    }

    // 버전이 업데이트 되었을 때 다시 만들어주는 메소드
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_NET);
        db.execSQL(SQL_DROP_LIST);
        db.execSQL(SQL_DROP_USER);
        db.execSQL(SQL_DROP_CHAT);
        db.execSQL(SQL_DROP_BLACK);
        db.execSQL(SQL_DROP_SET);

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //    onUpgrade(db,newVersion,oldVersion);
    }


    /** *** **** **** **** **** **** **** **** ** Net table 메소드 * ******* **** **** **** **** **** **** **** **** **/

    /**
     * Net table 에서 현재 채널(netcheck=1)을 현재 채널이 아니도록 설정하고 선택된 netkey의 net을 현재채널로 설정
     **/
    public void set_net(int check) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TBL_Net + " SET " + KEY_netcheck + "=0" + " WHERE " + KEY_netcheck + "=1");
        db.execSQL("UPDATE " + TBL_Net + " SET " + KEY_netcheck + "= 1" + " WHERE " + KEY_netkey + "=" + check);
        db.close();
    }


    /**
     * Net table에 데이터 저장
     **/
    public int save_net(int id, String name) {
        ContentValues values = new ContentValues();
        values.put(KEY_netid, id);
        values.put(KEY_netname, name);
        values.put(KEY_netcheck, 0);

        int key = -1;
        SQLiteDatabase db = getWritableDatabase();
        if (db.insert(TBL_Net, null, values) > 0) {
            Cursor c = db.rawQuery(SQL_SELECT_NET, null);
            c.moveToLast();
            key = c.getInt(0);
        }
        db.close();
        return key;
    }


    Cursor get_net_cursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_NET, null);
    }

    int get_net_ch(int key) {
        SQLiteDatabase db = getReadableDatabase();
        int ch = -1;
        Cursor c = db.rawQuery(SQL_SELECT_NET + " WHERE " + KEY_netkey + " = " + key, null);
        if (c.moveToFirst()) {
            ch = c.getInt(1);
        }
        db.close();

        return ch;
    }


    /**
     * 현재 채널(netcheck=1)을 담은 커서 리턴
     **/
    Cursor get_net_Current() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_NET + " WHERE " + KEY_netcheck + "=1", null);
    }


    int get_net_Current_key() {
        int ch = 0;
        Cursor c = get_net_Current();
        if (c.moveToFirst()) {
            ch = c.getInt(0);
        }
        c.close();
        return ch;
    }

    int get_net_Current_ch() {
        int ch = 0;
        Cursor c = get_net_Current();
        if (c.moveToFirst()) {
            ch = c.getInt(1);
        }
        c.close();

        return ch;
    }


    /**
     * 채널 address를 받아서 채널이 이미 존재하는지 아닌지 검출
     */
    int get_channel_exists(int ch) {
        int A = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_NET + " WHERE " + KEY_netid + " = " + ch, null);
        if (cursor.moveToFirst()) {
            A = 1;
        }
        db.close();
        return A;
    }

    public void delete_net_All() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_NET);
        db.close();
    }


    /** *** **** **** **** **** **** **** **** ******* **** **** *** **** **** **** ******* **** **** **** **** **** **** **** **** **/


    /** *** **** **** **** **** **** **** **** ** User table 메소드 * ******* **** **** **** **** **** **** **** **** **/
    /**
     * User table에 데이터 저장
     **/
    public int save_userMY(String name, long addr) {
        ContentValues values = new ContentValues();
        values.put(KEY_userkey, 0);
        values.put(KEY_username, name);
        values.put(KEY_useraddr, addr); // addr 데이터형 추후 협의

        int key = -1;
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_USER_WHERE + " " + KEY_userkey + "= 0");
        if (db.insert(TBL_User, null, values) > 0) {
            Cursor c = db.rawQuery(SQL_SELECT_USER, null);
            c.moveToLast();
            key = c.getInt(0);
        }
        db.close();

        return key;
    }

    /**
     * 유저 저장.
     * 이름과 addr 입력 addr가 블랙에 있으면 -2 리턴
     */
    public int save_user(String name, long addr) {
        ContentValues values = new ContentValues();
        values.put(KEY_username, name);
        values.put(KEY_useraddr, addr);
        int key = -1;
        Log.d("addr_saveuser", String.valueOf(addr));

        if (get_isblack(addr) != 1) {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL(SQL_DELETE_USER_WHERE + " " + KEY_useraddr + " = " + addr);
            if (db.insert(TBL_User, null, values) > 0) {
                Cursor c = db.rawQuery(SQL_SELECT_USER, null);
                c.moveToLast();
                key = c.getInt(0);
                c.close();
            }
        } else {
            key = -2;
        }
        return key;
    }

    Cursor get_user_cursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_USER + " WHERE " + KEY_userkey + " != 0", null);
    }

    /**
     */
    long get_user_addr(int user_key) {
        SQLiteDatabase db = getReadableDatabase();
        long addr = -2;
        Log.d("MSGMSG: lstgetUserAddr","user_key = "+user_key);
        Cursor c = db.rawQuery(SQL_SELECT_USER + " WHERE " + KEY_userkey + "= " + user_key, null);
        if (c.moveToFirst()) {
            addr = c.getLong(2);
        }
        db.close();
        return addr;
    }

    int get_user_num() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor C = db.rawQuery(SQL_SELECT_USER, null);
        int num = 0;
        if (C.moveToFirst()) {
            num++;
        }
        db.close();
        return num;
    }

    int get_user_key(long mac_addr) {
        SQLiteDatabase db = getReadableDatabase();
        int key = -1;
        Cursor c = db.rawQuery(SQL_SELECT_USER + " WHERE " + KEY_useraddr + " = " + mac_addr + "", null);
        if (c.moveToFirst())
            key = c.getInt(0);
        db.close();
        return key;
    }


    String get_user_Myname() {
        String name;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_USER + " WHERE " + KEY_userkey + " = 0", null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(1);
        } else {
            name = "(설정되지 않음)";
        }
        db.close();
        return name;
    }


    String get_user_name(int key) {
        String name;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_USER + " WHERE " + KEY_userkey + " = " + key, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(1);
        } else {
            name = "(등록되지 않은 유저)";
        }
        db.close();
        return name;
    }


    /**
     * 유저이름 변경, 같은 유저 키를 공유하는 채팅방 이름이 유저이름과 같으면 채팅방이름도 변경시켜줌
     **/
    public void update_user(String name, int key) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor join = db.rawQuery("SELECT " + TBL_List + "." + KEY_roomkey + "," + TBL_List + "." + KEY_roomname + ", " + TBL_User + "." + KEY_username + " FROM " + TBL_List + " INNER JOIN " + TBL_User + " ON " + TBL_List + "." + KEY_userkey + "=" + TBL_User + "." + KEY_userkey, null);
        if (join.moveToFirst()) {
            do {
                if (join.getString(1).equals(join.getString(2))) {
                    db.execSQL("UPDATE " + TBL_List + " SET " + KEY_roomname + " ='" + name + "' WHERE " + KEY_roomkey + "=" + join.getInt(0) + " AND " + KEY_userkey + "= " + key);
                }
            } while (join.moveToNext());
        }
        db.execSQL("UPDATE " + TBL_User + " SET " + KEY_username + " = '" + name + "' WHERE " + KEY_userkey + "= " + key);
        db.execSQL("UPDATE " + TBL_Chat + " SET " + KEY_username + " = '" + name + "' WHERE " + KEY_userkey + "= " + key);
    }

    public void delete_user_All() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_USER_WHERE + " " + KEY_userkey + " != " + 0);
    }

    public void delete_user(int key) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_USER_WHERE + " " + KEY_userkey + " = " + key);
    }
    /** *** **** **** **** **** **** **** **** ******* **** **** *** **** **** **** ******* **** **** **** **** **** **** **** **** **/


    /** *** **** **** **** **** **** **** **** ** Chat list table 메소드 * ******* **** **** **** **** **** **** **** **** **/
    /**
     * List table에 public 대화방 데이터 저장
     **/
    public int save_list_public() {
        ContentValues values = new ContentValues();
        int current_netkey = 0;
        int current_netaddr = 0;
        Cursor c = get_net_Current();
        if (c.moveToFirst()) {
            current_netkey = c.getInt(0);
            current_netaddr = c.getInt(1);
        }
        values.put(KEY_netkey, current_netkey);// net key
        values.put(KEY_roomname, current_netaddr + " 채널의 공개 채팅방");
        values.put(KEY_roomkey, 0);
        values.put(KEY_receivekey, 0);

        int key = -1;
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_LIST_WHERE + " " + KEY_roomkey + " = " + 0);

        if (db.insert(TBL_List, null, values) > 0)
            key = 0;
        db.close();
        return key;
    }

    /**
     * List table에 private 대화방 데이터 저장후 자신의 룸 키 반환
     * user key는 디스커버마다 바뀌니까 디스커버마다 유저어드레스랑 유저 키 동기화
     */
    public int save_list_private(String name, int userkey) {
        ContentValues values = new ContentValues();
        int current_netkey = -1;
        Cursor c = get_net_Current();
        if (c.moveToFirst()) {
            current_netkey = c.getInt(0);
        }
       long useraddr = get_user_addr(userkey);
        values.put(KEY_netkey, current_netkey);// net key
        values.put(KEY_roomname, name);
        values.put(KEY_userkey, userkey);
        values.put(KEY_useraddr, useraddr);
        values.put(KEY_receivekey, 0);

        int key = -1;
        SQLiteDatabase db = getWritableDatabase();
        if (db.insert(TBL_List, null, values) > 0) {
            c = get_list_cursor();
            c.moveToLast();
            key = c.getInt(1);
        }
        db.close();
        return key;
    }// update 메소드 정의 해야함

    /**
     * 메시지를 받았을때 받은메시지의 send/ receive 순서대로 넣어주기
     **/
    public void save_list_recievekey(int sendkey, int receivekey) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TBL_List + " SET " + KEY_receivekey + " = " + sendkey + " WHERE " + KEY_roomkey + " = " + receivekey);
        Log.d("DB  :", "save list receive key : " + sendkey);
        db.close();
    }

    /************** 나중에 다시 디버깅 **********/////////////
    public int get_list_receivekey(int sendkey) {
        SQLiteDatabase db = getReadableDatabase();
        int current_netkey = 0;
        Cursor c = get_net_Current();
        if (c.moveToFirst()) {
            current_netkey = c.getInt(0);
        }
        c = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_netkey + " = " + current_netkey + " AND " + KEY_roomkey + " = " + sendkey, null);
        int key = 0;
        if (c.moveToNext()) {
            key = c.getInt(5);
        }
        Log.d("DB  :", "get list receive key : " + key);
        c.close();
        return key;
    }

    Cursor get_list_cursor() {
        SQLiteDatabase db = getReadableDatabase();
        int current_netkey = 0;
        Cursor c = get_net_Current();
        if (c.moveToFirst()) {
            current_netkey = c.getInt(0);
        }
        return db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_netkey + " = " + current_netkey, null);
    }


    String get_list_name(int key) {
        String name = "";
        SQLiteDatabase db = getReadableDatabase();
        int current_netkey = 0;
        Cursor c = get_net_Current();
        if (c.moveToFirst()) {
            current_netkey = c.getInt(0);
        }
        Cursor cursor = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_netkey + " = " + current_netkey + " AND " + KEY_roomkey + " = " + key, null);
        if (cursor.moveToFirst())
            name = cursor.getString(2);

        db.close();
        return name;
    }

    String get_list_userName(int key) {
        String name = "";
        SQLiteDatabase db = getReadableDatabase();
        Cursor join = db.rawQuery("SELECT " + TBL_User + "." + KEY_username + " FROM " + TBL_User + " INNER JOIN " + TBL_List + " ON " + TBL_List + "." + KEY_userkey + "=" + TBL_User + "." + KEY_userkey, null);
        if (join.moveToFirst())
            name = join.getString(0);
        db.close();
        return name;
    }

    /**
     * 디스커버 메소드에서 사용
     * 디스커버가 될 떄마다 채팅방의 유저키-유저 어드레스를 지금 생성된 유저테이블의 유저키-유저 어드레스와 동기화
     * 디스커버 -> 유저 싹다지워지고 유저키-유저어드레스 생성 ,
     * -> 이 메소드실행. 채팅방 키 , 유저탭 유저 키 찾아냄 (같은 유저 어드레스를 가진)
     * 채팅방 유저키 = 유저 유저키
     */
    public void update_list_user() {
        int key;
        int room;
        SQLiteDatabase db = getWritableDatabase();
        Cursor join = db.rawQuery("SELECT " + TBL_List + "." + KEY_roomkey + ", " + TBL_User + "." + KEY_userkey + " FROM " + TBL_List + " INNER JOIN " + TBL_User + " ON " + TBL_List + "." + KEY_useraddr + "=" + TBL_User + "." + KEY_useraddr, null);
        if (join.moveToFirst())
            do {
                room = join.getInt(0);
                key = join.getInt(1);
                db.execSQL("UPDATE " + TBL_List + " SET " + KEY_userkey + " = " + key + " WHERE " + KEY_roomkey + " = " + room, null);
            } while (join.moveToNext());
    }

    /*** 메시지 receive시 사용
     * 받은 메시지에 대해  user mac 일치 검사
     * 채팅방 있는 경우 roomkey
     * 채팅방 없는 경우 생성한 후 리턴
     * */
    public int echo_room_key(String username, int user_key, long useraddr) {
        int room_ch = -1;
        int room_key = -1;
        int ch_current = get_net_Current_key();


        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_useraddr + " = " + useraddr, null);

        if (c.moveToFirst()) {
            room_ch= c.getInt(0);
            room_key = c.getInt(1);
        }

        if(room_key <0)
            room_key = save_list_private(username, user_key);

        db.close();

        return room_key;
    }

    /**
     * 채팅방 설정에서 채팅방 이름 변경
     */
    public void update_list_name(String _roomname, int _roomkey) {
        SQLiteDatabase db = getWritableDatabase();
        int current_netkey = 0;
        Cursor c = get_net_Current();
        if (c.moveToFirst()) {
            current_netkey = c.getInt(0);
            db.execSQL("UPDATE " + TBL_List + " SET " + KEY_roomname + " = " + _roomname + " WHERE " + KEY_netkey + " = " + current_netkey + " AND " + KEY_roomkey + " = " + _roomkey);
        }
        c.close();
    }

    public void delete_list(int key) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_LIST_WHERE + " " + KEY_roomkey + " = " + key);
        db.execSQL(SQL_DELETE_CHAT_WHERE + " " + KEY_roomkey + "=" + key);

        db.close();
    }

    public void delete_list_All() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_LIST);
        db.close();
    }

    /** *** **** **** **** **** **** **** **** ******* **** **** *** **** **** **** ******* **** **** **** **** **** **** **** **** **/


    /** *** **** **** **** **** **** **** **** ** Chat table 메소드 * ******* **** **** **** **** **** **** **** **** **/
    /**
     * Chat table에 데이터 저장
     **/
    public int save_chatmsg(int net_key, int room_key, String user_name, String msg, boolean sORr, int user_key) {
        ContentValues values = new ContentValues();
        values.put(KEY_netkey, net_key);
        values.put(KEY_roomkey, room_key);
        if (user_name.equals(""))
            values.put(KEY_username, "(저장하는 곳에서 유저 이름 입력 안함)");
        else
            values.put(KEY_username, user_name);

        values.put(KEY_chatmsg, msg);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        /**현재 시간을 삽입**/
        DateFormat sdFormat = new SimpleDateFormat("aKK:mm:ss");
        String time = sdFormat.format(date);
        values.put(KEY_chattime, time);
        values.put(KEY_chatsORr, sORr);
        values.put(KEY_userkey, user_key);

        int key = -1;
        SQLiteDatabase db = getWritableDatabase();
        if (db.insert(TBL_Chat, null, values) > 0) {
            Cursor c = get_chat_cursor(net_key, room_key);
            c.moveToLast();
            key = c.getInt(7);
        }

        Log.d("DBsavechat", "key::" + String.valueOf(key) + ":" + msg + "channel :" + net_key + "room :" + room_key);

        db.close();
        return key;
    }

    public String get_chat_time(int channel, int room, int key) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(SQL_SELECT_CHAT + " WHERE " + KEY_netkey + "= " + channel + " AND " + KEY_roomkey + " = " + room + " AND " + KEY_chatkey + " = " + key, null);
        String time = "";
        if (c.moveToFirst()) {
            time = c.getString(4);
        }
        db.close();
        return time;
    }

    Cursor get_chat_cursor(int channel, int room) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_CHAT + " WHERE " + KEY_netkey + "= " + channel + " AND " + KEY_roomkey + " = " + room, null);
    }

    Cursor get_chat_cusorLast(int channel, int room, int key) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_CHAT + " WHERE " + KEY_netkey + "= " + channel + " AND " + KEY_roomkey + " = " + room + " AND " + KEY_chatkey + " = " + key, null);
    }


    /**
     * 임시함수
     **/
    public void delete_chat() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_CHAT);
        db.close();
    }

    public void delete_net(int key) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_NET_WHERE + " " + KEY_netkey + "=" + key);
        db.execSQL(SQL_DELETE_LIST_WHERE + " " + KEY_netkey + "=" + key);
        db.execSQL(SQL_DELETE_CHAT_WHERE + " " + KEY_netkey + "=" + key);
        db.close();
    }

    /** *** **** **** **** **** **** **** **** ******* **** **** *** **** **** **** ******* **** **** **** **** **** **** **** **** **/


    /** *** **** **** **** **** **** **** **** ** Black table 메소드 * ******* **** **** **** **** **** **** **** **** **/

    /**
     * 블랙 리스트에 저장. 유저 리스트에서 삭제
     */
    public int save_black(int user_key) {
        long addr = get_user_addr(user_key);
        String name = get_user_name(user_key);

        ContentValues values = new ContentValues();
        values.put(KEY_useraddr, addr);
        values.put(KEY_username, name);

        int black_key = -1;
        SQLiteDatabase db = getWritableDatabase();
        if (db.insert(TBL_Black, null, values) > 0) {
            Cursor c = get_black_cursor();
            c.moveToLast();
            black_key = c.getInt(0);
            delete_user(user_key);
        }

        db.close();
        return black_key;
    }

    Cursor get_black_cursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_BLACK, null);
    }

    /**
     * ㅊ차ㅏ단이면 1 리턴
     **/
    public int get_isblack(long addr) {
        int isblack = 0;
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(SQL_SELECT_BLACK + " WHERE " + KEY_useraddr + " = " + addr + "", null);
        if (c.moveToFirst()) {
            isblack = 1;
        }
        db.close();
        return isblack;
    }

    long get_black_addr(int black_key) {
        long addr = -1;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(SQL_SELECT_BLACK + " WHERE " + KEY_blackkey + " = " + black_key, null);
        if (c.moveToFirst()) {
            addr = c.getLong(1);
        }
        db.close();
        return addr;
    }

    String get_black_name(int black_key) {
        String name = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(SQL_SELECT_BLACK + " WHERE " + KEY_blackkey + " = " + black_key, null);
        if (c.moveToFirst()) {
            name = c.getString(2);
        }
        db.close();
        return name;
    }

    /**
     * 블랙리스트에서 삭제, 채널에 없을수도 있기 때문에 바로 세이브는 ㄴㄴ(디스커버하라는 메시지 띄우기)
     **/
    public void delete_black(int black_key) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_BLACK + " WHERE " + KEY_blackkey + " = " + black_key);
        db.close();
    }

    public void delete_black_all() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_BLACK);
        db.close();
    }

    /** *** **** **** **** **** **** **** **** ******* **** **** *** **** **** **** ******* **** **** **** **** **** **** **** **** **/


    /**
     * ** **** **** **** **** **** **** **** ** Set table 메소드 * ******* **** **** **** **** **** **** **** ****
     **/


    public void save_set_setting(int set1, int set2) {

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_SET);

        ContentValues values = new ContentValues();
        values.put(KEY_set, set1);
        values.put(KEY_set2, set2);
        db.insert(TBL_Set, null, values);
        db.close();
    }

    public void save_push(int onoff) {
        int a = get_setint(0);
        int b = get_setint(1);
        if (onoff == 1)   // on = 1
            a = a | pushMSK;
        else
            a = a & ~(pushMSK);
        save_set_setting(a, b);
    }

    public void save_vibe(int onoff) {
        int a = get_setint(0);
        int b = get_setint(1);
        if (onoff == 1)
            a = a | vibeMSK;
        else
            a = a & ~(vibeMSK);
        save_set_setting(a, b);
    }

    public void save_sound(int onoff) {
        int a = get_setint(0);
        int b = get_setint(1);
        if (onoff == 1)
            a = a | soundMSK;
        else
            a = a & ~(soundMSK);
        save_set_setting(a, b);
    }

    public void save_dvlv(int dclv) {
        int a = get_setint(0);
        int b = get_setint(1);

        a = a & ~(dclvMSK);
        a = a | (dclv << 17);

        save_set_setting(a, b);
    }

    public void save_blueon(int onoff) {
        int a = get_setint(0);
        int b = get_setint(1);
        if (onoff == 1)
            a = a | blueonMSK;
        else
            a = a & ~(blueonMSK);
        save_set_setting(a, b);
    }

    public void save_bd(byte[] bd) {
        int bd1 = 0;
        bd1 |= (0xff&bd[0]) << 8;
        bd1 |= (0xff&bd[1]);
        int bd2 = 0;
        bd2 |= (0xff&bd[2]) << 24;
        bd2 |= (0xff&bd[3]) << 16;
        bd2 |= (0xff&bd[4]) << 8;
        bd2 |= (0xff&bd[5]);

        int a = get_setint(0);
        int b = 0;
        a = a & ~(bdMSK1);
        a |= bdMSK1&bd1;
        b |= bdMSK2&bd2;

        save_set_setting(a, b);
    }


    public int get_setint(int i) {
        Cursor c = get_set_cursor();
        int r = 0;
        switch (i) {
            case 0:
                r = 0x00C90000; // default = 0000 0000 111 0 100 1 000...
                break;
            case 1:
                r = 0; // default = 0
                break;
        }
        if (c.moveToFirst()) {
            r = c.getInt(i);
        }
        return r;
    }

    public Cursor get_set_cursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_SET, null);
    }


    public boolean get_set_push() {

        Cursor c = get_set_cursor();
        boolean set = true;
        if (c.moveToFirst()) {
            if ((c.getInt(0) & pushMSK) >> 23 == 1) {
                set = true;
            }
            else
                set = false;
        }
        return set;
    }

    public boolean get_set_vibe() {
        Cursor c = get_set_cursor();
        boolean set = true;
        if (c.moveToFirst()) {
            if ((c.getInt(0) & vibeMSK) >> 22 == 1)
                set = true;
            else
                set = false;
        }

        return set;
    }


    public boolean get_set_sound() {
        Cursor c = get_set_cursor();
        boolean set = true;
        if (c.moveToFirst()) {
            if ((c.getInt(0) & soundMSK) >> 21 == 1)
                set = true;
            else
                set = false;
        }

        return set;
    }

    public int get_set_dclv() {
        Cursor c = get_set_cursor();

        int set = 4;
        if (c.moveToFirst()) {
            set = (c.getInt(0) & dclvMSK) >> 17;
        }

        return set;
    }

    public boolean get_set_blueOn() {
        Cursor c = get_set_cursor();

        boolean set = true;
        if (c.moveToFirst()) {
            if ((c.getInt(0) & blueonMSK) >> 16 == 1)
                set = true;
            else
                set = false;
        }

        return set;
    }

    public byte[] get_set_bd() {
        Cursor c = get_set_cursor();
        byte[] bd = new byte[6];
        if (c.moveToFirst()) {
            bd[0] = (byte) ((c.getInt(0) & bdMSK1) >> 8);
            bd[1] = (byte) (c.getInt(0) & bdMSK1);
            bd[2] = (byte) (c.getInt(1) >> 24);
            bd[3] = (byte) (c.getInt(1) >> 16);
            bd[4] = (byte) (c.getInt(1) >> 8);
            bd[5] = (byte) c.getInt(1);
        }
        return bd;
    }


}