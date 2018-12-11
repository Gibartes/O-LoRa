package com.team_olora.olora_beta;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class A_Tab2_SelectCh_CreateNewCh extends android.support.v4.app.DialogFragment implements View.OnClickListener {
    EditText Netaddr, Channelname;
    Button BtnOK;
    C_DB DB = null;
    A_Tab2_SelectCh parent;

    private DialogInterface dialogInterface = getDialog();
    private DialogInterface.OnDismissListener listener;

    public A_Tab2_SelectCh_CreateNewCh() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.popup_ch_create, container, false);
        Netaddr = (EditText) view.findViewById(R.id.netAddress);
        Channelname = (EditText) view.findViewById(R.id.channelName);

        DB = new C_DB(getContext());

        BtnOK = (Button) view.findViewById(R.id.btnPopOK);
        BtnOK.setOnClickListener(this);

        return view;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        String ADDR = Netaddr.getText().toString();
        String Name = Channelname.getText().toString();

        if (listener == null) {
            dismiss();
        } else {
            if (ADDR.trim().length() != 0) {
                {
                    int addr = Integer.parseInt(ADDR);
                    int HP = addr & 0b0111;
                    int ID = addr & 0b1111111111111111111000;
                    ID >>= 3;

                    if (addr < 1 || addr > 262143) {
                        Toast.makeText(getContext(), "범위 내의 숫자를 입력해주세요.", Toast.LENGTH_LONG).show();
                        Netaddr.setText("");
                    } else if (DB.get_channel_exists(addr)==1) {
                        Toast.makeText(getContext(), "입력한 채널이 이미 존재합니다.", Toast.LENGTH_LONG).show();
                        Netaddr.setText("");
                    } else {
                        if (Name.trim().length() != 0) {
                            DB.save_ch(addr, Name);
                            listener.onDismiss(dialogInterface);
                            dismiss();
                        } else {
                            DB.save_ch(addr, null);
                            listener.onDismiss(dialogInterface);
                            dismiss();
                        }

                        //Toast.makeText(getContext(), "야호^^ HP : " + HP + "  ID :" + ID, Toast.LENGTH_LONG).show();
                    }
                }
            } else {
            }
        }

    }

    public void setOnDismissListener(DialogInterface.OnDismissListener $listener) {
        listener = $listener;
    }
}

