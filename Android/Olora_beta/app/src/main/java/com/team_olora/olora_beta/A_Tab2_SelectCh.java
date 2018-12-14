package com.team_olora.olora_beta;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class A_Tab2_SelectCh extends android.support.v4.app.DialogFragment {
    Button BtnConnect, BtnDel;
    ImageButton BtnClose;
    FloatingActionButton BtnPop;
    int callTab = 2;
    int dial = 0;
    C_DB DB = null;

    ListView channellistview;
    public ListChannelAdapter adapter = new ListChannelAdapter();

    private DialogInterface.OnDismissListener listener;

    int checked = -1;


    public A_Tab2_SelectCh() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        /*
        android.support.v4.app.FragmentManager fm = getFragmentManager();

        fm.addOnBackStackChangedListener(new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Intent intent = new Intent(getContext(), A_MainActivity.class);
                intent.putExtra("Page", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(intent);

            }
        });*/
        setCancelable(false);

        final View view = inflater.inflate(R.layout.popup_ch_select, container, false);
        callTab = getArguments().getInt("callTab");

        BtnConnect = (Button) view.findViewById(R.id.channelConnect);
        BtnConnect.setOnClickListener(new Event());
        BtnDel = (Button) view.findViewById(R.id.channelDelete);
        BtnDel.setOnClickListener(new Event());
        BtnPop = (FloatingActionButton) view.findViewById(R.id.btnPop);
        BtnPop.setOnClickListener(new Event());
        BtnClose = view.findViewById(R.id.channellistClose);
        BtnClose.setOnClickListener(new Event());

        channellistview = view.findViewById(R.id.channelList);
        channellistview.setAdapter(adapter);
        channellistview.setChoiceMode(channellistview.CHOICE_MODE_SINGLE);


        DB = new C_DB(getContext());
        load_values();
        return view;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }


    private class Event implements DialogInterface.OnDismissListener, View.OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
        @Override
        public void onClick(View v) {
            checked = channellistview.getCheckedItemPosition();
            switch (v.getId()) {
                case R.id.btnPop:
                    dial = 2;
                    A_Tab2_SelectCh_CreateNewCh PopupFragment = new A_Tab2_SelectCh_CreateNewCh();
                    PopupFragment.setOnDismissListener(this);
                    PopupFragment.show(getActivity().getSupportFragmentManager(), "A_Tab2_SelectCh_CreateNewCh");
                    break;
                case R.id.channelConnect:
                    if (checked != -1) {
                        if (Service_BluetoothChatService.mState == 3) {
                            int ch = adapter.getCh(checked);
                            //dummy chset -- lmk
                            DB.set_current_ch(ch);
                            DB.save_list_public();
                            //
                            Component_123_PopupProgress popupProgress = new Component_123_PopupProgress();
                            Bundle bundle = new Bundle(2);
                            bundle.putInt("dismiss", 1);
                            bundle.putInt("ChannelKey", ch);
                            popupProgress.setOnDismissListener(this);

                            popupProgress.setArguments(bundle);
                            dial = 1;
                            popupProgress.show(getActivity().getSupportFragmentManager(), "Progress");
                        }
                        else{
                            Toast.makeText(getContext(),"블루투스 연결이 필요합니다.",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                    }
                    channellistview.clearChoices();
                    break;
                case R.id.channelDelete:
                    if (checked != -1) {
                        DB.delete_ch(adapter.getCh(checked));
                        DB.delete_user_All();
                        adapter.remove(checked);
                        adapter.notifyDataSetChanged();
                    } else {
                    }
                    channellistview.clearChoices();
                    break;

                case R.id.channellistClose:
                    if(callTab==2) {
                        Intent intent = new Intent(getContext(), A_MainActivity.class);
                        intent.putExtra("Page", 1);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getActivity().startActivity(intent);
                    }else if(callTab==3){
                        Intent intent = new Intent(getContext(), A_MainActivity.class);
                        intent.putExtra("Page", 2);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getActivity().startActivity(intent);
                    }
                    break;
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            load_values();

            Log.d("dismisstest", "onDismiss: selectCh");
            if (dial == 1) {
                listener.onDismiss(dialog);
                dismiss();
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return false;
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener $listener) {
        Log.d("dismisstest", "onDismiss: selectCh2");
        listener = $listener;
    }

    private void load_values() {
        Cursor cursor = DB.get_ch_cursor();
        adapter.clear();
        while (cursor.moveToNext()) {
            int ch = cursor.getInt(0);
            String name = cursor.getString(1);
            adapter.addItem(ch, name);
        }
        adapter.notifyDataSetChanged();
    }
}