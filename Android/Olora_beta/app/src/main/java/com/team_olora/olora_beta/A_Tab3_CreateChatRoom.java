package com.team_olora.olora_beta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class A_Tab3_CreateChatRoom extends android.support.v4.app.DialogFragment {
    private ImageButton connect, ignore;
    private ImageButton userSet;
    private TextView user;
    private C_DB DB = null;

    private String userName;
    private int userKey;

    public A_Tab3_CreateChatRoom() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        final View view = inflater.inflate(R.layout.popup_friendclick, container, false);

        userSet = view.findViewById(R.id.userSet);
        user = view.findViewById(R.id.chatUser);
        connect = view.findViewById(R.id.chatConnect);
        ignore = view.findViewById(R.id.chatIgnore);
        connect.setOnClickListener(new Event());
        ignore.setOnClickListener(new Event());
        userSet.setOnClickListener(new Event());

        userName = getArguments().getString("userName");
        userKey = getArguments().getInt("userKey", 0);
        user.setText(userName);

        DB = new C_DB(getContext());
        return view;
    }


    private class Event implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.chatConnect:
                    int room_key=DB.save_list_private(userName,userKey);
                    if (room_key > 0) {
                    } else {
                        Toast.makeText(getContext(), "채널이 설정되어 있는지 확인해주세요.", Toast.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent(getContext(), A_Tab2_ChattingRoom.class);
                    int ch = DB.get_list_ch(room_key);
                    intent.putExtra("Room_ch", ch);
                    intent.putExtra("Room_key", room_key);
                    intent.putExtra("User_key", userKey);
                    intent.putExtra("device_address", A_MainActivity.RSP_MacAddr);

                    try {
                        getActivity().startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "대화방에 들어갈 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.chatIgnore:
                    DB.save_black(userKey);
                    Toast.makeText(getContext(), "BlackLIst", Toast.LENGTH_SHORT).show();
                    Intent setblack = new Intent(getContext(), A_MainActivity.class);
                    setblack.putExtra("Page", 2);
                    setblack.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().startActivity(setblack);

                    break;
                case R.id.userSet:
                    Intent intent1 = new Intent(getContext(), A_123_Setting__Activity.class);
                    intent1.putExtra("prev_Name", userName);
                    intent1.putExtra("Key", userKey);
                    intent1.putExtra("MODE", 2);
                    getActivity().startActivityForResult(intent1, 0);
                    break;
            }
        }

    }

}