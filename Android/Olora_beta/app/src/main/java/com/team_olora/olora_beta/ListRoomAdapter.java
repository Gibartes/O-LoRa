package com.team_olora.olora_beta;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListRoomAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListRoomItem> listViewItemList = new ArrayList<>();
    private ImageView iconImageView;
    private TextView titleTextView;
    private TextView descTextView;
    private TextView nonReadView;
    private CustomHolder holder;
    // ListViewAdapter의 생성자
    public ListRoomAdapter() {
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_room, parent, false);
            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            iconImageView = convertView.findViewById(R.id.imageView1);
            titleTextView = convertView.findViewById(R.id.textView1);
            descTextView = convertView.findViewById(R.id.textView2);
            nonReadView = convertView.findViewById(R.id.nonRead);

            holder = new CustomHolder();
            holder.h_iconImageView = iconImageView;
            holder.h_titleTextView=titleTextView;
            holder.h_desc=descTextView;
            holder.h_nonRead =nonReadView;
            convertView.setTag(holder);
        }else{
            holder = (CustomHolder)convertView.getTag();
            iconImageView = holder.h_iconImageView;
            titleTextView = holder.h_titleTextView;
            descTextView=holder.h_desc;
            nonReadView=holder.h_nonRead;
        }

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListRoomItem listViewItem = listViewItemList.get(pos);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.getIcon());
        titleTextView.setText(listViewItem.getTitle());
        descTextView.setText(listViewItem.getDesc());
        nonReadView.setText(Integer.toString(listViewItem.getNonRead()));
         return convertView;
    }

    // 지정한 위치에 있는 아이템 삭제
    public void remove(int index) {
        listViewItemList.remove(index);
    }

    //            adapter.remove(index);
    public void clear() {
        listViewItemList.clear();
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    // room key를 불러오는 기능
    public int getRoomkey(int position) {
        return listViewItemList.get(position).getRoomnum();
    }

    public int getUserkey(int position){return listViewItemList.get(position).getUserkey();}

    public int getNonread(int position){return listViewItemList.get(position).getNonRead();}

    // 아이템 데이터 추가를 위한 함수. 원하는대로 작성 가능.
    public void addItem(Drawable icon, String title, String desc, int send, int userkey, int nonRead) {
        ListRoomItem item = new ListRoomItem();

        item.setIcon(icon);
        item.setTitle(title);
        item.setDesc(desc);
        item.setRoomnum(send);
        item.setUserkey(userkey);
        item.setNonRead(nonRead);

        listViewItemList.add(item);
    }

    public String getName(int position) {
        return listViewItemList.get(position).getTitle();
    }

    private class CustomHolder {
        ImageView h_iconImageView;
        TextView h_titleTextView;
        TextView h_desc;
        TextView h_nonRead;
    }

}

