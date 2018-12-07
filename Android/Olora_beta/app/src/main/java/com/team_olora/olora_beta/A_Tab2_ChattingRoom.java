package com.team_olora.olora_beta;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.lang.Boolean.TRUE;


// 여기서 bingService
public class A_Tab2_ChattingRoom extends AppCompatActivity implements AdapterView.OnItemLongClickListener {
    public static int a;
    ///////////////////
    ///// 멤버
    ///////////////////
    private C_DB DB = null;
    private ListView m_ListView;
    private ListChatAdapter m_Adapter;
    private EditText chatMessage;
    private ImageButton sendMessage;
    private InputMethodManager imm;
    private Service_btService mbtService = null;
    private Service_packet packet = null;
    private TextView ano_room;
    private TextView Public_msg;

    private int receive_key = 0;

    /**
     * 상대 유저키와 유저네임
     **/
    private int userKey;
    private String userName;
    private long useraddr;
    private int cur_ch, room,ch;


    //////////////// 커넥션 객체
    ServiceConnection sconn = new ServiceConnection() {

        // bind 되었을 때 - bind 만 한다. 서비스 객체에 셋 되어있는 핸들러, 챗섭스를 가져온다.(없을때만 생성!)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Toast.makeText(getApplicationContext(), "바운드 됨", Toast.LENGTH_LONG).show();
            Service_btService.myBinder mbinder = (Service_btService.myBinder) service;
            mbtService = mbinder.getSercive();
            mbtService.make_handler();
            mbtService.set_connect();
        }

