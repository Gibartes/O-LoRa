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
    private static final int DB_VERSION = 31;
    /************************************* Net table ****************************************/
    private static final String TBL_Ch = "Ch_T";

    private static final String KEY_channel = "ch_channel";    // net address
    private static final String KEY_chname = "ch_name";
    private static final String KEY_chcheck = "ch_check";

    private static String SQL_CREATE_NET = "CREATE TABLE IF NOT EXISTS " + TBL_Ch
            + "(" +
            " " + KEY_channel + " INTEGER, " +
            " " + KEY_chname + " TEXT, " +
            " " + KEY_chcheck + " INTEGER" +
            ")";
    private static final String SQL_DROP_CH = "DROP TABLE IF EXISTS " + TBL_Ch;
    private static final String SQL_SELECT_CH = "SELECT * FROM " + TBL_Ch;
    private static final String SQL_DELETE_CH = "DELETE FROM " + TBL_Ch;
    private static final String SQL_DELETE_CH_WHERE = "DELETE FROM " + TBL_Ch + " WHERE ";
    /************************************** User table ***************************************/
    private static final String TBL_User = "User_T";

    private static final String KEY_userkey = "user_key";
    private static final String KEY_username = "user_name";
    private static final String KEY_useraddr = "user_addr"; // mac address
    private static final String KEY_userOname = "user_origin_name";

    private static String SQL_CREATE_USER = "CREATE TABLE IF NOT EXISTS " + TBL_User
            + "(" +
            " " + KEY_userkey + " INTEGER PRIMARY KEY, " +
            " " + KEY_username + " TEXT, " +
            " " + KEY_useraddr + " LONG," +
            " " + KEY_userOname + " TEXT" +
            ")";
    private static final String SQL_DROP_USER = "DROP TABLE IF EXISTS " + TBL_User;
    public static final String SQL_SELECT_USER = "SELECT * FROM " + TBL_User;
    public static final String SQL_DELETE_USER = "DELETE FROM " + TBL_User;
    public static final String SQL_DELETE_USER_WHERE = "DELETE FROM " + TBL_User + " WHERE ";
    /************************************** List table ***************************************/
    private static final String TBL_List = "List_T";

    private static final String KEY_roomkey = "room_key";
    private static final String KEY_roomname = "room_name";
    private static final String KEY_nonReadMsg = "room_nonReadMsg";

    private static String SQL_CREATE_LIST = "CREATE TABLE IF NOT EXISTS " + TBL_List
            + "(" +
            " " + KEY_channel + " INTEGER, " +
            " " + KEY_roomkey + " INTEGER PRIMARY KEY, " +
            " " + KEY_roomname + " TEXT, " +
            " " + KEY_userkey + " INTEGER, " +
            " " + KEY_useraddr + " LONG, " +
            " " + KEY_nonReadMsg + " INTEGER" +
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
            " " + KEY_channel + " INTEGER, " +
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
    private static final int def_set = pushMSK+vibeMSK+soundMSK+dclvMSK;

    private static String SQL_CREATE_SET = "CREATE TABLE IF NOT EXISTS " + TBL_Set
            + "(" +
            " " + KEY_set + " INTEGER DEFAULT " + def_set+
            ", " + KEY_set2 + " INTEGER " +
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
        db.execSQL(SQL_DROP_CH);
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


    /**
     * ** **** **** **** **** **** **** **** ** Net table 메소드 * ******* **** **** **** **** **** **** **** ****
     **/

    Cursor get_ch_cursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_CH, null);
    }

    /**
     * 현재 채널(chcheck=1)을 담은 커서 리턴
     **/
    Cursor get_ch_cursor_Current() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_CH + " WHERE " + KEY_chcheck + "=1", null);
    }

    /**
     * CH table 에서 현재 채널(chcheck=1)을 현재 채널이 아니도록 설정하고 선택된 netkey의 net을 현재채널로 설정
     **/
    public void set_current_ch(int ch) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TBL_Ch + " SET " + KEY_chcheck + "=0" + " WHERE " + KEY_chcheck + "=1");
        db.execSQL("UPDATE " + TBL_Ch + " SET " + KEY_chcheck + "= 1" + " WHERE " + KEY_channel + "=" + ch);
        db.close();
    }


    /**
     * CH table에 데이터 저장
     **/
    public int save_ch(int ch, String name) {
        ContentValues values = new ContentValues();
        values.put(KEY_channel, ch);
        values.put(KEY_chname, name);
        values.put(KEY_chcheck, 0);

        int ret = -1;
        SQLiteDatabase db = getWritableDatabase();
        if (db.insert(TBL_Ch, null, values) > 0) {
            Cursor c = db.rawQuery(SQL_SELECT_CH, null);
            c.moveToLast();
            ret = c.getInt(0);
        }
        db.close();
        return ret;
    }

    int get_ch_Current() {
        int ch = -1;
        Cursor c = get_ch_cursor_Current();
        if (c.moveToFirst()) {
            ch = c.getInt(0);
        }
        c.close();
        return ch;
    }

    /**
     * 채널을 받아서 채널이 이미 존재하는지 아닌지 검출
     */
    int get_channel_exists(int ch) {
        int A = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_CH + " WHERE " + KEY_channel + " = " + ch, null);
        if (cursor.moveToFirst()) {
            A = 1;
        }
        db.close();
        return A;
    }

    public void delete_ch_All() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_CH);
        db.close();
    }


    /** *** **** **** **** **** **** **** **** ******* **** **** *** **** **** **** ******* **** **** **** **** **** **** **** **** **/


    /**
     * ** **** **** **** **** **** **** **** ** User table 메소드 * ******* **** **** **** **** **** **** **** ****
     **/
    Cursor get_user_cursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_USER + " WHERE " + KEY_userkey + " != 0", null);
    }

    /**
     * User table에 데이터 저장
     **/
    public int save_userMY(String name, long addr) {
        ContentValues values = new ContentValues();
        values.put(KEY_userkey, 0);
        values.put(KEY_username, name);
        values.put(KEY_useraddr, addr);
        values.put(KEY_userOname, name);

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
        values.put(KEY_userOname, name);
        int key = -1;

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

    /**
     */
    long get_user_addr(int user_key) {
        SQLiteDatabase db = getReadableDatabase();
        long addr = -2;
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
            name = "(이름 설정되지 않음)";
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

    String get_user_origin_name(int key) {
        String name;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_USER + " WHERE " + KEY_userkey + " = " + key, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(3);
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
        int current_ch = get_ch_Current();
        /*
        Cursor c = get_ch_cursor_Current();
        if (c.moveToFirst()) {
            current_ch = c.getInt(0);
        }
        */
        values.put(KEY_channel, current_ch);
        values.put(KEY_roomname, current_ch + " 채널의 전체 채팅방");
        values.put(KEY_roomkey, 0);
        values.put(KEY_nonReadMsg, 0);

        int key = -1;
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_LIST_WHERE + " " + KEY_roomkey + " = " + 0);

        if (db.insert(TBL_List, null, values) > 0)
            key = 0;
        db.close();
        return key;
    }


    /**
     * user addr를 받아서 해당 키로 만들어진 채팅방의 채널을
     * 현재 채널로 변경 후 채팅방 키 반환 없으면 -1 반환
     */
    public int change_room_ch(long useraddr) {
        int ret = -1;
        int cur_ch = get_ch_Current();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_useraddr + " = " + useraddr, null);
        if (c.moveToFirst()) {
            ret = c.getInt(1);
            db.execSQL("UPDATE " + TBL_List + " SET " + KEY_channel + " = " + cur_ch + " WHERE " + KEY_useraddr + " = " + useraddr);
        } else {
        }
        //db.close();
        return ret;
    }

    /**
     * List table에 private 대화방 데이터 저장후 자신의 룸 키 반환
     * user key는 디스커버마다 바뀌니까 디스커버마다 유저어드레스랑 유저 키 동기화
     */

    public int save_list_private(String name, int userkey) {
        ContentValues values = new ContentValues();
        int current_ch = -1;
        int key = -1;
        long useraddr = get_user_addr(userkey);

        Cursor c = get_ch_cursor_Current();
        if (c.moveToFirst()) {
            key = change_room_ch(useraddr);
            if (0 > key) {
                current_ch = c.getInt(0);
                values.put(KEY_channel, current_ch);// net key
                values.put(KEY_roomname, name);
                values.put(KEY_userkey, userkey);
                values.put(KEY_useraddr, useraddr);
                values.put(KEY_nonReadMsg, 0);
                SQLiteDatabase db = getWritableDatabase();
                if (db.insert(TBL_List, null, values) > 0) {
                    c = get_list_cursor();
                    c.moveToLast();
                    key = c.getInt(1);
                }
                db.close();
            }
        } else {
            // dummy chatting - mklee
            // 원래는 '현재 채널'이 설정되지 않았으므로 방 만들어지지 않아야함
            key = change_room_ch(useraddr);
            if (0 > key) {
                values.put(KEY_channel, 99);// net key
                values.put(KEY_roomname, name);
                values.put(KEY_userkey, userkey);
                values.put(KEY_useraddr, useraddr);
                values.put(KEY_nonReadMsg, 0);

                SQLiteDatabase db = getWritableDatabase();
                if (db.insert(TBL_List, null, values) > 0) {
                    c = get_list_cursor();
                    c.moveToLast();
                    key = -1;
                }
                db.close();
            }
        }

        return key;
    }// update 메소드 정의 해야함

    /************** 나중에 다시 디버깅 **********/

    Cursor get_list_cursor() {
        SQLiteDatabase db = getReadableDatabase();

        int current_ch = 0;
        Cursor c = get_ch_cursor_Current();
        if (c.moveToFirst()) {
            current_ch = c.getInt(0);
        }
        return db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_channel + " = " + current_ch, null);
    }

    Cursor get_all_list_cursor() {
        /*모든 리스트--mklee*/
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_LIST, null);
    }

    int get_list_ch(int key) {
        int channel = -1;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_roomkey + " = " + key, null);
        if (cursor.moveToFirst())
            channel = cursor.getInt(0);

        db.close();
        return channel;
    }

    String get_list_name(int key) {
        String name = "";
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_roomkey + " = " + key, null);
        if (cursor.moveToFirst())
            name = cursor.getString(2);

        db.close();
        return name;
    }

    String get_user_name_inList(int roomKey) {
        int key = -1;
        String name = "";
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_roomkey + " = " + roomKey, null);
        if (cursor.moveToFirst())
            key = cursor.getInt(3);
        else
            Log.e("DB error", "get_user_name_inList_cannot find a room");

        Cursor cursor2 = db.rawQuery(SQL_SELECT_USER + " WHERE " + KEY_userkey + " = " + key, null);
        if (cursor2.moveToFirst())
            name = cursor2.getString(1);
        else
            Log.e("DB error", "get_user_name_inList_cannot find a room");
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
                db.execSQL("UPDATE " + TBL_List + " SET " + KEY_userkey + " = " + key + " WHERE " + KEY_roomkey + " = " + room);
            } while (join.moveToNext());
    }

    /*** 메시지 receive시 사용
     * 받은 메시지에 대해  user mac 일치 검사
     *
     * 채팅방 없는 경우 생성
     * 채팅방 있는 경우 채널이 같은지 검사해서 채널이 다르면 현재 체널로 업뎃하고
     * roomkey 리턴
     * */
    public int echo_room_key(String username, int user_key, long useraddr, int ch_current) {
        int room_ch = -1;
        int room_key = -1;


        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_useraddr + " = " + useraddr, null);

        if (c.moveToFirst()) {
            room_ch = c.getInt(0);
            room_key = c.getInt(1);
        }

        if (room_key < 0) {
            room_key = save_list_private(username, user_key);
        } else if (room_ch != ch_current) {
            db.execSQL("UPDATE " + TBL_List + " SET " + KEY_channel + " = '" + ch_current + "'" +
                    " WHERE " + KEY_useraddr + " = " + useraddr);
        }
        db.close();

        return room_key;
    }

    /**
     * 채팅방 설정에서 채팅방 이름 변경
     */
    public void update_list_name(String _roomname, int _roomkey) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TBL_List + " SET " + KEY_roomname + " = '" + _roomname + "'" +
                " WHERE " + KEY_roomkey + " = " + _roomkey);
        db.close();
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


    public void update_nonReadMsg(int curr_ch, int room_key) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c;
        int nonRead = 0;
        if (room_key == 0)
            c = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_roomkey + " = " + room_key + " AND " + KEY_channel + " = " + curr_ch, null);
        else
            c = db.rawQuery(SQL_SELECT_LIST + " WHERE " + KEY_roomkey + " = " + room_key, null);

        if (c.moveToFirst()) {
            nonRead = c.getInt(5);
        }

        nonRead=nonRead+1;
        Log.d("sibbal", "nonRead: "+nonRead);

        if (room_key == 0)
            db.execSQL("UPDATE " + TBL_List + " SET " + KEY_nonReadMsg + " = " + nonRead +
                    " WHERE " + KEY_roomkey + " = " + room_key + " AND " + KEY_channel + " = " + curr_ch);
        else
            db.execSQL("UPDATE " + TBL_List + " SET " + KEY_nonReadMsg + " = " + nonRead +
                    " WHERE " + KEY_roomkey + " = " + room_key);
        db.close();
    }

    public void reset_nonReadMsg(int curr_ch, int room_key) {
        SQLiteDatabase db = getWritableDatabase();
        if (room_key == 0)
            db.execSQL("UPDATE " + TBL_List + " SET " + KEY_nonReadMsg + " = " + 0 +
                    " WHERE " + KEY_roomkey + " = " + room_key + " AND " + KEY_channel + " = " + curr_ch);
        else
            db.execSQL("UPDATE " + TBL_List + " SET " + KEY_nonReadMsg + " = " + 0 +
                    " WHERE " + KEY_roomkey + " = " + room_key);
        db.close();
    }

    /** *** **** **** **** **** **** **** **** ******* **** **** *** **** **** **** ******* **** **** **** **** **** **** **** **** **/


    /** *** **** **** **** **** **** **** **** ** Chat table 메소드 * ******* **** **** **** **** **** **** **** **** **/
    /**
     * Chat table에 데이터 저장
     **/
    public int save_chatmsg(int ch, int room_key, String user_name, String msg, boolean sORr, int user_key, boolean isRead) {
        ContentValues values = new ContentValues();
        values.put(KEY_channel, ch);
        values.put(KEY_roomkey, room_key);
        if (user_name.equals(""))
            values.put(KEY_username, "(유저 이름 찾을 수 없음)");
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

        Log.d("sibbal", "before isRead: " + isRead);
        if (isRead)
            //아무것도안함
            isRead = false;
        else {
            Log.d("sibbal", "ch: " + ch + "\nroome_key"+room_key);
            update_nonReadMsg(ch, room_key);
        }//하드코딩으로 +1
        isRead = false;

        int key = -1;
        SQLiteDatabase db = getWritableDatabase();
        if (db.insert(TBL_Chat, null, values) > 0) {
            Cursor c = get_chat_cursor(ch, room_key);
            c.moveToLast();
            key = c.getInt(7);
        }
        db.close();
        return key;
    }

    public String get_chat_time(int key) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(SQL_SELECT_CHAT + " WHERE " + KEY_chatkey + " = " + key, null);
        String time = "";
        if (c.moveToFirst()) {
            time = c.getString(4);
        }
        db.close();
        return time;
    }

    Cursor get_chat_cursor(int channel, int room) {
        // 전체채팅방일 때만 현재 채널의 메시지 띄움
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        if (room == 0) {
            c = db.rawQuery(SQL_SELECT_CHAT + " WHERE " + KEY_channel + "= " + channel + " AND " + KEY_roomkey + " = " + room, null);
        } else {
            c = db.rawQuery(SQL_SELECT_CHAT + " WHERE " + KEY_roomkey + " = " + room, null);
        }
        return c;
    }

    Cursor get_chat_cusorLast(int key, int roomkey) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(SQL_SELECT_CHAT + " WHERE " + KEY_chatkey + " = " + key + " AND " + KEY_roomkey + " = " + roomkey, null);
    }


    /**
     * 임시함수
     **/
    public void delete_chat() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_CHAT);
        db.close();
    }

    public void delete_ch(int ch) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_CH_WHERE + " " + KEY_channel + "=" + ch);
        //      db.execSQL(SQL_DELETE_LIST_WHERE + " " + KEY_channel + "=" + ch +" AND " + KEY_roomkey + " = "+ 0);
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
        Log.d("dclv", "save_blueon"+Integer.toHexString(a)+"  "+Integer.toHexString(b));

        save_set_setting(a, b);
    }

    public void save_bd(byte[] bd) {
        int bd1 = 0;
        bd1 |= (0xff & bd[0]) << 8;
        bd1 |= (0xff & bd[1]);
        int bd2 = 0;
        bd2 |= (0xff & bd[2]) << 24;
        bd2 |= (0xff & bd[3]) << 16;
        bd2 |= (0xff & bd[4]) << 8;
        bd2 |= (0xff & bd[5]);

        int a = get_setint(0);
        int b = 0;

        a = a & ~(bdMSK1);
        a |= bdMSK1 & bd1;
        b |= bdMSK2 & bd2;

        Log.d("dclv", "save_bd"+Integer.toHexString(a)+"  "+Integer.toHexString(b));
        save_set_setting(a, b);
    }


    public int get_setint(int i) {
        Cursor c = get_set_cursor();
        int r = 0;
        switch (i) {
            case 0:
                r = 0xFFFE0000;
                break;
            case 1:
                r = 0; // default = 0
                break;
        }
        if (c.moveToFirst()) {
            r = c.getInt(i);
            Log.d("dclv", "default set "+ Integer.toHexString(r));
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
            } else
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

        int set = 15;
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