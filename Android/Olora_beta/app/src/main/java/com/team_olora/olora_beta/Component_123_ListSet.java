package com.team_olora.olora_beta;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.Arrays;


public class Component_123_ListSet extends AppCompatActivity {
    InputMethodManager imm;
    int mode;

    int mod=0;

    int HP;
    int ID;
    private int ch;
    byte[] HPbyte = new byte[1];
    byte[] IDbyte = new byte[2];


    /**
     * 0 = 본인이름 설정
     * 1 = 채팅방이름 설정
     * 2 = 상대이름 설정
     */
    private TextView titleBar;
    private EditText setname;
    private ImageButton btnDel;
    private Button btnSet;
    private int Key;
    private String Name;

    ProgressBar prog;

    private String userName;
    public C_DB DB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_list_set);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        Intent intent = getIntent();
        mode = intent.getIntExtra("MODE", 2); /// 모드 인텐트 전하는거 !
        Key = intent.getIntExtra("Key", 0);
        Name = intent.getStringExtra("prev_Name");

        prog = findViewById(R.id.Prog);
        titleBar = findViewById(R.id.titleBarText);
        setname = findViewById(R.id.txtSetList);

        btnDel = findViewById(R.id.btnTextDel);
        btnSet = findViewById(R.id.btn_set);
        DB = new C_DB(getApplicationContext());

        Provider_BusProvider.getInstance().register(this);

        switch (mode) {
            case 0:
                titleBar.setText("사용자 이름 변경");
                setname.setHint("내 이름");
                setname.setText(Name);

                break;
            case 1:
                userName = DB.get_list_userName(Key);
                titleBar.setText("채팅방 이름 변경");
                setname.setHint(userName);
                setname.setText(Name); // 태그명 PrevName으로 변경

                break;
            case 2:
                titleBar.setText("친구 이름 변경");
                setname.setHint("친구 이름");
                setname.setText(Name); // 태그명 PrevName으로 변경
                break;
        }

        btnDel.setOnClickListener(new Event());
        btnSet.setOnClickListener(new Event());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        imm.hideSoftInputFromWindow(setname.getWindowToken(), 0);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        Provider_BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    private class Event implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnTextDel:
                    setname.setText("");
                    break;
                case R.id.btn_set:
                    String name = setname.getText().toString();

                    switch (mode) {
                        case 0:
                            Service_packet packet = new Service_packet();
                            String s = A_MainActivity.addr_self;
                            byte[] d = packet.converted_addr(A_MainActivity.RSP_MacAddr);
                            ch=DB.get_net_Current_ch();
                            HP = ch & 0x00000007;
                            ID = ch & 0x0003FFF8;
                            ID >>= 3;
                            HPbyte[0] = (byte) HP;
                            IDbyte[0] = (byte) (ID >> 8);
                            IDbyte[1] = (byte) ID;
                            byte[] CH = new byte[3];
                            CH[0] = (byte) HP;
                            CH[1] = (byte) (ID >> 8);
                            CH[2] = (byte) ID;
                            if (Service_BluetoothChatService.mState == 3) {
                                prog.setVisibility(View.VISIBLE);
                                btnSet.setVisibility(View.GONE);
                                mod=1;
                                A_MainActivity.mbtService.mChatService.write(packet.converted_packet(s, d, "SET_NODEIDENTIFIER", HPbyte[0], IDbyte, name.getBytes()));
                            }
                            else{
                                DB.save_userMY(name, 0); // 본인 Xbee 맥 어드레스
                                Intent intent = new Intent(getApplicationContext(),A_MainActivity.class);
                                intent.putExtra("Page",0);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                             }
                            break;
                        case 1:
                            if(name.length()==0)
                            {
                                DB.update_list_name(userName,Key);
                            }else{
                                DB.update_list_name(name, Key);
                            }
                            Intent intent = new Intent(getApplicationContext(),A_MainActivity.class);
                            intent.putExtra("Page",1);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            break;
                        case 2:
                            DB.update_user(name,Key);
                            Intent intent1 = new Intent(getApplicationContext(),A_MainActivity.class);
                            intent1.putExtra("Page",2);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent1);
                            break;
                    }
                    break;
            }
        }
    }
    @Subscribe
    public void receive_NI(Provider_BusProviderFunc bpf) {
        String NI = bpf.getMyNI();
        long addr = bpf.getMyMac();
        byte[] ni = NI.getBytes();
        int nilen = ni.length;
        byte[] naMe = Arrays.copyOfRange(ni, 0, nilen-3);
        NI = new String(naMe);
        Log.d("SetNI final :", NI+addr);
        DB.save_userMY(NI, addr); // 본인 Xbee 맥 어드레스

        Intent intent = new Intent(getApplicationContext(),A_MainActivity.class);
        intent.putExtra("Page",0);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mod==1) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}

// DB 접근 안하도록 설계