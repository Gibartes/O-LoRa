package com.team_olora.olora_beta;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

    private ImageButton btnChannel;
    private ImageButton btnReset;

    // dumy btn - lmk
    ImageButton dummyFrieandBtn;
    private C_DB DB = null;

    private ListView listview;
    private ListFriendAdapter adapter = new ListFriendAdapter();

    public A_Tab3() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.main_c_friendlist, container, false);


        //dumy btn - lmk
        dummyFrieandBtn = layout.findViewById(R.id.dumyCreateFriendList);
        dummyFrieandBtn.setOnClickListener(new Event());
        dummyFrieandBtn.setVisibility(View.GONE);

        listview = layout.findViewById(R.id.List_friendView);
        listview.setAdapter(adapter);
        listview.setChoiceMode(listview.CHOICE_MODE_SINGLE);

        listview.setOnItemClickListener(new Event());
        listview.setOnItemLongClickListener(new Event());

        btnChannel = layout.findViewById(R.id.button_channel);
        btnReset = layout.findViewById(R.id.button_discovery);
        btnChannel.setOnClickListener(new Event());
        btnReset.setOnClickListener(new Event());

        viewChannel = layout.findViewById(R.id.viewCH);
        DB = new C_DB(getContext());
        load_values();

        return layout;
    }

    private class Event implements DialogInterface.OnDismissListener, View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_channel:
                    A_Tab2_SelectCh PopupChannel = new A_Tab2_SelectCh();
                    Bundle bundle_ch = new Bundle(1);
                    bundle_ch.putInt("callTab", 3);
                    PopupChannel.setOnDismissListener(this);
                    PopupChannel.setArguments(bundle_ch);
                    PopupChannel.show(getActivity().getSupportFragmentManager(), "A_Tab3_SelectCh");
                    break;


                case R.id.button_discovery:
                    final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                    int dclv = DB.get_set_dclv();
                    alert.setTitle("채널 탐색");
                    alert.setMessage("같은 채널을 사용하는 유저를 탐색합니다.\n약 "+dclv+"초가 소요됩니다.");
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Service_BluetoothChatService.mState == 3) {
                                Component_123_PopupProgress popupProgress = new Component_123_PopupProgress();
                                Bundle bundle = new Bundle(2);
                                bundle.putInt("dismiss", 2);
                                int key = DB.get_ch_Current();
                                bundle.putInt("ChannelKey", key);

                                popupProgress.setArguments(bundle);
                                popupProgress.show(getActivity().getSupportFragmentManager(), "Progress");
                            } else {
                                Toast.makeText(getContext(), "블루투스 연결이 필요합니다.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
                    alert.show();


                    break;
                case R.id.dumyCreateFriendList:
                    //dumy btn - mklee
                    long dumyAddr = Math.round(Math.random() * 100);
                    int key = DB.save_user("이민재", dumyAddr);
                    load_values();
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
            popupFriend.setArguments(bundle);
            popupFriend.show(getActivity().getSupportFragmentManager(), "Friend");
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return false;
        }


        @Override
        public void onDismiss(DialogInterface dialog) {
            Log.d("dismisstest", "onDismiss: tab3");
            load_values();
        }
    }


    private void load_values() {
        Cursor cursor = DB.get_ch_cursor_Current();
        Cursor cursor1 = DB.get_user_cursor();
        adapter.clear();

        if (cursor.moveToFirst()) {
            if (cursor.getString(1) == null) {
                viewChannel.setText( cursor.getInt(0) + " 채널");
            } else {
                viewChannel.setText(cursor.getString(1)+ " 채널");
            }
        } else {
            viewChannel.setText("(채널을 설정해주세요.)");
        }


        if (cursor1.moveToFirst()) {
            do {
                int user_key = cursor1.getInt(0);
                String user_name = cursor1.getString(1);
                String user_desc = cursor1.getString(2);
                adapter.addItem(ContextCompat.getDrawable(getContext(), R.drawable.friend), user_name, user_desc, user_key);
            } while (cursor1.moveToNext());
        }
        adapter.notifyDataSetChanged();
    }


}





