package com.team_olora.olora_beta;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
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


    /***/

    /***/

    public Service_DBHelper DB = null;

    /****/

    /****/


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
    public void onCreate() {
        Toast.makeText(getApplicationContext(), "service :  start", Toast.LENGTH_LONG).show();

        DB = new Service_DBHelper(getApplicationContext());
        startForeground(1, new Notification());

    }
    // onBind 바인더 return

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Toast.makeText(getApplicationContext(), "service : onBind", Toast.LENGTH_LONG).show();

        return mIBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 백그라운드 핵심..
        // foreground 만 해주면 잡지랄 아무것도 안해도됨.??
        Toast.makeText(getApplicationContext(), "service : onStartCommand", Toast.LENGTH_LONG).show();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(getApplicationContext(), "service : onUnbind", Toast.LENGTH_LONG).show();

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "service : onDestroy", Toast.LENGTH_LONG).show();

        // 대몬 종료시 addr 초기화.
        MainActivity.RSP_MacAddr = "00:00:00:00:00:00";


        if (mChatService != null) {
            mChatService.stop();
            Toast.makeText(getApplicationContext(), "service : 쓰레드 종료", Toast.LENGTH_LONG).show();


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
                                    Provider_BlueOn.getInstance().post(new Provider_BOf(0));
                                    DB.save_blueon(0);
                                    String nullbd = "000000000000";
                                    DB.save_bd(hexStringToByteArray(nullbd));

                                    // 스스로 종료시키기. 재시작을 해줘야함.
                                    // 재시작하는 인터페이스를 연결해 줘야함???
                                    //setStatus(R.string.title_not_connected);
                                    break;
                            }
                            break;

                        case Service_Constants.MESSAGE_WRITE:
                            Toast.makeText(getApplicationContext(), "h : 송신 성공.", Toast.LENGTH_SHORT).show();
                            byte[] writeBuf = (byte[]) msg.obj;
                            break;

                        case Service_Constants.MESSAGE_READ:
                            Toast.makeText(getApplicationContext(), "h : 수신 성공.", Toast.LENGTH_SHORT).show();
                            /**에코테스트**/
                            Component_2_msgAdapter msgAdapter1 = new Component_2_msgAdapter();
                            /**보내기 */
                            // encodingmsg가 감

                            /**받기*/
                            byte[] read = (byte[]) msg.obj;
                            if (read.length != 0) {
                                String receivemsg = msgAdapter1.parse_msg((byte[]) msg.obj);
                                int send_key = msgAdapter1.Send;         // 받은 send
                                int receive_key = msgAdapter1.Receive;   // 받은 receive
                                /** 바꾸기 전 **/
                                //헤더에서 유저 맥어드레스 받아서 유저키 얻음
                                long usermac = Service_packet.macaddr; // 유저 mac addr
                                int user = DB.get_user_key(usermac); // 유저 키
                                Log.d("Service", ": get user key = " + user);
                                int black = DB.get_isblack(usermac);

                                String username = DB.get_user_name(user);
                                /**** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **
                                 * 룸넘버 체크 알고리즘
                                 * */
                                if (send_key == 0 && receive_key == 0) {
                                    Log.d("Service", ": 퍼블릭! : " + String.valueOf(usermac));
                                    Log.d("Service", ": 퍼블릭! : " + toByteArray(usermac));
                                    Log.d("Service", ": 퍼블릭! : " + byteArrayToHexString(toByteArray(usermac)));

                                    //public, 처리필요없음
                                } else if (send_key != 0 && receive_key == 0) {
                                    //채팅방 생성 안한 상대한테 온 메시지
                                    //상대가 친구목록에 있는지 체크
                                    if (user == -1) {
                                        //없는유저
                                        Log.d("Service", ": 없는친구 : " + usermac);
                                        //일단 채팅방 만들고 못보낸다는 메시지 띄워주기
                                        //유저는 탐색되지 않은 유저로 친구목록에 생성
                                        Toast.makeText(getApplicationContext(), "친구 목록에 존재하지 않는 사용자입니다.\n친구목록 우측 상단의 리셋버튼을 눌러 검색해보세요!", Toast.LENGTH_LONG).show();
                                        user = DB.save_user("이친구와대화할수없습니다.", usermac);
                                        receive_key = DB.save_list_private(username, user);
                                    } else {
                                        //있는유저
                                        //채팅방 생성
                                        if (black > 0) {
                                            Log.d("Service", ": 있는유저 : 채팅방 생성 : " + user);
                                            receive_key = DB.save_list_private(username, user);
                                        } else
                                            break;
                                    }

                                    if (user == 0) // echo용
                                    {
                                        Log.d("Service", ": 에코용 : 나와의 고독한 대화방 : " + user);
                                        receive_key = DB.save_list_private("나와의 대화방" + DB.get_user_name(user), user);
                                    }
                                } else if (send_key == 0 && receive_key != 0) {
                                    // 에러
                                    Log.d("Service::ERR", String.valueOf(receive_key));
                                    Log.d("Service", ": 에러용에러용에러용 : " + user);
                                    break;
                                } else if (send_key != 0 && receive_key != 0) {
                                    //서로 채팅방이 생성되어있는 private
                                    Log.d("Service::채팅", String.valueOf(receive_key));
                                    Log.d("Service", ": 채팅방이 생성된 프라이빗 : " + user);
                                    // 블랙인지 아닌지 검색
                                    Log.d("Service::ROM", String.valueOf(receive_key));
                                    if (black > 0) {
                                        // 블랙 아닌경우
                                        // 채팅방 테이블에서 룸 넘버 == receive인 채팅방이
                                        // (채널 == 현재채널 && 유저 키 = return ) 인지 확인
                                        if (DB.check_list_key(receive_key, user) > 0) {
                                            //같으므로 룸넘버 receive에 채팅저장

                                            Log.d("Service::ROM", String.valueOf(receive_key));
                                        } else {
                                            //다르므로 새로 채팅방 생성하고 새로운 receive 저장.
                                            receive_key = DB.save_list_private(username, user);
                                        }
                                    } else {
                                        // 블랙인경우
                                        // 프로세스 종료.
                                        break;
                                    }
                                }

                                int ch = DB.get_net_View_ch();
                                int chatkey = DB.save_chatmsg(ch, receive_key, username, receivemsg, false, user);

                                Log.d("Service::ROM", "저장된 chatkey:" + chatkey + " 저장된 채널 :" + ch + " 저장된 recieve :" + receive_key +
                                        "저장된 유저네임 : " + username + " 저장된 메시지 :" + receivemsg + " 저장된 유저 키: " + user);
                                Provider_RecvMsg.getInstance().post(new Provider_RMf(chatkey));
                                DB.save_list_recievekey(send_key, receive_key);
                                /** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** */


                                Boolean push = DB.get_set_push();
                                Boolean vibe = DB.get_set_vibe();
                                Boolean sound = DB.get_set_sound();

                                if (push) {

                                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    Intent intent = new Intent(getApplicationContext(), Component_2_ChattingRoomMain.class);
                                    Intent intent_ = new Intent(getApplicationContext(), Service_PushPop.class);
                                    String address = MainActivity.RSP_MacAddr.toString();
                                    String time = DB.get_chat_time(ch, receive_key, chatkey);
                                    intent.putExtra("device_address", address);
                                    intent.putExtra("Room_key", receive_key);

                                    //   intent_.putExtra("NAME", username);
                                    //  intent_.putExtra("MSG", receivemsg);
                                    //  intent_.putExtra("TIME", time);
                                    //  intent_.putExtra("ROOMKEY", receive_key);
                                    //  intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    //  startActivity(intent_);

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
                            String id1 = byteArrayToHexString(tlqkf);
                            ch |= (tlqkf[1] & 0x7f);
                            ch <<= 8;
                            ch |= (tlqkf[2] & 0x00ff);
                            ch <<= 3;
                            ch |= (tlqkf[0] & 0x07);
                            Provider_SetCH.getInstance().post(new Provider_SCf(ch));
                            break;
                        case Service_Constants.MESSAGE_DISCOVERY:
                            discover((byte[]) msg.obj);
                            Log.d("DIscover:::", "bt Service 진입");
                            Provider_Discovery.getInstance().post(new Provider_DCf());
                            break;
                        case Service_Constants.MESSAGE_DEVICE_NAME:
                            // save the connected device's name
                            mConnectedDeviceName = msg.getData().getString(Service_Constants.DEVICE_NAME);

                            Toast.makeText(getApplicationContext(), "h : Connected to "
                                    + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            Provider_BlueOn.getInstance().post(new Provider_BOf(1));
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
                                Provider_BusProvider.getInstance().post(new Provider_BPf(myNI, myMac));
                            }
                            break;
                    }
                }
            };
        } else if (mChatService.mState != Service_BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), "h : 재연결 필요", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(getApplicationContext(), " h : 이미 연결되어있습니다.", Toast.LENGTH_LONG).show();

    }

    // 연결 끊겼을 때 변수 비워줘야함.
    public void set_connect() {

        if (mChatService == null) {                      // 챗섭스 객체가 없을때만 객체 생성
            // 계속 생성했기 때문에 끊겼다.
            mChatService = new Service_BluetoothChatService(getApplicationContext(), mHandler);
            mOutStringBuffer = new StringBuffer("");
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


            // 연결 직전 여기서 addr 셋팅
            if (MainActivity.RSP_MacAddr != "00:00:00:00:00:00")
                address = MainActivity.RSP_MacAddr;
            else
                MainActivity.RSP_MacAddr = "00:00:00:00:00:00";


            try {
                connectDevice(address, true);
                Toast.makeText(getApplicationContext(), "connected : " + address, Toast.LENGTH_SHORT).show();
                String BD = address.replaceAll(":", "");
                Log.d("BDA : ", BD);
                DB.save_bd(hexStringToByteArray(BD));

            } catch (Exception e) {
                //Toast.makeText(this, "error : " + e.toString(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, MainActivity.RSP_MacAddr, Toast.LENGTH_SHORT).show();
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
        int max = 28;
        int num_user = bytes.length / max;
        byte[] user = new byte[max];
        DB.delete_user_All();
        Log.d("Discovery:::", "삭제후" + String.valueOf(DB.get_user_num()));

        while (num_user > 0) {
            num_user--;
            int index = num_user * max;
            int i = 0;
            while (i < max) {
                int b = i + index;
                user[i] = bytes[b];
                i++;
            }
            Log.d("user : ", byteArrayToHexString(user));
            parse(user);
        }
    }

    public static byte[] toByteArray(long value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);
        return bytes;
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
        for (; i < 20; i++) {
            name_byte[i] = bytes[i];
        }
        Log.d("user - name byte : ", byteArrayToHexString(name_byte) + "\n name = " + new String(name_byte));

        ByteBuffer Lbuf = ByteBuffer.wrap(addr_byte);
        addr = Lbuf.getLong();

        name = new String(name_byte);
        Log.d("addr", String.valueOf(addr));
        int key = DB.save_user(name, addr);
        Log.d("Discovery:::", String.valueOf(key));
        Log.d("Discovery:::", "으악 늘어난다" + String.valueOf(DB.get_user_num()));

    }

    public static String byteArrayToHexString(byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {

            sb.append(String.format("%02X", b & 0xff));
        }

        return sb.toString();
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

}
