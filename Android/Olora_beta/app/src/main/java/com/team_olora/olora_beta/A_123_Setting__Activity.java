package com.team_olora.olora_beta;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class A_123_Setting__Activity extends AppCompatActivity {
    InputMethodManager imm;
    int mode;
    int mod = 0;
    private int ch;
    private int timer_sec = 10;
    private TimerTask second;
    private TextView timer_text;
    private final Handler handler = new Handler();
    RelativeLayout setBox;
    RelativeLayout progressBox;
    /**
     * 0 = 본인이름 설정
     * 1 = 채팅방이름 설정
     * 2 = 상대이름 설정
     * 3 = set dclv
     */
    private TextView titleBar,setHint;
    private EditText setname;
    private ImageButton btnDel;
    private Button btnSet;

    private int Key;
    private String Name;
    private String originName="";

    private NumberPicker set_Dclv;
    private TextView set_Dclv_char;
    private int dclv;


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

        titleBar = findViewById(R.id.titleBarText);
        setBox = findViewById(R.id.setbox);

        setHint = findViewById(R.id.set_hint);
        setname = findViewById(R.id.txtSetList);
        btnDel = findViewById(R.id.btnTextDel);
        set_Dclv = findViewById(R.id.setDclv);
        set_Dclv_char = findViewById(R.id.setDclvchar);

        progressBox = findViewById(R.id.progressbox);
        prog = findViewById(R.id.Prog);
        timer_text = findViewById(R.id.timer);
        progressBox.setVisibility(View.GONE);

        btnSet = findViewById(R.id.btn_set);
        DB = new C_DB(getApplicationContext());

        setname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    setAction();
                    return true;
                }
                return false;
            }
        });

        Provider_BusProvider.getInstance().register(this);
        switch (mode) {
            case 0:
                set_Dclv.setVisibility(View.GONE);
                set_Dclv_char.setVisibility(View.GONE);
                setname.setVisibility(View.VISIBLE);
                btnDel.setVisibility(View.VISIBLE);
                setHint.setText("본인 이름을 입력해 주세요.");
                titleBar.setText("사용자 이름 변경");
                setname.setHint("내 이름");
                /*
                setname.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if((event.getAction()==KeyEvent.ACTION_DOWN)&&(keyCode==KeyEvent.KEYCODE_ENTER)){
                            setAction();
                        }
                        return false;
                    }
                });
                */
                setname.setText(Name);
                break;
            case 1:
                set_Dclv.setVisibility(View.GONE);
                set_Dclv_char.setVisibility(View.GONE);
                setname.setVisibility(View.VISIBLE);
                btnDel.setVisibility(View.VISIBLE);

                userName=DB.get_user_name_inList(Key);
                setHint.setText("설정할 채팅방 이름을 입력해 주세요.");
                titleBar.setText("채팅방 이름 변경");
                setname.setHint(userName);
                setname.setText(Name); // 태그명 PrevName으로 변경
                break;
            case 2:
                set_Dclv.setVisibility(View.GONE);
                set_Dclv_char.setVisibility(View.GONE);
                setname.setVisibility(View.VISIBLE);
                btnDel.setVisibility(View.VISIBLE);

                originName = DB.get_user_origin_name(Key);
                setHint.setText("설정할 상대방 별명을 입력해 주세요.");
                titleBar.setText("상대방 별명 변경");
                setname.setHint(originName); // 태그명 PrevName으로 변경
                setname.setText(Name);
                break;
            case 3:
                setname.setVisibility(View.GONE);
                btnDel.setVisibility(View.GONE);
                set_Dclv.setVisibility(View.VISIBLE);
                set_Dclv_char.setVisibility(View.VISIBLE);

                setHint.setText("채널 탐색 시간을 설정해 주세요.");
                titleBar.setText("채널 탐색 시간 설정");
                int prev_val = DB.get_set_dclv();
                set_Dclv.setMinValue(6);
                set_Dclv.setMaxValue(15);
                set_Dclv.setWrapSelectorWheel(false);
                set_Dclv.setValue(prev_val);
                set_Dclv.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        dclv = newVal;
                    }
                });

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
                    setAction();
                    break;
            }
        }
    }

    protected void setAction(){
        final String name = setname.getText().toString();
        final Service_packet packet = new Service_packet();
        final String s = A_MainActivity.addr_self;
        final byte[] d = packet.converted_addr(A_MainActivity.RSP_MacAddr);
        switch (mode) {
            case 0:
                ch = DB.get_ch_Current();

                if (Service_BluetoothChatService.mState == 3) {
                    final android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(A_123_Setting__Activity.this);
                    alert.setTitle("이름 변경");
                    alert.setMessage("사용 이름을 변경하시겠습니까?\n(단말기와 동기화를 위해 잠시 작동이 제한됩니다.)");
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mod = 1;
                            byte[] tmp_name = new byte[952];
                            byte[] nameByte = name.getBytes();
                            Arrays.fill(tmp_name, (byte) 0);
                            for (int i = 0; i < nameByte.length; i++) {
                                tmp_name[i] = nameByte[i];
                            }
                            setBox.setVisibility(View.GONE);
                            progressBox.setVisibility(View.VISIBLE);
                            btnSet.setVisibility(View.GONE);

                            byte[] temp_ch = FuncGroup.getCHbyte(ch);
                            byte hp = temp_ch[0];
                            byte[] temp_id = new byte[2];
                            temp_id[0] = temp_ch[0];
                            temp_id[1] = temp_ch[1];

                            // get hash
                            byte[] hash = FuncGroup.getHash(tmp_name);
                            // 다른기능이랑 똑같이 해시하는거같은데
                            byte[] setNIpacket = null;
                            setNIpacket = packet.converted_packet(s, d, "SET_NODEIDENTIFIER", hp, temp_id, hash, nameByte);

                            Log.d("finalTest", "--\n\n-----------------Start NI_set Packet -----------------"
                                    +"\n"+"msg send : "+packetHandler.byteArrayToHexString(setNIpacket)
                                    +"\n"+"src : "+packetHandler.getHeaderString(setNIpacket,0,packetHandler.LEN_SRC)
                                    +"\n"+"dest : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_DST,packetHandler.LEN_DST)
                                    +"\n"+"cm : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_CM,packetHandler.LEN_CM)
                                    +"\n"+"hp : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_HP,packetHandler.LEN_HP)
                                    +"\n"+"proto : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_PROTO,packetHandler.LEN_PROTO)
                                    +"\n"+ "id : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_ID,packetHandler.LEN_ID)
                                    +"\n"+ "flags : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_FLAGS,packetHandler.LEN_FLAGS)
                                    +"\n"+ "frag : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_FRAG,packetHandler.LEN_FRAG)
                                    +"\n"+ "seq : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_SEQ,packetHandler.LEN_SEQ)
                                    +"\n"+ "tms : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_TMS,packetHandler.LEN_TMS)
                                    +"\n"+ "len : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_LEN,packetHandler.LEN_LEN)
                                    +"\n"+ "ttl : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_TTL,packetHandler.LEN_TTL)
                                    +"\n"+ "param : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_PARAM,packetHandler.LEN_PARAM)
                                    +"\n"+ "dc : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_DC,packetHandler.LEN_DC)
                                    +"\n"+ "#####################   END NI_set Packet #################### \n\n");

                            int dataLen=packetHandler.getMsgLen(setNIpacket);
                            Log.d("finalTest", "len2 : "+dataLen);
                            Log.d("finalTest", "data : "+packetHandler.getHeaderString(setNIpacket,packetHandler.MASK_DATA,dataLen));

                            /****/


                            A_MainActivity.mbtService.mChatService.write(setNIpacket);
                            time_run(10);
                        }
                    });
                    alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
                    alert.show();
                } else {
                    DB.get_user_Myname();
                    DB.save_userMY(name, 0); // 본인 Xbee 맥 어드레스
                    Intent intent = new Intent(getApplicationContext(), A_MainActivity.class);
                    intent.putExtra("Page", 0);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
            case 1:
                if (name.length() == 0) {
                    Log.d("Eho", "length = 0");
                    DB.update_list_name(userName, Key);
                } else {
                    Log.d("Eho", "no! length = "+name.length());
                    DB.update_list_name(name, Key);
                }
                Intent intent = new Intent(getApplicationContext(), A_MainActivity.class);
                intent.putExtra("Page", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case 2:
                if(name.length()==0)
                    DB.update_user(originName, Key);
                else
                    DB.update_user(name, Key);

                Intent intent1 = new Intent(getApplicationContext(), A_MainActivity.class);
                intent1.putExtra("Page", 2);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                break;
            case 3:
                ch = DB.get_ch_Current();

                final android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(A_123_Setting__Activity.this);
                alert.setTitle("탐색 시간 조절");
                alert.setMessage("채널 탐색시간을 조절하시겠습니까?\n(단말기와 동기화를 위해 잠시 작동이 제한됩니다.)");
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mod = 4;
                        set_Dclv.setVisibility(View.GONE);
                        set_Dclv_char.setVisibility(View.GONE);
                        progressBox.setVisibility(View.VISIBLE);
                        btnSet.setVisibility(View.GONE);

                        byte[] tmp_dclv = new byte[952];
                        byte[] _dclv = new byte[1];
                        _dclv[0] =  (byte)dclv;

                        tmp_dclv[0] = _dclv[0]; // ?? 이게 모징

                        byte[] temp_ch = FuncGroup.getCHbyte(ch);
                        byte hp = temp_ch[0];
                        byte[] temp_id = new byte[2];
                        temp_id[0] = temp_ch[0];
                        temp_id[1] = temp_ch[1];

                        // get hash
                        byte[] hash = FuncGroup.getHash(tmp_dclv);

                        byte[] setDClvpacket = null;
                        setDClvpacket = packet.converted_packet(s, d, "SET_DISCOVERY_TIME", hp, temp_id, hash, _dclv);

                        Log.d("finalTest", "--\n\n-----------------Start SET_DISCOVERY_TIME Packet -----------------"
                                +"\n"+"msg send : "+packetHandler.byteArrayToHexString(setDClvpacket)
                                +"\n"+"src : "+packetHandler.getHeaderString(setDClvpacket,0,packetHandler.LEN_SRC)
                                +"\n"+"dest : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_DST,packetHandler.LEN_DST)
                                +"\n"+"cm : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_CM,packetHandler.LEN_CM)
                                +"\n"+"hp : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_HP,packetHandler.LEN_HP)
                                +"\n"+"proto : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_PROTO,packetHandler.LEN_PROTO)
                                +"\n"+ "id : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_ID,packetHandler.LEN_ID)
                                +"\n"+ "flags : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_FLAGS,packetHandler.LEN_FLAGS)
                                +"\n"+ "frag : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_FRAG,packetHandler.LEN_FRAG)
                                +"\n"+ "seq : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_SEQ,packetHandler.LEN_SEQ)
                                +"\n"+ "tms : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_TMS,packetHandler.LEN_TMS)
                                +"\n"+ "len : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_LEN,packetHandler.LEN_LEN)
                                +"\n"+ "ttl : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_TTL,packetHandler.LEN_TTL)
                                +"\n"+ "param : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_PARAM,packetHandler.LEN_PARAM)
                                +"\n"+ "dc : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_DC,packetHandler.LEN_DC)
                                +"\n"+ "#####################   END SET_DISCOVERY_TIME Packet #################### \n\n");
                        int dataLen=packetHandler.getMsgLen(setDClvpacket);
                        Log.d("finalTest", "len2 : "+dataLen);
                        Log.d("finalTest", "data : "+packetHandler.getHeaderString(setDClvpacket,packetHandler.MASK_DATA,dataLen));

                        /****/
                        // write ( set dclv )
                        DB.save_dvlv(dclv);
                        time_run(10);
                                    /*
                                    DB.save_dvlv(dclv);
                                    Intent intent2 = new Intent(getApplicationContext(),A_MainActivity.class);
                                    intent2.putExtra("Page",3);
                                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent2);
                                    */
                    }
                });
                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                alert.show();
                break;
        }
    }

    protected void time_run(int default_time) {
        timer_sec = default_time;
        timer_text.setVisibility(View.VISIBLE);
        second = new TimerTask() {
            @Override
            public void run() {
                Update();
                timer_sec--;
            }
        };
        Timer timer = new Timer();
        timer.schedule(second, 0, 1000);
    }

    protected void Update() {
        Runnable updater = new Runnable() {
            public void run() {
                timer_text.setText("설정 대기시간\n" + timer_sec + "초");
                if (timer_sec < 2 & mod != 0) {
                    timer_sec = 10;
                    Toast.makeText(A_123_Setting__Activity.this, "설정에 실패했습니다. \n연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), A_MainActivity.class);
                    intent.putExtra("Page", mod - 1);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mod = 0;
                    startActivity(intent);
                }
            }
        };
        handler.post(updater);
    }

    @Subscribe
    public void receive_NI(Provider_BusProviderFunc bpf) {
        mod = 0;
        String NI = bpf.getMyNI();
        long addr = bpf.getMyMac();

        DB.save_userMY(NI, addr); // 본인 Xbee 맥 어드레스

        Intent intent = new Intent(getApplicationContext(), A_MainActivity.class);
        intent.putExtra("Page", 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mod != 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}


// DB 접근 안하도록 설계