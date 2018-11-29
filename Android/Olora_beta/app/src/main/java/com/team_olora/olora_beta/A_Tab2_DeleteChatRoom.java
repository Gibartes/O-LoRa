package com.team_olora.olora_beta;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class A_Tab2_DeleteChatRoom extends android.support.v4.app.DialogFragment {
    private Button BtnSet, BtnDel;
    private ImageButton BtnClose;
    private C_DB DB = null;
    public String roomName;
    public int roomKey;
    private DialogInterface dialogInterface = getDialog();
    private DialogInterface.OnDismissListener listener;

    int checked = 0;

    public A_Tab2_DeleteChatRoom() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);

        final View view = inflater.inflate(R.layout.pupop_list_del, container, false);
        roomKey = getArguments().getInt("RoomKey");
        roomName = getArguments().getString("RoomName");
        BtnSet = view.findViewById(R.id.list_set);
        BtnDel = view.findViewById(R.id.list_del);
        BtnClose = view.findViewById(R.id.list_Close);
        BtnSet.setOnClickListener(new Event());
        BtnDel.setOnClickListener(new Event());
        BtnClose.setOnClickListener(new Event());

        DB = new C_DB(getContext());
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
            switch (v.getId()) {
                case R.id.list_set:
                    Intent intent = new Intent(getContext(), Component_123_ListSet.class);
                    intent.putExtra("MODE",1 );
                    intent.putExtra("prev_Name", roomName);
                    intent.putExtra("Key", roomKey);
                    Toast.makeText(getContext(), String.valueOf(roomKey), Toast.LENGTH_SHORT).show();

                    getActivity().startActivity(intent);
                    break;
                case R.id.list_del:
                    if(checked==2){
                    DB.delete_list(roomKey);
                    listener.onDismiss(dialogInterface);
                    dismiss();}else{
                        checked++;
                        Toast.makeText(getContext(),"삭제 버튼을 3번누르면 삭제됩니다. \n'"+ roomName+"' 채팅방을 정말 삭제하시겠습니까? :"+String.valueOf(checked),Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.list_Close:
                    listener.onDismiss(dialogInterface);
                    dismiss();
                    break;
            }
        }

    }

    public void setOnDismissListener(DialogInterface.OnDismissListener $listener) {
        listener = $listener;
    }

}