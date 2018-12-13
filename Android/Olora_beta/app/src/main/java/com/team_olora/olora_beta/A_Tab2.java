package com.team_olora.olora_beta;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

public class A_Tab2 extends Fragment {

    public C_DB DB = null;
    ListView listview;
    public ListRoomAdapter adapter = new ListRoomAdapter();
    InputMethodManager imm;
    TextView channel;
    ImageButton btnChannel;
    //dumy btn - mklee
    ImageButton dumy_make_chatlist;

    public A_Tab2() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.main_b_chatlist, container, false);
        channel = layout.findViewById(R.id.viewChannel);
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        btnChannel = layout.findViewById(R.id.button_channel);
        btnChannel.setOnClickListener(new Event());
        listview = layout.findViewById(R.id.List_listView);
        listview.setAdapter(adapter);
        listview.setChoiceMode(listview.CHOICE_MODE_SINGLE);
        listview.setOnItemClickListener(new Event());

        listview.setOnItemLongClickListener(new Event());

        //dumy btn - mklee
        dumy_make_chatlist = layout.findViewById(R.id.dumyCreateChatList);
        dumy_make_chatlist.setOnClickListener(new Event());
        //dumy_make_chatlist.setVisibility(View.GONE);

        DB = new C_DB(getContext());
        load_values();
        Provider_RecvMsg.getInstance().register(this);

        return layout;
    }

    @Override
    public void onDestroy() {
        Provider_RecvMsg.getInstance().unregister(this);
        super.onDestroy();
    }


    /*이 함수는 보통 사용자의 세션에서 유지되어야 하는 모든 변경사항을 저장하는 곳
     * 사용자가 프래그먼트를 떠나는 순간 시스템에서 호출한다.*/
    @Override
    public void onPause() {
        super.onPause();
    }

    private class Event implements DialogInterface.OnDismissListener, View.OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.button_channel:
                    A_Tab2_SelectCh PopupChannel = new A_Tab2_SelectCh();
                    PopupChannel.setOnDismissListener(this);
                    PopupChannel.show(getActivity().getSupportFragmentManager(), "A_Tab2_SelectCh");
                    break;

                case R.id.dumyCreateChatList:
                    //dumy btn - mklee
                    String userName = "dummy user";
                    int userKey = 999;
                    int key = DB.save_list_private(userName, userKey);
                    if (key > 0) {
                    } else {
                        Toast.makeText(getContext(), "채널이 설정되어 있는지 확인해주세요.", Toast.LENGTH_LONG).show();
                    }
                    adapter.addItem(ContextCompat.getDrawable(getContext(), R.drawable.tzui_icon), "dummy room", "yahoo" + key, key, userKey);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }


        // 대화창으로 이동하는 이벤트 함수
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(getContext(), A_Tab2_ChattingRoom.class);
            int roomkey = adapter.getRoomkey(position);
            int ch = DB.get_list_ch(roomkey);
            intent.putExtra("Room_ch", ch);
            intent.putExtra("Room_key", roomkey);
            intent.putExtra("User_key", adapter.getUserkey(position));
            intent.putExtra("device_address", A_MainActivity.RSP_MacAddr);

            try {
                getActivity().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "대화방에 들어갈 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (position != 0) {
                int roomKey = adapter.getRoomkey(position);
                String roomName = DB.get_list_name(roomKey);

                A_Tab2_DeleteChatRoom PopupDel = new A_Tab2_DeleteChatRoom();
                Bundle bundle = new Bundle(2);
                bundle.putString("RoomName", roomName);
                bundle.putInt("RoomKey", roomKey);
                PopupDel.setOnDismissListener(this);
                PopupDel.setArguments(bundle);
                PopupDel.show(getActivity().getSupportFragmentManager(), "A_Tab2_DeleteChatRoom");
            } else
                Toast.makeText(getContext(), "Public Room은 변경할 수 없습니다.", Toast.LENGTH_LONG).show();
            return true;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            Log.d("dismisstest", "onDismiss: tab2");
            load_values();
        }
    }

    @Subscribe
    public void receive_msg(Provider_RecvMsgFunc recvEvent) {
        load_values();
    }

    private void load_values() {
        Cursor cursor = DB.get_all_list_cursor();
        Cursor cursor2 = DB.get_ch_cursor_Current();
        adapter.clear();
        if (cursor2.moveToFirst()) {
            if (cursor2.getString(1) == null) {
                channel.setText("현재 채널 : (" + cursor2.getString(0) + ")");
            } else {
                channel.setText("현재 채널 : " + cursor2.getString(1));
            }
        } else {
            channel.setText("(채널을 설정해주세요.)");
        }

        if (cursor.moveToFirst()) {
            do {
                String room_name = cursor.getString(2);
                int ch = cursor.getInt(0);
                int room_key = cursor.getInt(1);
                int user_key = cursor.getInt(3);
                adapter.addItem(ContextCompat.getDrawable(getContext(), R.drawable.tzui_icon), room_name, "최근 대화한 채널 : " + Integer.toString(ch) + "채널", room_key, user_key);
            } while (cursor.moveToNext());
        }
        adapter.notifyDataSetChanged();
    }
}
