package com.wellth.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.wellth.R;

import java.io.File;
import java.util.ArrayList;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;


public class FriendRequestAdapter extends ArrayAdapter<ParseUser> {
    private final Context mContext;
    private ArrayList<ParseUser> dataSet;

    public FriendRequestAdapter(ArrayList<ParseUser> data, Context context) {
        super(context, R.layout.list_adapter_friend_request_cell, data);
        this.dataSet = data;
        this.mContext=context;
    }
    @Override
    public ParseUser getItem(int position){
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
        ParseUser user = getItem(position);
        if(itemView == null) {
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = (View)inflater.inflate(R.layout.list_adapter_friend_request_cell, parent, false);

            holder = new AppInfoHolder();

            holder.img_avatar = (AvatarView) itemView.findViewById(R.id.img_avatar);
            holder.txt_username = (TextView) itemView.findViewById(R.id.txt_username);
            holder.txt_location = (TextView) itemView.findViewById(R.id.txt_location);
            holder.txt_age = (TextView) itemView.findViewById(R.id.txt_age);

            holder.btn_cancel = (Button) itemView.findViewById(R.id.btn_cancel);
            holder.btn_accept = (Button) itemView.findViewById(R.id.btn_accept);
            holder.btn_health_issue = (Button) itemView.findViewById(R.id.btn_health_issue);

            itemView.setTag(holder);
        } else {
            holder = (AppInfoHolder)itemView.getTag();
        }

        holder.btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });
        holder.btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });
        holder.btn_health_issue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });

        AvatarPlaceholder placeholder = new AvatarPlaceholder(user.getString("fullname"));
        try {
            File file = null;
            if (user.getParseFile("photo") != null) {
                file = user.getParseFile("photo").getFile();
            }
            Picasso.with(getContext())
                    .load(file)
                    .placeholder(placeholder)
                    .into(holder.img_avatar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!user.getString("type").equals("Email") && user.getParseFile("photo") == null) {
            Picasso.with(getContext())
                    .load(user.getString("photoUrl"))
                    .placeholder(placeholder)
                    .into(holder.img_avatar);
        }
        holder.txt_username.setText(user.getString("fullname"));
        int age = user.getInt("age");
        if (age == 0) {
            holder.txt_age.setVisibility(View.GONE);
        }
        else {
            holder.txt_age.setText(String.format("Age: %d", age));
            holder.txt_age.setVisibility(View.VISIBLE);
        }
        String country = user.getString("country");
        if (country == null || country.equals("")) {
            holder.txt_location.setVisibility(View.GONE);
        }
        else {
            holder.txt_location.setText(country);
            holder.txt_location.setVisibility(View.VISIBLE);
        }

        return itemView;
    }

    static class AppInfoHolder
    {
        AvatarView img_avatar;
        TextView txt_username, txt_location, txt_age;
        Button btn_cancel, btn_accept, btn_health_issue;
    }
}
