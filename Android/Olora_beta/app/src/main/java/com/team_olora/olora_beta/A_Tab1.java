package com.team_olora.olora_beta;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import static android.content.Context.BIND_AUTO_CREATE;

public class A_Tab1 extends android.support.v4.app.Fragment implements DialogInterface.OnDismissListener {

    ///////////////////
    ///// 멤버
    ///////////////////
    ImageButton callManual;
    ImageButton clear_all;
    ImageButton show_clear;
    int mod = 0;
    int view_ch;
    ImageButton edit_Name;
    ImageButton find_Xbee, connected_Xbee;
    LinearLayout nameBox, bdBox,welcomeTitle;
    TextView welcomeText, myName, find_Text, show_text, show_name;
    private C_DB DB = null;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private int blueon;

    //////////////// 커넥션 객체
    ServiceConnection sconn = new ServiceConnection() {

        // bind 되었을 때 - bind 만 한다. 서비스 객체에 셋 되어있는 핸들러, 챗섭스를 가져온다.(없을때만 생성!)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Toast.makeText(getApplicationContext(), "바운드 됨", Toast.LENGTH_LONG).show();
            Service_btService.myBinder mbinder = (Service_btService.myBinder) service;

            A_MainActivity.mbtService = mbinder.getSercive();
            A_MainActivity.mbtService.make_handler();
            A_MainActivity.mbtService.set_connect();
        }

