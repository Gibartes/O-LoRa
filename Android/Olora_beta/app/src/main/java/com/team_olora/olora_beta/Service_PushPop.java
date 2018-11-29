package com.team_olora.olora_beta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class Service_PushPop extends Activity {

    public static int num_msg = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DIM_BEHIND
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setAttributes(layoutParams);
        layoutParams.dimAmount = 0.6f;

        setContentView(R.layout.activity_service__push_pop);


        num_msg = 0;
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME"); /// 모드 인텐트 전하는거 !
        String msg = intent.getStringExtra("MSG");
        String time = intent.getStringExtra("TIME");
        int room_key = intent.getIntExtra("ROOMKEY", 0);

        ImageView icon = findViewById(R.id.popIcon);
        TextView poptime = findViewById(R.id.popTime);
        TextView popname = findViewById(R.id.popName);
        TextView popmsg = findViewById(R.id.popMsg);

        icon.setImageResource(R.drawable.sana_icon);
        poptime.setText(time);
        popname.setText(name);
        popmsg.setText(msg);
    }
}
