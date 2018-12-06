package com.team_olora.olora_beta;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

public class Component_123_PopupProgress extends android.support.v4.app.DialogFragment {

    Button nayeonBtn;
    private int callTab;
    private int key;
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

        nayeonBtn = view.findViewById(R.id.nayoenBtn);
        nayeonBtn.setOnClickListener(new Event());
 //       nayeonBtn.setVisibility(View.GONE);
        DB = new C_DB(getContext());

        ch = DB.get_net_ch(key);
/**
 *
 *   여기서 ch 파싱해서 명령어로
 *
 * */
        if (callTab == 1) {
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

            Log.d("Discovery:::", "채널 입력 = " + ch);

            /** HP ID 순서대로 1 , 2바이트*/
            A_MainActivity.mbtService.mChatService.write(packet.converted_packet(s,d , "SET_CH", HPbyte[0], IDbyte, CH));
        } else if (callTab == 2) {
            ch = DB.get_net_Current_ch();
            A_MainActivity.mbtService.mChatService.write(packet.converted_packet(s, d, "START_DISCOVERY", HPbyte[0], IDbyte, IDbyte));
        }

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
            Toast.makeText(getContext(), "T.W.I.C.E.!", Toast.LENGTH_LONG);
            /*Intent intent = new Intent(getContext(), A_MainActivity.class);
            intent.putExtra("Page", dismiss);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getActivity().startActivity(intent);*/
            if (callTab == 1) {
                Intent intent1 = new Intent(getContext(), A_MainActivity.class);
                intent1.putExtra("Page", 1);


                Intent intent = new Intent(getContext(), A_Tab2_ChattingRoom.class);
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
            }
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
        Toast.makeText(getContext(), "CHANNEL:" + setchannel, Toast.LENGTH_LONG).show();
        Log.d("Discovery", "set ch ok");

        ch = DB.get_net_Current_ch();
        A_MainActivity.mbtService.mChatService.write(packet.converted_packet(s, d, "START_DISCOVERY", HPbyte[0], IDbyte, null));

    }

    @Subscribe
    public void dodiscovery(Provider_DiscoveryFunc dcf) {
        /**디스커버리 성공시 아래 실행**/

        if (callTab == 1) {
            Intent intent1 = new Intent(getContext(), A_MainActivity.class);
            intent1.putExtra("Page", 1);

            Intent intent = new Intent(getContext(), A_Tab2_ChattingRoom.class);
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
        }
        dismiss();
    }
}