        // unbind 되었을 때
        @Override
        public void onServiceDisconnected(ComponentName name) {

            //Toast.makeText(getApplicationContext(), "언바운드 됨", Toast.LENGTH_LONG).show();
            A_MainActivity.mbtService = null;
        }
    };
    //////////////// 커넥션 객체


    /*****************
     // bind 함수
     *****************/
    private void bind() {
        Intent bind = new Intent(getActivity(), Service_btService.class);
        getActivity().bindService(bind, sconn, BIND_AUTO_CREATE);
    }


    public A_Tab1() {
    }


    //******************** 권한 처리

    private boolean grantLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

                return false;
            }
        } else {
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= 23) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //resume tasks needing this permission
            }
        }
    }
    //******************** 권한 처리


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.main_a_connect, container, false);

        callManual = layout.findViewById(R.id.call_manual);
        callManual.setOnClickListener(new Event());
        edit_Name = layout.findViewById(R.id.editMyName);
        edit_Name.setOnClickListener(new Event());
        find_Text = layout.findViewById(R.id.findText);
        show_name = layout.findViewById(R.id.showBDName);
        show_text = layout.findViewById(R.id.showText);
        nameBox = layout.findViewById(R.id.NameLayout);
        bdBox = layout.findViewById(R.id.BDLayout);

        clear_all = layout.findViewById(R.id.clearall);
        clear_all.setOnClickListener(new Event());
        show_clear = layout.findViewById(R.id.showAllclear);
        show_clear.setOnClickListener(new Event());
        clear_all.setVisibility(View.GONE);

        welcomeTitle = layout.findViewById(R.id.welcome_title);
        welcomeText = layout.findViewById(R.id.welcome_text);
        find_Xbee = layout.findViewById(R.id.findXbee);
        find_Xbee.setOnClickListener(new Event());
        connected_Xbee = layout.findViewById(R.id.connectedXbee);
        connected_Xbee.setOnClickListener(new Event());

        myName = layout.findViewById(R.id.showMyName);
        connected_Xbee.setVisibility(View.GONE);
        show_text.setVisibility(View.GONE);

        DB = new C_DB(getContext());

        view_ch = DB.get_ch_Current();
        blueon = Service_BluetoothChatService.mState;

        if (blueon == 0) {
            connected_Xbee.setVisibility(View.GONE);
            show_text.setVisibility(View.GONE);
            nameBox.setVisibility(View.GONE);
            bdBox.setVisibility(View.GONE);

            welcomeTitle.setVisibility(View.VISIBLE);
            welcomeText.setVisibility(View.VISIBLE);
            find_Xbee.setVisibility(View.VISIBLE);
            find_Text.setVisibility(View.VISIBLE);
            //Toast.makeText(getContext(), "blueon = " + A_MainActivity.RSP_Name, Toast.LENGTH_LONG).show();
        } else if (blueon == 3) {
            if (A_MainActivity.RSP_Name.length() != 0)
                show_name.setText(A_MainActivity.RSP_Name);
            connected_Xbee.setVisibility(View.VISIBLE);
            show_text.setVisibility(View.VISIBLE);
            nameBox.setVisibility(View.VISIBLE);
            bdBox.setVisibility(View.VISIBLE);

            welcomeTitle.setVisibility(View.GONE);
            welcomeText.setVisibility(View.GONE);
            find_Xbee.setVisibility(View.GONE);
            find_Text.setVisibility(View.GONE);
            //Toast.makeText(getContext(), "blueon = " + A_MainActivity.RSP_Name, Toast.LENGTH_LONG).show();
        }


        load_values();

        return layout;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        blueon = Service_BluetoothChatService.mState;
        if (blueon == 3) {
            if (A_MainActivity.RSP_Name.length() != 0)
                show_name.setText(A_MainActivity.RSP_Name);
/*
            connected_Xbee.setVisibility(View.VISIBLE);
            show_text.setVisibility(View.VISIBLE);
            find_Text.setVisibility(View.GONE);
            find_Xbee.setVisibility(View.GONE);
            */
        } else {
            /*
            connected_Xbee.setVisibility(View.GONE);
            show_text.setVisibility(View.GONE);
            find_Text.setVisibility(View.VISIBLE);
            find_Xbee.setVisibility(View.VISIBLE);
            */
        }
    }

    private class Event implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String _name = myName.getText().toString();

            switch (v.getId()) {
                case R.id.call_manual:
                    Intent manual = new Intent(getContext(), A_manual.class);
                    startActivity(manual);
                    break;
                case R.id.showAllclear:
                    mod++;
                    switch (mod) {
                        case 1:

                            break;
                        case 2:
                            Toast.makeText(getContext(), "화면의 왼쪽 하단을 7회 클릭하면 데이터베이스 삭제 버튼이 나타납니다. 현재 :" + mod + "회", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(getContext(), "고생해 주신 분. 이명근 - Android UI. 현재 :" + mod + "회", Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            Toast.makeText(getContext(), "고생해 주신 분. 권홍균 - RaspberryPi Controlunit, 현재 :" + mod + "회", Toast.LENGTH_SHORT).show();

                            break;
                        case 5:
                            Toast.makeText(getContext(), "고생해 주신 분, 이민재 - Xbee  현재 :" + mod + "회", Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(getContext(), "고생해 주신 분, 김민수 - Android Service. 현재 :" + mod + "회", Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(getContext(), "사나를 누르면 모든 데이터베이스가 삭제됩니다.", Toast.LENGTH_SHORT).show();
                            clear_all.setVisibility(View.VISIBLE);
                            show_clear.setVisibility(View.GONE);
                            break;
                    }
                    break;


                case R.id.clearall:
                    DB.delete_user_All();
                    DB.delete_list_All();
                    DB.delete_ch_All();
                    DB.delete_chat();
                    DB.delete_black_all();
                    Intent resetDB = new Intent(getContext(), A_MainActivity.class);
                    resetDB.putExtra("Page", 0);
                    resetDB.setFlags(resetDB.FLAG_ACTIVITY_CLEAR_TASK | resetDB.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().startActivity(resetDB);
                    break;

                case R.id.editMyName:
                    Intent intent = new Intent(getContext(), A_123_Setting__Activity.class);
                    intent.putExtra("prev_Name", _name);
                    intent.putExtra("Key", 0); // 내 user key = 0
                    intent.putExtra("MODE", 0);
                    getActivity().startActivity(intent);
                    break;

                case R.id.findXbee:                      // 찾는 액티비티.
                    grantLocationPermission();
                    //////////////////
                    //// 서비스 재시작
                    //////////////////
                    Provider_BusProvider.getInstance().unregister(this);
                    getActivity().stopService(new Intent(getActivity(), Service_btService.class));

                    try {
                        getActivity().unbindService(sconn);
                    } catch (Exception e) {

                    }

                    Provider_BusProvider.getInstance().register(this);
                    Intent intent_s = new Intent(getActivity(), Service_btService.class);
                    getActivity().startService(intent_s);

                    Intent serverIntent = new Intent(getActivity(), A_Tab1_BtDeviceList.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                    break;

                case R.id.connectedXbee:                      // 찾는 액티비티.
                    final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                    alert.setTitle("OLora 재탐색");
                    alert.setMessage("다른 OLora 기기를 연결하시겠습니까?");
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            grantLocationPermission();
                            //////////////////
                            //// 서비스 재시작
                            //////////////////
                            //           Provider_BusProvider.getInstance().unregister(this);
                            try {
                                getActivity().unbindService(sconn);
                            } catch (Exception e) {
                            }
                            getActivity().stopService(new Intent(getActivity(), Service_btService.class));
                            //         Provider_BusProvider.getInstance().register(this);
                            Intent intent_s2 = new Intent(getActivity(), Service_btService.class);
                            getActivity().startService(intent_s2);


                            Intent serverIntent2 = new Intent(getActivity(), A_Tab1_BtDeviceList.class);
                            startActivityForResult(serverIntent2, REQUEST_CONNECT_DEVICE_SECURE);
                        }
                    });
                    alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
                    alert.show();

                    break;

            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Provider_BlueOn.getInstance().register(this);
        Provider_BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroyView() {
        Provider_BusProvider.getInstance().unregister(this);
        Provider_BlueOn.getInstance().unregister(this);

        super.onDestroyView();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When A_Tab1_BtDeviceList returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String addr = data.getExtras().getString(A_Tab1_BtDeviceList.EXTRA_DEVICE_ADDRESS);
                    String name = data.getExtras().getString(A_Tab1_BtDeviceList.EXTRA_DEVICE_NAME);
                    ((A_MainActivity) getActivity()).RSP_MacAddr = addr;
                    ((A_MainActivity) getActivity()).RSP_Name = name;


                    A_Tab1_BtDeviceList_connectingProgress bluetoothProgress = new A_Tab1_BtDeviceList_connectingProgress();
                    Bundle bundle = new Bundle(1);
                    bundle.putString("BDNAME", name);
                    bluetoothProgress.setArguments(bundle);
                    bluetoothProgress.setOnDismissListener(this);
                    bluetoothProgress.show(getActivity().getSupportFragmentManager(), "Progress");
                    // bind
                    try {
                        bind();
                    } catch (Exception e) {
                        //Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                    }

                    //connectDevice(data, true);
                    //Toast.makeText(getContext(), addr.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Subscribe
    public void A(Provider_BusProviderFunc activityResultEvent) {
        myName.setText(DB.get_user_Myname());
    }

    @Subscribe
    public void isBlueOn(Provider_BlueOnFunc isblue) {

        int Bo = isblue.getIsBlueOn();

        if (Bo == 1) {
            connected_Xbee.setVisibility(View.VISIBLE);
            show_text.setVisibility(View.VISIBLE);
            nameBox.setVisibility(View.VISIBLE);
            bdBox.setVisibility(View.VISIBLE);

            welcomeTitle.setVisibility(View.GONE);
            welcomeText.setVisibility(View.GONE);
            find_Xbee.setVisibility(View.GONE);
            find_Text.setVisibility(View.GONE);
            Toast.makeText(getContext(), "OLora에 연결됐습니다.", Toast.LENGTH_SHORT).show();
        } else {
            connected_Xbee.setVisibility(View.GONE);
            show_text.setVisibility(View.GONE);
            nameBox.setVisibility(View.GONE);
            bdBox.setVisibility(View.GONE);

            welcomeTitle.setVisibility(View.VISIBLE);
            welcomeText.setVisibility(View.VISIBLE);
            find_Xbee.setVisibility(View.VISIBLE);
            find_Text.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "OLora 연결이 끊어졌습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    private void load_values() {
        myName.setText(DB.get_user_Myname());
    }

}
