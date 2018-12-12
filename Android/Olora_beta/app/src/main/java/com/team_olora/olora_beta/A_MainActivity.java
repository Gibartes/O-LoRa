package com.team_olora.olora_beta;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 자기자신 주소 참조.
        // 와드
        addr_self = android.provider.Settings.Secure.getString(this.getContentResolver(),"bluetooth_address");
        Log.d("loglog", "onCreate11: "+addr_self);

        if(addr_self==null) {
            Log.d("loglog", "null addr");
            addr_self = getBluetoothMacAddress();
        }

        Log.d("loglog", "not null addr");
        Log.d("loglog", "onCreate22: "+addr_self);
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getBluetoothMacAddress(){

        String bluetoothMacAddress= "";
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try{
            Field mServiceField = mBluetoothAdapter.getClass().getDeclaredField("mService");
            mServiceField.setAccessible(true);
            Object btManagerService = mServiceField.get(mBluetoothAdapter);
            Method mMethod = btManagerService.getClass().getMethod("getAddress");
            bluetoothMacAddress = (String) mMethod.invoke(btManagerService);
        }catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.e("loge", "getBluetoothMacAddress: Failed to get Bluetooth address " + e.getMessage(), e);
        }

        return bluetoothMacAddress;
    }


    private void setupViewPager(ViewPager viewPager) {
        pagerAdapter adapter = new pagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new A_Tab1(), "ONE");
        adapter.addFragment(new A_Tab2(), "TWO");
        adapter.addFragment(new A_Tab3(), "THREE");
        adapter.addFragment(new A_Tab4(), "FOUR");
        viewPager.setAdapter(adapter);
        Intent intent = getIntent();
        vp.setCurrentItem(intent.getIntExtra("Page", 0));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}