package com.team_olora.olora_beta;

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

public class A_Tab2 extends Fragment {

    public C_DB DB = null;
    ListView listview;
    public ListRoomAdapter adapter = new ListRoomAdapter();
    InputMethodManager imm;
    TextView channel;
    ImageButton btnChannel;


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
        DB = new C_DB(getContext());
        load_values();

        return layout;
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
            }
        }


        // 대화창으로 이동하는 이벤트 함수
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(getContext(), A_Tab2_ChattingRoom.class);
            intent.putExtra("Room_key", adapter.getRoomkey(position));
            intent.putExtra("User_key",adapter.getUserkey(position));
            intent.putExtra("device_address", A_MainActivity.RSP_MacAddr);

            try
            {getActivity().startActivity(intent);}
            catch (Exception e)
            {
                Toast.makeText(getActivity(), "연결할 장치를 선택해주세요", Toast.LENGTH_SHORT).show();
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
            load_values();
        }
    }

    private void load_values() {
        Cursor cursor = DB.get_list_cursor();
        Cursor cursor2 = DB.get_net_Current();
        adapter.clear();
        if (cursor2.moveToFirst()) {
            if (cursor2.getString(2) == null) {
                channel.setText("채널 : (" + cursor2.getString(1) + ")");
            } else {
                channel.setText("채널 : " + cursor2.getString(2));
            }
        } else {
            channel.setText("(채널을 설정해주세요.)");
        }

        if (cursor.moveToFirst()) {
            do {
                String room_name = cursor.getString(2);
                int room_key = cursor.getInt(1);
                int user_key = cursor.getInt(3);
                adapter.addItem(ContextCompat.getDrawable(getContext(), R.drawable.tzui_icon), room_name, "yahoo" + room_key, room_key,user_key);
            } while (cursor.moveToNext());
        }
        adapter.notifyDataSetChanged();
    }
}
