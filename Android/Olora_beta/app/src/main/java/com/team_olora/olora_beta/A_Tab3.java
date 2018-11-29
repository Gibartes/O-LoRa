package com.team_olora.olora_beta;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class A_Tab3 extends android.support.v4.app.Fragment {
    private TextView viewChannel;
    private ImageButton reset;
    private C_DB DB = null;

    private ListView listview;
    private ListFriendAdapter adapter = new ListFriendAdapter();
    private EditText txt;
    private Button btnAdd, btnDel;

    public A_Tab3() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.main_c_friendlist, container, false);

        txt = layout.findViewById(R.id.txt);
        btnAdd = layout.findViewById(R.id.btnAdd);
        btnDel = layout.findViewById(R.id.btnDel);
        btnAdd.setOnClickListener(new Event());
        btnDel.setOnClickListener(new Event());
        btnAdd.setVisibility(View.GONE);
        btnDel.setVisibility(View.GONE);
        txt.setVisibility(View.GONE);
        listview = layout.findViewById(R.id.List_friendView);
        listview.setAdapter(adapter);
        listview.setChoiceMode(listview.CHOICE_MODE_SINGLE);

        listview.setOnItemClickListener(new Event());
        listview.setOnItemLongClickListener(new Event());

        reset = layout.findViewById(R.id.button_reset1);
        reset.setOnClickListener(new Event());

        viewChannel = layout.findViewById(R.id.viewCH);
        DB = new C_DB(getContext());
        load_values();

        return layout;
    }

    private class Event implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnAdd:
                    String str;
                    str = txt.getText().toString();
                    int size = adapter.getCount() + 1;
                    if (str.trim().length() != 0) {
                        int B = DB.save_user(str, 11); // user mac address
                        load_values();
                        txt.setText("");
                    }
                    break;
                case R.id.btnDel:
                    DB.delete_user_All();
                    adapter.clear();
                    load_values();
                    break;

                case R.id.button_reset1:

                    if (Service_BluetoothChatService.mState == 3) {
                        Component_123_PopupProgress popupProgress = new Component_123_PopupProgress();
                        Bundle bundle = new Bundle(2);
                        bundle.putInt("dismiss", 2);
                        int key = DB.get_net_Current_key();
                        bundle.putInt("ChannelKey",key);

                        popupProgress.setArguments(bundle);
                        popupProgress.show(getActivity().getSupportFragmentManager(), "Progress");
                    }
                    else{
                        Toast.makeText(getContext(),"블루투스 연결이 필요합니다.",Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = adapter.getName(position);
            int key = adapter.getUserkey(position);

            A_Tab3_CreateChatRoom popupFriend = new A_Tab3_CreateChatRoom();
            Bundle bundle = new Bundle(3);
            bundle.putString("userName", name);
            bundle.putInt("userKey", key);
            Log.d("MSGMSG: - putbundle ","userName = "+name+"   userKey = "+key);
            popupFriend.setArguments(bundle);
            popupFriend.show(getActivity().getSupportFragmentManager(), "Friend");
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return false;
        }
    }

    private void load_values() {
        Cursor cursor = DB.get_net_Current();
        Cursor cursor1 = DB.get_user_cursor();
        adapter.clear();

        if (cursor.moveToFirst()) {
            if (cursor.getString(2) == null) {
                viewChannel.setText("채널 : (" + cursor.getString(1) + ")");
            } else {
                viewChannel.setText("채널 : " + cursor.getString(2));
            }
        } else {
            viewChannel.setText("(채널을 설정해주세요.)");
        }


        if (cursor1.moveToFirst()) {
            do {
                int user_key = cursor1.getInt(0);
                String user_name = cursor1.getString(1);
                String user_desc = cursor1.getString(2);
                adapter.addItem(ContextCompat.getDrawable(getContext(), R.drawable.nayeon_icon), user_name, user_desc, user_key);
            } while (cursor1.moveToNext());
        }
        adapter.notifyDataSetChanged();
    }


}





