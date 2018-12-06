package com.team_olora.olora_beta;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class A_MainActivity extends AppCompatActivity {

    ///////////////////
    ///// 멤버
    ///////////////////
    ViewPager vp;
    public static String RSP_MacAddr = "00:00:00:00:00:00";
    public static String RSP_Name = null;
    public static String addr_self;
    public static Service_btService mbtService = null;

    public static String s;
    public static String d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 자기자신 주소 참조.
        addr_self = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");

        setContentView(R.layout.main_);

        vp = findViewById(R.id.vp);
        setupViewPager(vp);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(vp);

        tabLayout.getTabAt(0).setCustomView(R.layout.tab_a);
        tabLayout.getTabAt(1).setCustomView(R.layout.tab_b);
        tabLayout.getTabAt(2).setCustomView(R.layout.tab_c);
        tabLayout.getTabAt(3).setCustomView(R.layout.tab_d);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    private void setupViewPager(ViewPager viewPager) {
        pagerAdapter adapter = new pagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new A_Tab1(), "ONE");
        adapter.addFragment(new A_Tab2(), "TWO");
        adapter.addFragment(new A_Tab3(), "THREE");
        adapter.addFragment(new A_Tab4(), "FOUR");
        viewPager.setAdapter(adapter);
        Intent intent = getIntent();
        vp.setCurrentItem(intent.getIntExtra("Page",0));
    }

    private class pagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> FragmentList = new ArrayList<>();
        private final List<String> FragmentTitleList = new ArrayList<>();

        public pagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return FragmentList.get(position);
        }

        @Override
        public int getCount() {
            return 4;
        }

        public void addFragment(Fragment fragment, String title) {
            FragmentList.add(fragment);
            FragmentTitleList.add(title);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
    }
}