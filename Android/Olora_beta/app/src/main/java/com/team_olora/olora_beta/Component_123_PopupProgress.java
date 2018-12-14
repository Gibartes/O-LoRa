package com.team_olora.olora_beta;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Component_123_PopupProgress extends android.support.v4.app.DialogFragment {
    private int dc_timeout;
    private int timer_sec = 10;
    private TimerTask second;
    private TextView DiscoverySeconds;
    private final Handler handler = new Handler();

    int discoveryRunning = 0;

    Button nayeonBtn;
    private int callTab;
    private int ch;
    private DialogInterface dialogInterface = getDialog();
    private DialogInterface.OnDismissListener listener;
    private C_DB DB = null;

    Service_packet packet = new Service_packet();
    String s = A_MainActivity.addr_self;

    byte[] d = packet.converted_addr(A_MainActivity.RSP_MacAddr);

    int HP;
    int ID;

    byte[] HPbyte = new byte[1];
    byte[] IDbyte = new byte[2];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);

        final View view = inflater.inflate(R.layout.popup_progress, container, false);

        callTab = getArguments().getInt("dismiss");
        ch = getArguments().getInt("ChannelKey");

        nayeonBtn = view.findViewById(R.id.nayoenBtn);
        nayeonBtn.setOnClickListener(new Event());
        DB = new C_DB(getContext());

        DiscoverySeconds = view.findViewById(R.id.discoverySec);
        DiscoverySeconds.setVisibility(View.VISIBLE);


        discoveryRunning = 1;
        dc_timeout = DB.get_set_dclv();
        time_run(dc_timeout);


/**
 *
 *   여기서 ch 파싱해서 명령어로
 *
 * */

        // 파싱.
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
        // 셋 파라미터 명령어 실행.
        // HPbyte / IDbyte


        /** HP ID 순서대로 1 , 2바이트*/
        byte[] CH_send = new byte[952];
        Arrays.fill(CH_send, (byte) 0);

        CH_send[0] = CH[0];
        CH_send[1] = CH[1];
        CH_send[2] = CH[2];

        Log.d("finalTest", "CH _ send " + packetHandler.byteArrayToHexString(CH_send));

        // get hash
        byte[] hash = FuncGroup.getHash(CH_send);
        byte[] setCHpacket = null;
        setCHpacket = packet.converted_packet(s, d, "SET_CH", HPbyte[0], IDbyte, hash, CH);

        Log.d("finalTest", "--\n\n-----------------Start CH_set Packet -----------------"
                + "\n" + "msg send : " + packetHandler.byteArrayToHexString(setCHpacket)
                + "\n" + "src : " + packetHandler.getHeaderString(setCHpacket, 0, packetHandler.LEN_SRC)
                + "\n" + "dest : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_DST, packetHandler.LEN_DST)
                + "\n" + "cm : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_CM, packetHandler.LEN_CM)
                + "\n" + "hp : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_HP, packetHandler.LEN_HP)
                + "\n" + "proto : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_PROTO, packetHandler.LEN_PROTO)
                + "\n" + "id : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_ID, packetHandler.LEN_ID)
                + "\n" + "flags : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_FLAGS, packetHandler.LEN_FLAGS)
                + "\n" + "frag : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_FRAG, packetHandler.LEN_FRAG)
                + "\n" + "seq : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_SEQ, packetHandler.LEN_SEQ)
                + "\n" + "tms : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_TMS, packetHandler.LEN_TMS)
                + "\n" + "len : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_LEN, packetHandler.LEN_LEN)
                + "\n" + "ttl : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_TTL, packetHandler.LEN_TTL)
                + "\n" + "param : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_PARAM, packetHandler.LEN_PARAM)
                + "\n" + "dc : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_DC, packetHandler.LEN_DC)
                + "\n" + "#####################   END CH_set Packet #################### \n\n");
        int dataLen = packetHandler.getMsgLen(setCHpacket);
        Log.d("finalTest", "len2 : " + dataLen);
        Log.d("finalTest", "data : " + packetHandler.getHeaderString(setCHpacket, packetHandler.MASK_DATA, dataLen));

        /****/

        /** HP ID 순서대로 1 , 2바이트*/
        A_MainActivity.mbtService.mChatService.write(setCHpacket);

        return view;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    private class Event implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (callTab == 1) {
                goToTab2();
            } else
                discoveryRunning = 0;
            dismiss();
        }

    }

    public void setOnDismissListener(DialogInterface.OnDismissListener $listener) {
        listener = $listener;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Provider_Discovery.getInstance().register(this);
        Provider_SetCH.getInstance().register(this);
    }

    @Override
    public void onDestroyView() {
        Provider_SetCH.getInstance().unregister(this);
        Provider_Discovery.getInstance().unregister(this);
        super.onDestroyView();
    }

    @Subscribe
    public void isChSet(Provider_SetCHFunc scf) {
        int setchannel = scf.getChannel();
        /**셋 채널 성공시 아래 실행**/

        byte BB[] = new byte[0];
        byte[] BB_send = new byte[952];
        Arrays.fill(BB_send, (byte) 0);

        // get hash
        byte[] hash = FuncGroup.getHash(BB_send);

        A_MainActivity.mbtService.mChatService.write(packet.converted_packet(s, d, "START_DISCOVERY", HPbyte[0], IDbyte, hash, BB));
    }

    @Subscribe
    public void dodiscovery(Provider_DiscoveryFunc dcf) {
        /**디스커버리 성공시 아래 실행**/
        if (discoveryRunning == 1) {
            if (callTab == 1) {
                goToTab2();
            } else
                discoveryRunning = 0;
            dismiss();
        }
    }

    public void goToTab2() {
        discoveryRunning = 0;
        Intent intent1 = new Intent(getContext(), A_MainActivity.class);
        intent1.putExtra("Page", 1);

        Intent intent = new Intent(getContext(), A_Tab2_ChattingRoom.class);
        intent.putExtra("Room_ch", ch);
        intent.putExtra("Room_key", 0);
        intent.putExtra("User_key", 0);
        intent.putExtra("device_address", A_MainActivity.RSP_MacAddr);

        getActivity().startActivity(intent1);

        try {
            getActivity().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "연결할 장치를 선택해주세요", Toast.LENGTH_SHORT).show();
        }

        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        /***지금은 메인액티비티도 열고 채팅룸도 열게 했음.. 개선이 필요할지도**/
        listener.onDismiss(dialogInterface);
        dismiss();
    }

    protected void time_run(int default_time) {
        timer_sec = default_time;
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
                DiscoverySeconds.setText(Integer.toString(timer_sec));
                if (timer_sec < 1 & discoveryRunning == 1) {
                    timer_sec = 10;
                    discoveryRunning = 0;
                    Toast.makeText(getContext(), "설정에 실패했습니다. \n연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        };
        handler.post(updater);
    }

}