        // unbind 되었을 때
        @Override
        public void onServiceDisconnected(ComponentName name) {

            //Toast.makeText(getApplicationContext(), "언바운드 됨", Toast.LENGTH_LONG).show();
            mbtService = null;
        }
    };
    //////////////// 커넥션 객체

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(A_Tab2_ChattingRoom.this, A_MainActivity.class);
        intent.putExtra("Page", 1);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    /*****************
     // bind 함수
     *****************/
    private void bind() {
        Intent bind = new Intent(getApplicationContext(), Service_btService.class);
        bindService(bind, sconn, BIND_AUTO_CREATE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        ch =  intent.getIntExtra("Room_ch",0);
        room = intent.getIntExtra("Room_key", 0);
        userKey = intent.getIntExtra("User_key", 0);
        Log.d("MSGMSG: - InRoom ","Room_key = "+room+"   User_key = "+userKey);

        if (room == 0)
            // pubic room ( for broadcast )
            setContentView(R.layout.a_chatting_room_p);
        else
            // private room ( for unicast)
            setContentView(R.layout.a_chatting_room);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Provider_RecvMsg.getInstance().register(this);

        // bind
        try {
            bind();
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }


        m_Adapter = new ListChatAdapter();
        // Xml에서 추가한 ListView 연결
        m_ListView = findViewById(R.id.listView1);
        DB = new C_DB(this);
        userName = DB.get_user_name(userKey);
        cur_ch = DB.get_ch_Current();

        try {
            Log.d("MSGMSG: - last key ","userKey = "+userKey);
            useraddr = DB.get_user_addr(userKey);
            Log.d("MSGMSG: - last addr ","userAddr = "+useraddr);
        } catch (Exception e) {
            Log.e("CHATTINGROOM:::", "NO_user key");
        }

        if (room == 0) {
            Public_msg = findViewById(R.id.public_msg);
            Public_msg.setText(String.valueOf(cur_ch) + " 채널의 전체 대화방입니다.");

            Animation mAnimation = new AlphaAnimation(1, 0);
            mAnimation.setDuration(500);
            mAnimation.setInterpolator(new LinearInterpolator());
            mAnimation.setRepeatCount(Animation.INFINITE);
            mAnimation.setRepeatMode(Animation.REVERSE);

            //애니메이션 시작
            Public_msg.startAnimation(mAnimation);
        }
        // ListView에 어댑터 연결
        m_ListView.setAdapter(m_Adapter);


        load_values();

//        curText = findViewById(R.id.msgMax);
        chatMessage = findViewById(R.id.chatText);
        sendMessage = findViewById(R.id.chatSend);
        ano_room= findViewById(R.id.anotherch_alarm);
        if(DB.get_ch_Current() != ch){
            chatMessage.setVisibility(View.GONE);
            sendMessage.setVisibility(View.GONE);
            ano_room.setVisibility(View.VISIBLE);
        }
        imm.hideSoftInputFromWindow(chatMessage.getWindowToken(), 0);
        chatMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (chatMessage.isFocusable()) {
                    byte[] byteText = chatMessage.getText().toString().getBytes();
                    //               curText.setText(String.valueOf(byteText.length)+"/980 바이트");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String after_text = s.toString();
                byte[] getbyte = after_text.getBytes();
                if (getbyte.length > 980) {
                    s.delete(s.length() - 2, s.length() - 1);
                    //Toast.makeText(getApplicationContext(), "그만쳐..", Toast.LENGTH_SHORT);
                }
            }
        });
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Component_2_msgAdapter msgAdapter = new Component_2_msgAdapter();
                /**send 버튼의 클릭 이벤트 **/
                String msg = chatMessage.getText().toString();
                if (msg.trim().length() != 0) {
                    imm.hideSoftInputFromWindow(chatMessage.getWindowToken(), 0);  // 키보드 내리기 or 내리지말기?
                    // send
                    byte[] encodingmsg = msg.getBytes();

                    Log.d("MSGMSG", "여기서 보내는ch " + String.valueOf(cur_ch));
                    Log.d("MSGMSG", "여기서 보내는리시브 키 " + String.valueOf(receive_key));
                    Log.d("MSGMSG", "여기서 보내는샌드 키 " + String.valueOf(room));
                    String strrrr = new String(encodingmsg);
                    Log.d("MSGMSG", "여기서 보내는 메시지 " + strrrr);

                    int key = save_values("", msg, TRUE, 0);
                    load_in_chat(key);
                    chatMessage.setText("");
                    sendMessage(encodingmsg);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        Provider_RecvMsg.getInstance().unregister(this);
        super.onDestroy();
    }


    @Override
    protected void onStart() {
        super.onStart();
        packet = new Service_packet();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //m_Adapter.remove(position);
        Toast.makeText(getApplicationContext(), "삭제, 복사", Toast.LENGTH_LONG).show();
        return false;
    }

    private int save_values(String name, String msg, boolean type, int userKey) {
        return DB.save_chatmsg(cur_ch, room, name, msg, type, userKey);
    }

    private void load_values() {
        Cursor cursor = DB.get_chat_cursor(cur_ch, room);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(2);
                String msg = cursor.getString(3);
                boolean sORr = cursor.getInt(5) > 0;
                String date = cursor.getString(4);
                if (sORr) {
                    m_Adapter.add("", msg, 1, date);
                } else {
                    m_Adapter.add(name, msg, 0, date);
                }
            } while (cursor.moveToNext());
        }
        m_Adapter.notifyDataSetChanged();
        m_ListView.setSelection(m_Adapter.getCount() - 1);
    }

    private void load_in_chat(int key) {
        Cursor cursor = DB.get_chat_cusorLast(key);
        if (cursor.moveToFirst()) {
            do {
                Log.d("Chatting Key :", String.valueOf(key));
                String name = cursor.getString(2);
                String msg = cursor.getString(3);
                Log.d("Chatting msg :", String.valueOf(msg));
                boolean sORr = cursor.getInt(5) > 0;
                Log.d("Chatting sORr :", String.valueOf(sORr));
                String date = cursor.getString(4);
                if (sORr) {
                    m_Adapter.add("", msg, 1, date);
                } else {
                    m_Adapter.add(name, msg, 0, date);
                }
            } while (cursor.moveToNext());
        }
        m_Adapter.notifyDataSetChanged();
        m_ListView.setSelection(m_Adapter.getCount() - 1);
    }

    @Subscribe
    public void receive_msg(Provider_RecvMsgFunc recvEvent) {
        int key = recvEvent.getChatkey();
        load_in_chat(key);
        Log.d("Chatting recvkey :", String.valueOf(key));
    }


    // send 함수
    private void sendMessage(byte[] message) {

        /*******/
        Component_2_msgAdapter msgAdapter = new Component_2_msgAdapter();
        // 연결중과 연결 안되어있을 때 구분. 예외처리.
        // Check that we're actually connected before trying anything
        if (mbtService.mChatService.getState() == Service_BluetoothChatService.STATE_NONE) {
            //Toast.makeText(getApplicationContext(), "연결이 필요합니다!!", Toast.LENGTH_SHORT).show();
            mbtService.stopSelf();
            return;
        } else if (mbtService.mChatService.getState() == Service_BluetoothChatService.STATE_CONNECTING) {
            //Toast.makeText(getApplicationContext(), "연결중입니다.", Toast.LENGTH_SHORT).show();
            return;
        }


        // 패킷 send
        try {
            //mbtService.mChatService.write(converted_packet(send));

            // 패킷 조작 클래스에서 handle
            String s = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");

            ByteBuffer Lbuf = ByteBuffer.allocate(8);
            Lbuf.putLong(useraddr);
            byte[] d = Lbuf.array();

            Log.d("MSGMSG:::", "sendMessage sauce: " + useraddr);

            // send
            byte hp = 0x00;
            byte[] id = new byte[2];
            hp = (byte) (cur_ch & 0x0007);
            id[0] = (byte) (cur_ch & 0x0003);
            id[1] = (byte) (cur_ch & 0xFFF8);
            byte[] sendapacket = null;

            byte[] message_send = new byte[952];
            Arrays.fill( message_send, (byte) 0 );
            for(int i = 0; i < message.length; i ++)
            {
                message_send[i] = message[i];
            }

            if (room == 0) {
                Log.d("MSGMSG:::", "브로드 캐스트 sendMessage dest: " + String.valueOf(useraddr));
                sendapacket = packet.converted_packet(s, d, "SEND_BROADCAST", hp, id, message_send);
            } else {
                Log.d("MSGMSG:::", "유니 캐스트 user addr Lbuf: " + Lbuf);
                Log.d("MSGMSG:::", "유니 캐스트 user addr useraddr: " + useraddr);
                Log.d("MSGMSG:::", "유니 캐스트 s: " + s);
                Log.d("MSGMSG:::", "유니 캐스트 d: " + d);
                Log.d("MSGMSG:::", "유니 캐스트 msg: " + message);
                sendapacket = packet.converted_packet(s, d, "SEND_UNICAST", hp, id, message_send);
            }
            mbtService.mChatService.write(sendapacket);
            Log.d("MSGMSG:::", "유니 캐스트 전체패킷: " + byte2hex(sendapacket));
        } catch (Exception e) {
            //Toast.makeText(this, "send 에러" + android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address") + getIntent().getStringExtra("device_address"), Toast.LENGTH_SHORT).show();
        }

        // Reset out string buffer to zero and clear the edit text field
        mbtService.mOutStringBuffer.setLength(0);
        chatMessage.setText(mbtService.mOutStringBuffer);
    }

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
}
