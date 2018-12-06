package com.team_olora.olora_beta;

import android.content.DialogInterface;
import android.os.Bundle;
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

public class A_Tab1_BtDeviceList_connectingProgress extends android.support.v4.app.DialogFragment {

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
                dismiss();

            }
        });

      //  Nayoen.setVisibility(View.GONE);

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
            listener.onDismiss(dialogInterface);
            dismiss();
        } else {
            Toast.makeText(getContext(), "장치에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
            listener.onDismiss(dialogInterface);
            dismiss();
        }

    }

}
