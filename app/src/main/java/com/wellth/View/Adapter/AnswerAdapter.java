package com.wellth.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.wellth.Model.AppConfig;
import com.wellth.R;

import java.io.File;
import java.util.Date;
import java.util.List;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;


public class AnswerAdapter extends ArrayAdapter<ParseObject> {
    private final Context mContext;
    private List<ParseObject> dataSet;

    public AnswerAdapter(List<ParseObject> data, Context context) {
        super(context, R.layout.list_adapter_answer_cell, data);
        this.dataSet = data;
        this.mContext=context;
    }
    @Override
    public ParseObject getItem(int position){
        return dataSet.get(position);
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View itemView = convertView;
        LayoutInflater inflater;
        AppInfoHolder holder = null;
        ParseObject comment = getItem(dataSet.size() - position - 1);
        if(itemView == null) {
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = (View)inflater.inflate(R.layout.list_adapter_answer_cell, parent, false);

            holder = new AppInfoHolder();

            holder.img_avatar = (AvatarView) itemView.findViewById(R.id.img_avatar);
            holder.txt_count = (TextView) itemView.findViewById(R.id.txt_count);
            holder.txt_answer = (ExpandableTextView) itemView.findViewById(R.id.txt_answer);
            holder.txt_post_time = (TextView) itemView.findViewById(R.id.txt_post_time);

            itemView.setTag(holder);
        } else {
            holder = (AppInfoHolder)itemView.getTag();
        }

        ParseUser fromUser = comment.getParseUser("fromUser");
        String photoUrl = fromUser.getString("photoUrl");
        String username = fromUser.getString("fullname");
        String usertype = fromUser.getString("type");
        String content = comment.getString("content");
        Date updatedAt = comment.getUpdatedAt();
        ParseFile photoFile = fromUser.getParseFile("photo");
        File photo = null;
        if (photoFile != null) {
            try {
                photo = photoFile.getFile();
            } catch (com.parse.ParseException e) {
                e.printStackTrace();
            }
        }
        AvatarPlaceholder placeholder = new AvatarPlaceholder(username);
        Picasso.with(getContext())
                .load(photo)
                .placeholder(placeholder)
                .into(holder.img_avatar);
        if (!usertype.equals("Email") && photo == null) {
            Picasso.with(getContext())
                    .load(photoUrl)
                    .placeholder(placeholder)
                    .into(holder.img_avatar);
        }
        String time_ago = AppConfig.getTimeAgo(updatedAt, getContext());
        holder.txt_post_time.setText(time_ago);
        holder.txt_answer.setText(content);
//        if (content.length() > 100)
//            AppConfig.makeTextViewResizable(holder.txt_answer, 3, ".. See More", true);
        holder.txt_count.setText(String.format("%d/%d", position + 1, dataSet.size()));

        return itemView;
    }

    static class AppInfoHolder
    {
        AvatarView img_avatar;
        TextView txt_count, txt_post_time;
        ExpandableTextView txt_answer;
    }
}
