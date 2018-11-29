package com.team_olora.olora_beta;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListChatAdapter extends BaseAdapter {
    public class ListContents {
        String name;
        String msg;
        String time;
        int type;

        ListContents(String _name, String _msg, int _type, String _time) {
            name = _name;
            msg = _msg;
            type = _type;
            time =_time;
        }
    }

    private ArrayList m_List;

    ListChatAdapter() {
        m_List = new ArrayList();
    }

    // 외부에서 아이템 추가 요청 시 사용
    public void add(String _name, String _msg, int _type, String _time) {

        m_List.add(new ListContents(_name,_msg, _type,_time));
    }

    // 외부에서 아이템 삭제 요청 시 사용
    public void remove(int _position) {
        m_List.remove(_position);
    }

    @Override
    public int getCount() {
        return m_List.size();
    }

    @Override
    public Object getItem(int position) {
        return m_List.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();
        //폰트설정
        //Typeface font = Typeface.createFromAsset(context.getAssets(),"MILKYWAY.TTF");
        TextView text;
        TextView name;
        CustomHolder holder;
        LinearLayout layout;
        View viewRight;
        View viewLeft;
        TextView time_left;
        TextView time_right;


        // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
        if (convertView == null) {
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_msg, parent, false);
            layout = convertView.findViewById(R.id.chatRoom);
            text = convertView.findViewById(R.id.chatDate);
            name = convertView.findViewById(R.id.chatName);
            viewRight = convertView.findViewById(R.id.chatRight);
            viewLeft = convertView.findViewById(R.id.chatLeft);
            time_right=convertView.findViewById(R.id.time_log_right);
            time_left=convertView.findViewById(R.id.time_log_left);


            // 홀더 생성 및 Tag로 등록
            holder = new CustomHolder();
            holder.m_name = name;
            holder.m_TextView = text;
            holder.layout = layout;
            holder.viewRight = viewRight;
            holder.viewLeft = viewLeft;
            holder.time_R = time_right;
            holder.time_L = time_left;

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
            name = holder.m_name;
            text = holder.m_TextView;
            layout = holder.layout;
            viewRight = holder.viewRight;
            viewLeft = holder.viewLeft;
            time_right=holder.time_R;
            time_left=holder.time_L;
        }

        ListContents item = (ListContents) m_List.get(position);
        // Text 등록
        text.setText(item.msg);
        name.setText(item.name);
        time_left.setText(item.time);
        time_right.setText(item.time);

        if (item.type == 0) {
            name.setVisibility(View.VISIBLE);
            text.setBackgroundResource(R.drawable.inbox2);
            layout.setGravity(Gravity.LEFT);
            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);
            time_left.setVisibility(View.GONE);
            time_right.setVisibility(View.VISIBLE);
        } else if (item.type == 1) {
            text.setBackgroundResource(R.drawable.outbox2);
            layout.setGravity(Gravity.RIGHT);
            name.setVisibility(View.GONE);
            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);
            time_left.setVisibility(View.VISIBLE);
            time_right.setVisibility(View.GONE);
        } else if (item.type == 2) {
            text.setText(item.time);
            text.setBackgroundResource(R.color.colorInvisible);
            layout.setGravity(Gravity.CENTER);
            name.setVisibility(View.GONE);
            viewRight.setVisibility(View.VISIBLE);
            viewLeft.setVisibility(View.VISIBLE);
            time_left.setVisibility(View.GONE);
            time_right.setVisibility(View.GONE);
        }


        // 리스트 아이템을 터치 했을 때 이벤트 발생
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 터치 시 해당 아이템 이름 출력
            }
        });


        // 리스트 아이템을 길게 터치 했을때 이벤트 발생
        convertView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // 터치 시 해당 아이템 이름 출력
                return true;
            }
        });

        return convertView;
    }

    private class CustomHolder {
        TextView m_name;
        TextView m_TextView;
        LinearLayout layout;
        View viewRight;
        View viewLeft;
        TextView time_L;
        TextView time_R;
    }
}
