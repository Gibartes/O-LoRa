package com.team_olora.olora_beta;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.Timer;
import java.util.TimerTask;

public class A_Tab1_BtDeviceList_connectingProgress extends android.support.v4.app.DialogFragment {

    private int timer_sec = 4, btConnecting = 0;
    private TimerTask second;
    private TextView BtConnectSeconds;
    private final Handler handler = new Handler();

    TextView ProgressTitle, progBdaddress;
    LinearLayout progLinear, progLinear2;
    private DialogInterface dialogInterface = getDialog();
    private DialogInterface.OnDismissListener listener;

    Button Nayoen;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        final View view = inflater.inflate(R.layout.popup_progress, container, false);

        Nayoen = view.findViewById(R.id.nayoenBtn);
        Nayoen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDismiss(dialogInterface);
                btConnecting = 0;
                dismiss();

            }
        });
        //  Nayoen.setVisibility(View.GONE);

        BtConnectSeconds = view.findViewById(R.id.btConnectSeconds);
        BtConnectSeconds.setVisibility(View.VISIBLE);
        btConnecting=1;
        time_run(6);

        ProgressTitle = view.findViewById(R.id.progresstitle);
        ProgressTitle.setText("블루투스 연결");
        progLinear = view.findViewById(R.id.progLinear);
        progLinear.setVisibility(View.GONE);
        progLinear2 = view.findViewById(R.id.progLinear2);
        progLinear2.setVisibility(View.VISIBLE);
        String BDname = getArguments().getString("BDNAME");
        progBdaddress= view.findViewById(R.id.progBdaddress);
        progBdaddress.setText(BDname);

        // key = getArguments().getInt("ChannelKey");
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Provider_BlueOn.getInstance().register(this);
    }

    @Override
    public void onDestroyView() {
        Provider_BlueOn.getInstance().unregister(this);
        super.onDestroyView();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener $listener) {
        listener = $listener;
    }

    @Subscribe
    public void isBlueOn(Provider_BlueOnFunc isblue) {

        int Bo = isblue.getIsBlueOn();

        if (Bo == 1) {
            btConnecting=0;
            listener.onDismiss(dialogInterface);
            dismiss();
        } else {
            Toast.makeText(getContext(), "장치에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
            listener.onDismiss(dialogInterface);
            dismiss();
        }

    }

    protected void time_run(int default_time) {
        timer_sec = default_time;
        second = new TimerTask() {
            @Override
            public void run() {
                Update();
                timer_sec--;
            }
        };
        Timer timer = new Timer();
        timer.schedule(second, 0, 1000);
    }

    protected void Update() {
        Runnable updater = new Runnable() {
            public void run() {
                BtConnectSeconds.setText(Integer.toString(timer_sec));
                if (timer_sec < 1 & btConnecting == 1) {
                    timer_sec = 10;
                    btConnecting = 0;
                    Toast.makeText(getContext(), "설정에 실패했습니다. \n연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        };
        handler.post(updater);
    }

}
