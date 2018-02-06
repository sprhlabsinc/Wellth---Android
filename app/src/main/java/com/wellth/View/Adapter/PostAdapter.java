package com.wellth.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.wellth.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import cn.jzvd.JZVideoPlayerStandard;


public class PostAdapter extends ArrayAdapter<ParseObject> {
    private final Context mContext;
    private ArrayList<ParseObject> dataSet;

    public PostAdapter(ArrayList<ParseObject> data, Context context) {
        super(context, R.layout.list_adapter_image_cell, data);
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
        if(itemView == null) {
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = (View)inflater.inflate(R.layout.list_adapter_image_cell, parent, false);

            holder = new AppInfoHolder();

            holder.img_post = (ImageView) itemView.findViewById(R.id.img_post);
            holder.jzVideoPlayerStandard = (JZVideoPlayerStandard) itemView.findViewById(R.id.videoplayer);

            itemView.setTag(holder);
        } else {
            holder = (AppInfoHolder)itemView.getTag();
        }

        ParseObject post = getItem(position);
        String content = post.getString("content");
        String type = post.getString("type");
        Date updatedAt = post.getUpdatedAt();
        ParseFile attachment = post.getParseFile("attachment");
        File postFile = null;
        if (attachment != null) {
            try {
                postFile = attachment.getFile();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (type.equals("image")) {
            holder.img_post.setVisibility(View.VISIBLE);
            holder.jzVideoPlayerStandard.setVisibility(View.GONE);
            Glide.with(getContext()).load(postFile).into(holder.img_post);
        }
        else if (type.equals("video")) {
            holder.img_post.setVisibility(View.GONE);
            holder.jzVideoPlayerStandard.setVisibility(View.VISIBLE);
            holder.jzVideoPlayerStandard.setUp(postFile.toString(), JZVideoPlayerStandard.SCREEN_LAYOUT_LIST, "");
            Glide.with(getContext()).load(postFile.toString()).into(holder.jzVideoPlayerStandard.thumbImageView);
        }

        holder.img_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GridView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });

        return itemView;
    }

    static class AppInfoHolder
    {
        ImageView img_post;
        JZVideoPlayerStandard jzVideoPlayerStandard;
    }
}
