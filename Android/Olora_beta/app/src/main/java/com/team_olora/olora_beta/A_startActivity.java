package com.team_olora.olora_beta;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class A_startActivity extends AppCompatActivity {

    BluetoothAdapter blueAdapter;
    int BLUETOOTH_REQUEST = 1;

    C_DB DB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        DB = new C_DB(getApplicationContext());
      /*  if (DB.get_set_blueOn()) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(A_startActivity.this);
            alert.setTitle("이전 장치 연결");
            alert.setMessage("최근 사용한 OLora 단말에 연결하시곘습니까?");
            alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(),"좃까!!!!!!!",Toast.LENGTH_LONG).show();
                    Intent go_now = new Intent(A_startActivity.this, A_MainActivity.class);
                    go_now.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    go_now.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(go_now);
                    finish();
                }
            });
            alert.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    Intent go_now = new Intent(A_startActivity.this, A_MainActivity.class);
                    go_now.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    go_now.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(go_now);
                    finish();
                }
            });
            alert.show();
        }*/

        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter == null) {
        } else {
            if (blueAdapter.isEnabled()) {

                Intent go_now = new Intent(getApplicationContext(), A_MainActivity.class);
                go_now.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                go_now.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(go_now);
                finish();
            } else {
                Intent blue_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(blue_intent, BLUETOOTH_REQUEST);
            }

        }
    }

    // 리시버 등록
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static class PlaceholderFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //TODO Auto-generated method stub
            View rootView = inflater.inflate(R.layout.main_, container, false);
            return rootView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //TODO Auto-generated method stub
        if (requestCode == BLUETOOTH_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getBaseContext(), "BLUETOOTH SUCCESSFULLY TURNED ON", Toast.LENGTH_SHORT).show();
                Intent go_now = new Intent(A_startActivity.this, A_MainActivity.class);
                go_now.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                go_now.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(go_now);
                finish();
            } else if (resultCode == RESULT_CANCELED) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(A_startActivity.this);
                alert.setTitle("블루투스 권한 요청");
                alert.setMessage("블루투스기능을 사용하지 않고 어플리케이션을 실행하시겠습니까?\n(통신기능이 제한되며 읽기만 가능합니다.)");
                alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent go_now = new Intent(A_startActivity.this, A_MainActivity.class);
                        go_now.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        go_now.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(go_now);
                        finish();

                    }
                });
                alert.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent blue_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(blue_intent, BLUETOOTH_REQUEST);
                    }
                });
                alert.show();

            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
