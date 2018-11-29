package com.team_olora.olora_beta;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class A_Tab4_BlackList extends AppCompatActivity {

    private C_DB DB = null;
    private ListView listView;
    private ListFriendAdapter adapter = new ListFriendAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_blacklist);

        listView = findViewById(R.id.List_blackView);
        listView.setAdapter(adapter);
        listView.setChoiceMode(listView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new Event());
        DB = new C_DB(this);

        load_values();
    }

    private class Event implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(A_Tab4_BlackList.this);
            alert.setTitle("차단해제");
            alert.setMessage("해당 유저 차단을 해제합니다.\n친구목록을 리셋해야 해당 유저가 친구목록에 나타납니다.");
            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DB.delete_black(adapter.getUserkey(position));
                    Intent blacklist = new Intent(A_Tab4_BlackList.this, A_Tab4_BlackList.class);
                    blacklist.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(blacklist);
                }
            });
            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                }
            });
            alert.show();

        }
    }

    private void load_values() {
        String user_name, user_desc;
        int black_key;
        adapter.clear();

        Cursor cursor = DB.get_black_cursor();
        if (cursor.moveToFirst()) {
            do {
                black_key = cursor.getInt(0);
                user_desc = cursor.getString(1);
                user_name = cursor.getString(2);
                adapter.addItem(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nayeon_icon), user_name, user_desc, black_key);
            } while (cursor.moveToNext());
        }
        adapter.notifyDataSetChanged();
    }
}
