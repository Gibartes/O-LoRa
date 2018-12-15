package com.team_olora.olora_beta;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.EventLog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class A_manual extends AppCompatActivity {
    TextView slaSh;
    Button prevBtn, nextBtn;
    TextView pageNum;
    RelativeLayout manual0;
    ImageView manual1, manual2, manual3, manual4, manual5;
    private int curPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_manual);

        pageNum = findViewById(R.id.page_num);

        prevBtn = findViewById(R.id.prevBtn);
        slaSh = findViewById(R.id.slash);
        nextBtn = findViewById(R.id.nextBtn);

        manual0 = findViewById(R.id.m0);
        manual1 = findViewById(R.id.m1);
        manual2 = findViewById(R.id.m2);
        manual3 = findViewById(R.id.m3);
        manual4 = findViewById(R.id.m4);
        manual5 = findViewById(R.id.m5);

        prevBtn.setOnClickListener(new Event());
        nextBtn.setOnClickListener(new Event());
    }

    private class Event implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            {
                switch (v.getId()) {
                    case R.id.prevBtn:
                        curPage -= 1;
                        if(curPage<0)
                            curPage=0;
                        load_page(curPage);
                        pageNum.setText("("+Integer.toString(curPage)+"/5)");
                        break;
                    case R.id.nextBtn:
                        curPage += 1;
                        load_page(curPage);
                        pageNum.setText("("+Integer.toString(curPage)+"/5)");
                        break;
                }

            }
        }
    }

    private void load_page(int page) {
        switch (page) {
            case 0:
                pageNum.setVisibility(View.GONE);
                prevBtn.setVisibility(View.GONE);
                slaSh.setVisibility(View.GONE);
                manual0.setVisibility(View.VISIBLE);
                manual1.setVisibility(View.GONE);
                break;
            case 1:
                pageNum.setVisibility(View.VISIBLE);
                prevBtn.setVisibility(View.VISIBLE);
                slaSh.setVisibility(View.VISIBLE);
                manual0.setVisibility(View.GONE);
                manual1.setVisibility(View.VISIBLE);
                manual2.setVisibility(View.GONE);
                break;
            case 2:
                manual1.setVisibility(View.GONE);
                manual2.setVisibility(View.VISIBLE);
                manual3.setVisibility(View.GONE);
                break;
            case 3:
                manual2.setVisibility(View.GONE);
                manual3.setVisibility(View.VISIBLE);
                manual4.setVisibility(View.GONE);
                break;
            case 4:
                manual3.setVisibility(View.GONE);
                manual4.setVisibility(View.VISIBLE);
                manual5.setVisibility(View.GONE);
                break;
            case 5:
                manual4.setVisibility(View.GONE);
                manual5.setVisibility(View.VISIBLE);
                break;
            case 6:
                pageNum.setVisibility(View.GONE);
                Intent intent = new Intent(this, A_MainActivity.class);
                intent.putExtra("Page", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }

}
