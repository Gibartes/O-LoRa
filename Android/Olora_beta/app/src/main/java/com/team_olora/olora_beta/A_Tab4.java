package com.team_olora.olora_beta;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

public class A_Tab4 extends Fragment {
    public static char setting = 0;
    Switch set_push, set_vibration, set_sound;
    Button set_blockList, set_DcLv, set_DataLv, set_orolaReboot;
    C_DB DB = null;

    public A_Tab4() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.main_d_setting, container, false);

        final Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        DB = new C_DB(getContext());
        set_push = layout.findViewById(R.id.setPush);
        set_vibration = layout.findViewById(R.id.setVibration);
        set_sound = layout.findViewById(R.id.setSound);

        set_blockList = layout.findViewById(R.id.setBlockList);
        set_DcLv = layout.findViewById(R.id.setDcLv);
        set_DataLv = layout.findViewById(R.id.setMsgLv);
        set_orolaReboot = layout.findViewById(R.id.setOloraReboot);

        set_push.setChecked(DB.get_set_push());
        set_vibration.setChecked(DB.get_set_vibe());
        set_sound.setChecked(DB.get_set_sound());

        set_push.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getContext(), "푸시알림 ON ", Toast.LENGTH_SHORT).show();
                    DB.save_push(1);
                } else {
                    Toast.makeText(getContext(), "푸시알림 OFF", Toast.LENGTH_SHORT).show();
                    DB.save_push(0);
                }
            }
        });
        set_vibration.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getContext(), "진동알림 ON", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(500);
                    DB.save_vibe(1);
                } else {
                    Toast.makeText(getContext(), "진동알림 OFF", Toast.LENGTH_SHORT).show();
                    vibrator.cancel();
                    DB.save_vibe(0);
                }
            }
        });
        set_sound.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getContext(), "소리알림 ON", Toast.LENGTH_SHORT).show();
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
                    r.play();
                    DB.save_sound(1);
                } else {
                    Toast.makeText(getContext(), "소리알림 OFF", Toast.LENGTH_SHORT).show();
                    DB.save_sound(0);
                }
            }
        });

        set_blockList.setOnClickListener(new Event());
        set_DcLv.setOnClickListener(new Event());
        set_DataLv.setOnClickListener(new Event());
        set_orolaReboot.setOnClickListener(new Event());

        return layout;
    }

    private class Event implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.setOloraReboot:

                    final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("OLora Reboot");
                    alert.setMessage("OLora 단말기와의 블루투스 연결을 초기화합니다.");
                    alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getActivity(), "서비스가 종료되었습니다.\n재연결을 원하시면 새로운 기기를 찾아주세요.", Toast.LENGTH_LONG).show();
                            Intent stop = new Intent(v.getContext(), Service_btService.class);
                            getActivity().stopService(stop);
                        }
                    });
                    alert.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
                    alert.show();


                    break;
                case R.id.setBlockList:
                    Intent blacklist = new Intent(v.getContext(), A_Tab4_BlackList.class);
                    startActivity(blacklist);
                    break;
                case R.id.setDcLv:
                    Intent setdclv = new Intent(getContext(), A_123_Setting__Activity.class);
                    setdclv.putExtra("prev_Name", "");
                    setdclv.putExtra("Key", "");
                    setdclv.putExtra("MODE", 3);
                    getActivity().startActivityForResult(setdclv, 0);
                    break;
            }
        }

    }
}

// 1 //  32767


