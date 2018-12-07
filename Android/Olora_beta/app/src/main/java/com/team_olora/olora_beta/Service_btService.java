package com.team_olora.olora_beta;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;

// 연결타입 서비스
public class Service_btService extends Service {

    ///////////////////
    ///// 멤버
    ///////////////////
    private String mConnectedDeviceName = null;
    protected StringBuffer mOutStringBuffer;
    protected Service_BluetoothChatService mChatService = null;
    public BluetoothAdapter mBluetoothAdapter = null;
    public int var = 777; //서비스바인딩의 예시로 출력할 값
    public Handler mHandler = null;
    public String address = null;
    ListChatAdapter m_Adapter;

    public C_DB DB = null;

    // 바인더 객체 반환.
    private IBinder mIBinder = new myBinder();

    class myBinder extends Binder {
        Service_btService getSercive() {
            return Service_btService.this;
        }
    }

    public Service_btService() {
    }

    ///////////////////////////
    //    콜백 메서드
    //////////////////////
    @Override
    @RequiresApi(Build.VERSION_CODES.O)
    public void onCreate() {
        //Toast.makeText(getApplicationContext(), "service :  start", Toast.LENGTH_LONG).show();

        DB = new C_DB(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            /************************/
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// 채널 ID
            String id = "my_channel_01";
// 채널 이름
            CharSequence name = "test";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
// 알림 채널에 사용할 설정을 구성한다.
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]
                    {
                            100, 200, 300, 400, 500, 400, 300, 200, 400
                    });
// 뱃지 사용 여부를 설정한다.(8.0부터는 기본이 true인듯하다.)
            mChannel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(mChannel);
            /*************************/
        }
        else
            startForeground(1, new Notification());

    }
    // onBind 바인더 return
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //Toast.makeText(getApplicationContext(), "service : onBind", Toast.LENGTH_LONG).show();

        return mIBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 백그라운드 핵심..
        // foreground 만 해주면 잡지랄 아무것도 안해도됨.??
        //Toast.makeText(getApplicationContext(), "service : onStartCommand", Toast.LENGTH_LONG).show();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Toast.makeText(getApplicationContext(), "service : onUnbind", Toast.LENGTH_LONG).show();

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 대몬 종료시 addr 초기화.
        A_MainActivity.RSP_MacAddr = "00:00:00:00:00:00";

        if (mChatService != null) {
            mChatService.stop();
        }
    }


    ///////////////////////////
    //    사용자 정의 메서드 --- onBind 하여 호출할 함수 - 오직 채팅방 들어갈때만?
    //////////////////////

    @SuppressLint("HandlerLeak")
    public void make_handler() {
        if (mHandler == null) {                               // 핸들러가 없을때만 생성. 얘도 마찬가지로 계속 생성하니까 끊김??
            mHandler = new Handler() {
                @Override

                public void handleMessage(Message msg) {
                    //FragmentActivity activity = getActivity();

                    switch (msg.what) {
                        case Service_Constants.MESSAGE_STATE_CHANGE:
                            switch (msg.arg1) {
                                case Service_BluetoothChatService.STATE_CONNECTED:
                                    //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                                    //mConversationArrayAdapter.clear();
                                    break;
                                case Service_BluetoothChatService.STATE_CONNECTING:
                                    Toast.makeText(getApplicationContext(), "h : 장치와 연결 되었습니다.", Toast.LENGTH_SHORT).show();

                                    //setStatus(R.string.title_connecting);
                                    break;
                                case Service_BluetoothChatService.STATE_LISTEN:
                                    Toast.makeText(getApplicationContext(), "h : 장치에 연결 중..", Toast.LENGTH_SHORT).show();

                                case Service_BluetoothChatService.STATE_NONE:
                                    Toast.makeText(getApplicationContext(), "h : 장치와 연결이 끊어졌습니다.\n다시 연결해주세요.", Toast.LENGTH_SHORT).show();
                                    Provider_BlueOn.getInstance().post(new Provider_BlueOnFunc(0));
                                    DB.save_blueon(0);
                                    String nullbd = "000000000000";
                                    DB.save_bd(FuncGroup.hexStringToByteArray(nullbd));
                                    // 스스로 종료시키기. 재시작을 해줘야함.
                                    // 재시작하는 인터페이스를 연결해 줘야함???
                                    //setStatus(R.string.title_not_connected);
                                    break;
                            }
                            break;

                        case Service_Constants.MESSAGE_WRITE:
                            // Toast.makeText(getApplicationContext(), "h : 송신 성공.", Toast.LENGTH_SHORT).show();
                            byte[] writeBuf = (byte[]) msg.obj;
                            break;

                        case Service_Constants.MESSAGE_READ:
                            Component_2_msgAdapter msgAdapter1 = new Component_2_msgAdapter();
                            /**받기*/

                            byte[] read = (byte[]) msg.obj;
                            int readLen = 0;
                            try {
                                readLen = read.length;
                            } catch (Exception e) {
                            }

                            if (readLen != 0) {
                                String receivemsg = msgAdapter1.parse_msg((byte[]) msg.obj);
                                int room_key;
                                long usermac = Service_packet.macaddr; // 유저 mac addr
                                int user = DB.get_user_key(usermac); // 유저 키
                                Log.d("Service", ": get user key = " + user);
                                int black = DB.get_isblack(usermac);

                                if (user == -1) {
                                    //user 검색이 안되면 즉 discovery 필요
                                    user = DB.save_user("친구검색이필요합니다.", usermac);
                                } else {
                                    //user 검색 된다 discovery 불필요
                                }
                                String username = DB.get_user_name(user);

                                // addr 와 일치하는 room key 있는지 검색
                                // 있으면 roomkey, 없으면 만들어서 리턴
                                int ch = DB.get_net_Current_ch();
                                room_key = DB.echo_room_key(username, user, usermac);
                                int chatkey = DB.save_chatmsg(ch, room_key, username, receivemsg, false, user);

                                Log.d("Service::ROM", "저장된 chatkey:" + chatkey + " 저장된 채널 :" + ch + " 저장된 recieve :" + room_key +
                                        "저장된 유저네임 : " + username + " 저장된 메시지 :" + receivemsg + " 저장된 유저 키: " + user);
                                Provider_RecvMsg.getInstance().post(new Provider_RecvMsgFunc(chatkey));
                                //DB.save_list_recievekey(send_key, receive_key);

                                /** ** ** ** ** ** ** ** ** ** ** 푸시알람 ** ** ** ** ** ** ** ** ** ** ** */
                                Boolean push = DB.get_set_push();
                                Boolean vibe = DB.get_set_vibe();
                                Boolean sound = DB.get_set_sound();

                                if (push) {

                                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    Intent intent = new Intent(getApplicationContext(), A_Tab2_ChattingRoom.class);
                                    Intent intent_ = new Intent(getApplicationContext(), Service_PushPop.class);
                                    String address = A_MainActivity.RSP_MacAddr.toString();
                                    String time = DB.get_chat_time(ch, room_key, chatkey);
                                    intent.putExtra("device_address", address);
                                    intent.putExtra("Room_key", room_key);

                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
                                    wakeLock.acquire(5000);

                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                                            0, intent, PendingIntent.FLAG_ONE_SHOT);


                                    if (sound && vibe) {
                                        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                                                .setContentIntent(pendingIntent)
                                                .setSmallIcon(R.drawable.nayeon_icon).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.tzui_icon))
                                                .setContentTitle("안 읽은 메시지")
                                                .setContentText(receivemsg)
                                                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                                .setTicker("")
                                                .setAutoCancel(true);
                                        Notification notification = builder.build();
                                        nm.notify(1232, notification);
                                    }
                                    if (!sound && vibe) {
                                        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                                                .setContentIntent(pendingIntent)
                                                .setSmallIcon(R.drawable.nayeon_icon)
                                                .setContentTitle("안 읽은 메시지")
                                                .setContentText(receivemsg)
                                                .setDefaults(Notification.DEFAULT_VIBRATE)
                                                .setTicker("")
                                                .setAutoCancel(true);
                                        Notification notification = builder.build();
                                        nm.notify(1232, notification);
                                    }
                                    if (sound && !vibe) {
                                        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                                                .setContentIntent(pendingIntent)
                                                .setSmallIcon(R.drawable.nayeon_icon)
                                                .setContentTitle("안 읽은 메시지")
                                                .setContentText(receivemsg)
                                                .setDefaults(Notification.DEFAULT_SOUND)
                                                .setTicker("")
                                                .setAutoCancel(true);
                                        Notification notification = builder.build();
                                        nm.notify(1232, notification);
                                    }
                                    if (!sound && !vibe) {
                                        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                                                .setContentIntent(pendingIntent)
                                                .setSmallIcon(R.drawable.nayeon_icon)
                                                .setContentTitle("안 읽은 메시지")
                                                .setContentText(receivemsg)
                                                .setTicker("")
                                                .setAutoCancel(true);
                                        Notification notification = builder.build();
                                        nm.notify(1232, notification);
                                    }
                                } else {

                                }


                                /****/
                            }

                            break;
                        case Service_Constants.MESSAGE_CHANNELSET:
                            byte[] tlqkf = (byte[]) msg.obj;
                            int ch = 0;
                            ch &= 0x3FFFF;
                            String id1 = FuncGroup.byteArrayToHexString(tlqkf);
                            ch |= (tlqkf[1] & 0x7f);
                            ch <<= 8;
                            ch |= (tlqkf[2] & 0x00ff);
                            ch <<= 3;
                            ch |= (tlqkf[0] & 0x07);
                            Provider_SetCH.getInstance().post(new Provider_SetCHFunc(ch));
                            break;
                        case Service_Constants.MESSAGE_DISCOVERY:
                            Log.d("DIscover:::", "받은 msg.org" + msg.obj);
                            discover((byte[]) msg.obj);
                            Log.d("DIscover:::", "bt Service 진입");
                            Provider_Discovery.getInstance().post(new Provider_DiscoveryFunc());
                            break;
                        case Service_Constants.MESSAGE_DEVICE_NAME:
                            // save the connected device's name
                            mConnectedDeviceName = msg.getData().getString(Service_Constants.DEVICE_NAME);

                            Toast.makeText(getApplicationContext(), "h : Connected to "
                                    + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            Provider_BlueOn.getInstance().post(new Provider_BlueOnFunc(1));
                            DB.save_blueon(1);


                            break;
                        case Service_Constants.MESSAGE_TOAST:
                            Toast.makeText(getApplicationContext(), msg.getData().getString(Service_Constants.TOAST),
                                    Toast.LENGTH_SHORT).show();
                            break;

                        // send result 처리
                        case Service_Constants.MESSAGE_RESULT:
                            Toast.makeText(getApplicationContext(), msg.getData().getString(Service_Constants.TOAST),
                                    Toast.LENGTH_SHORT).show();
                            break;

                        case Service_Constants.MESSAGE_SET_NI:
                            Service_packet packet = new Service_packet();
                            if (((byte[]) msg.obj).length != 0) {
                                byte[] mbuffer = (byte[]) msg.obj;
                                Log.d("SETNI:::", "Set NI 결과" + mbuffer);
                                byte[] Mac = Arrays.copyOfRange(mbuffer, 36, 44);
                                byte[] NI = Arrays.copyOfRange(mbuffer, 44, packet.indexOfEOT(mbuffer) + 1);
                                String myNI = new String(NI);
                                Log.d("SETNI:::", "Set NI 결과" + myNI);
                                Log.d("SETNI:::", "Set NI 결과" + Mac);
                                ByteBuffer Lbuf = ByteBuffer.wrap(Mac);
                                long myMac = Lbuf.getLong();
                                Provider_BusProvider.getInstance().post(new Provider_BusProviderFunc(myNI, myMac));
                            }
                            break;
                    }
                }
            };
        } else if (mChatService.mState != Service_BluetoothChatService.STATE_CONNECTED) {
            //Toast.makeText(getApplicationContext(), "h : 재연결 필요", Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(getApplicationContext(), " h : 이미 연결되어있습니다.", Toast.LENGTH_LONG).show();

    }

    // 연결 끊겼을 때 변수 비워줘야함.
    public void set_connect() {

        if (mChatService == null) {                      // 챗섭스 객체가 없을때만 객체 생성
            // 계속 생성했기 때문에 끊겼다.
            mChatService = new Service_BluetoothChatService(getApplicationContext(), mHandler);
            mOutStringBuffer = new StringBuffer("");
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


            // 연결 직전 여기서 addr 셋팅
            if (A_MainActivity.RSP_MacAddr != "00:00:00:00:00:00")
                address = A_MainActivity.RSP_MacAddr;
            else
                A_MainActivity.RSP_MacAddr = "00:00:00:00:00:00";


            try {
                connectDevice(address, true);
                Toast.makeText(getApplicationContext(), "connected : " + address, Toast.LENGTH_SHORT).show();
                String BD = address.replaceAll(":", "");
                Log.d("BD - reconnect : ", BD);
                DB.save_bd(FuncGroup.hexStringToByteArray(BD));
                Log.d("BD - reconnect", "connectedbd : " + FuncGroup.byteArrayToHexString(FuncGroup.hexStringToByteArray(BD)));
                Log.d("BD - reconnect", " get set bd : " + FuncGroup.byteArrayToHexString(DB.get_set_bd()));

            } catch (Exception e) {
                //Toast.makeText(this, "error : " + e.toString(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, A_MainActivity.RSP_MacAddr, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "connected : nothing\n장치를 선택하고 연결해주세요", Toast.LENGTH_SHORT).show();


            }
        } else if (mChatService.mState != Service_BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), "재연결 필요", Toast.LENGTH_LONG).show();

        } else
            Toast.makeText(getApplicationContext(), "이미 연결되어있습니다.", Toast.LENGTH_LONG).show();
    }

    // 기존함수에서 인자값을 string 으로 떄려박았다.
    // 연결을 하려면 서비스에 있는 주소값을 set 해야한다. Bind 했을 때 꼭 해줘야함
    // ㄹㅇ 루다가 thread 를 생성하는 곳.
    private void connectDevice(String addr, boolean secure) {
        // Get the device MAC address


        String address = addr;
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    public void discover(byte[] bytes) {
        // 8 : Addr / 20 : NI
        int addrLen = 28;
        int num_user = (bytes.length - 1) / addrLen;
        // int num_user = bytes[0];
        byte[] user = new byte[addrLen];
        DB.delete_user_All();
        Log.d("Discovery:::", "삭제후" + String.valueOf(DB.get_user_num()));
        Log.d("Discovery:::", "받은 bytearray" + FuncGroup.byteArrayToHexString(bytes));
        Log.d("Discovery:::", "받은 bytearray 길이 " + bytes.length + "  받은 bytearray 길이2 " + bytes[0]);
        Log.d("Discovery:::", "num_user" + num_user);

        int user_index = 1;
        while (user_index < num_user) {
            int i = 0;
            while (i < addrLen) {
                user[i] = bytes[i + user_index * addrLen];
                i++;
            }
            Log.d("user : ", FuncGroup.byteArrayToHexString(user));
            parse(user);
            user_index++;
        }
    }

    public void parse(byte[] bytes) {
        byte[] addr_byte = new byte[8];
        byte[] name_byte = new byte[20];

        int bytesleng = bytes.length;
        String name;
        long addr;
        int i = 0;

        for (; i < 8; i++) {
            addr_byte[i] = bytes[i];
        }
        for (i = 8; i < 20; i++) {
            name_byte[i] = bytes[i];
        }

        ByteBuffer Lbuf = ByteBuffer.wrap(addr_byte);
        addr = Lbuf.getLong();

        Log.d("User - addr byte : ", FuncGroup.byteArrayToHexString(addr_byte) + "\n name = " + new String(addr_byte));
        Log.d("User - name byte : ", FuncGroup.byteArrayToHexString(name_byte) + "\n name = " + new String(name_byte));

        name = new String(name_byte);
        Log.d("addr", String.valueOf(addr));

        Log.d("User - addr byte : ", "\n name = " + addr);
        Log.d("User - name byte : ", "\n name = " + name);
        int key = DB.save_user(name, addr);
        Log.d("Discovery:::", String.valueOf(key));
        Log.d("Discovery:::", "으악 늘어난다" + String.valueOf(DB.get_user_num()));

    }

}
