package com.wellth.View.Adapter;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.wellth.Model.AppConfig;
import com.wellth.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;
import cn.jzvd.JZVideoPlayerStandard;


public class QuestionAdapter extends ArrayAdapter<ParseObject> {
    private final Context mContext;
    private ArrayList<ParseObject> dataSet;

    public QuestionAdapter(ArrayList<ParseObject> data, Context context) {
        super(context, R.layout.list_adapter_post_cell, data);
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

        final ParseObject post = getItem(position);
        ParseUser fromUser = post.getParseUser("fromUser");
        String photoUrl = fromUser.getString("photoUrl");
        String username = fromUser.getString("fullname");
        String usertype = fromUser.getString("type");
        String content = post.getString("content");
        String type = post.getString("type");
        Date updatedAt = post.getCreatedAt();
        ParseFile photoFile = fromUser.getParseFile("photo");
        File photo = null;
        if (photoFile != null) {
            try {
                photo = photoFile.getFile();
            } catch (com.parse.ParseException e) {
                e.printStackTrace();
            }
        }
        ParseFile attachment = post.getParseFile("attachment");
        File postFile = null;
        if (attachment != null) {
            try {
                postFile = attachment.getFile();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(itemView == null) {
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = (View)inflater.inflate(R.layout.list_adapter_post_cell, parent, false);

            holder = new AppInfoHolder();

            holder.img_avatar = (AvatarView) itemView.findViewById(R.id.img_avatar);
            holder.txt_username = (TextView) itemView.findViewById(R.id.txt_username);
            holder.txt_post_time = (TextView) itemView.findViewById(R.id.txt_post_time);
            holder.txt_answers = (TextView) itemView.findViewById(R.id.txt_answers);
            holder.txt_question = (TextView) itemView.findViewById(R.id.txt_question);
            holder.txt_like = (TextView) itemView.findViewById(R.id.txt_like);

            holder.btn_like = (ImageButton) itemView.findViewById(R.id.btn_like);
            holder.btn_share = (ImageButton) itemView.findViewById(R.id.btn_share);
            holder.btn_post_answer = (Button) itemView.findViewById(R.id.btn_post_answer);
            holder.layout_wrapper = (LinearLayout) itemView.findViewById(R.id.layout_wrapper);
            holder.layout_more = (LinearLayout) itemView.findViewById(R.id.layout_more);

            holder.img_avatar_user = (AvatarView) itemView.findViewById(R.id.img_avatar_user);
            holder.txt_answer = (TextView) itemView.findViewById(R.id.txt_answer);
            holder.txt_post_time_user = (TextView) itemView.findViewById(R.id.txt_post_time_user);
            holder.txt_count = (TextView) itemView.findViewById(R.id.txt_count);
            holder.layout_comment = (RelativeLayout) itemView.findViewById(R.id.layout_comment);

            holder.layout_image = (RelativeLayout) itemView.findViewById(R.id.layout_image);
            holder.txt_question_mark = (TextView) itemView.findViewById(R.id.txt_question_mark);
            holder.view_line = (View) itemView.findViewById(R.id.view_line);

            holder.img_attachment = (ImageView) itemView.findViewById(R.id.img_attachment);
            holder.jzVideoPlayerStandard = (JZVideoPlayerStandard) itemView.findViewById(R.id.videoplayer);

            itemView.setTag(holder);
        } else {
            holder = (AppInfoHolder)itemView.getTag();
        }

        holder.layout_wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });
        final AppInfoHolder finalHolder = holder;
        holder.btn_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Like");
                query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
                query.whereEqualTo("toPost", post);
                query.countInBackground(new CountCallback() {
                    @Override
                    public void done(int count, ParseException e) {
                        if (count == 0) {
                            final ParseObject object = new ParseObject("Like");
                            object.put("fromUser", ParseUser.getCurrentUser());
                            object.put("toPost", post);
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Post");
                                        query.getInBackground(post.getObjectId(), new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(ParseObject post, ParseException e) {
                                                if (e == null) {
                                                    List<ParseObject> likeList = post.getList("likes");

                                                    if (likeList == null) {
                                                        likeList = new ArrayList<ParseObject>();
                                                    }
                                                    likeList.add(object);
                                                    post.put("likes", likeList);
                                                    post.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if (e == null) {
                                                                finalHolder.btn_like.setImageResource(R.drawable.ic_like);
                                                                Snackbar.make(finalHolder.btn_like, getContext().getResources().getString(R.string.message_like_success), Snackbar.LENGTH_LONG)
                                                                        .setAction("Action", null).show();
                                                            }
                                                            else {
                                                                Snackbar.make(finalHolder.btn_like, e.getMessage(), Snackbar.LENGTH_LONG)
                                                                        .setAction("Action", null).show();
                                                            }
                                                        }
                                                    });
                                                }
                                                else {
                                                    Snackbar.make(finalHolder.btn_like, e.getMessage(), Snackbar.LENGTH_LONG)
                                                            .setAction("Action", null).show();
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        Snackbar.make(finalHolder.btn_like, e.getMessage(), Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                }
                            });
                        }
                        else {
                            Snackbar.make(finalHolder.btn_like, getContext().getResources().getString(R.string.message_like_exist), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                });
            }
        });
        holder.btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });
        holder.btn_post_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });

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
        holder.txt_question.setText(content);
        holder.txt_username.setText(username);
        List<ParseObject> likeList = post.getList("likes");
        if (likeList == null) {
            likeList = new ArrayList<ParseObject>();
        }
        holder.txt_like.setText(String.format("%d", likeList.size()));

        List<ParseObject> commentList = post.getList("comments");
        if (commentList == null) {
            commentList = new ArrayList<ParseObject>();
        }
        holder.btn_like.setImageResource(R.drawable.ic_like_1);
        for (int i = 0; i < likeList.size(); i ++) {
            if (likeList.get(i).getParseUser("fromUser").getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                holder.btn_like.setImageResource(R.drawable.ic_like);
                break;
            }
        }
        if (type.equals("text") || type.equals("forum")) {
            holder.txt_answers.setText(String.format("%d Answers", commentList.size()));
            holder.btn_post_answer.setText("Post an Answer");

            holder.layout_image.setVisibility(View.GONE);
            holder.view_line.setVisibility(View.VISIBLE);
            holder.layout_comment.setVisibility(View.VISIBLE);
            holder.txt_question_mark.setVisibility(View.VISIBLE);

            if (commentList.size() == 0) {
                holder.layout_more.setVisibility(View.GONE);
                holder.layout_comment.setVisibility(View.GONE);
            }
            else {
                holder.layout_more.setVisibility(View.GONE);
                holder.layout_comment.setVisibility(View.VISIBLE);

                ParseObject comment = commentList.get(commentList.size() - 1);
                ParseUser fromUser1 = comment.getParseUser("fromUser");
                String photoUrl1 = fromUser1.getString("photoUrl");
                String username1 = fromUser1.getString("fullname");
                String usertype1 = fromUser1.getString("type");
                String content1 = comment.getString("content");
                Date updatedAt1 = comment.getUpdatedAt();
                ParseFile photoFile1 = fromUser1.getParseFile("photo");
                File photo1 = null;
                if (photoFile1 != null) {
                    try {
                        photo1 = photoFile1.getFile();
                    } catch (com.parse.ParseException e) {
                        e.printStackTrace();
                    }
                }
                AvatarPlaceholder placeholder1 = new AvatarPlaceholder(username1);
                Picasso.with(getContext())
                        .load(photo1)
                        .placeholder(placeholder1)
                        .into(holder.img_avatar_user);
                if (!usertype1.equals("Email") && photo1 == null) {
                    Picasso.with(getContext())
                            .load(photoUrl1)
                            .placeholder(placeholder1)
                            .into(holder.img_avatar_user);
                }
                String time_ago1 = AppConfig.getTimeAgo(updatedAt1, getContext());
                holder.txt_post_time_user.setText(time_ago1);
                holder.txt_answer.setText(content1);
//                AppConfig.makeTextViewResizable(holder.txt_answer, 3, ".. See More", true);
                holder.txt_count.setText(String.format("%d/%d", 1, commentList.size()));
            }
        }
        else {
            holder.txt_answers.setText(String.format("%d Comments", commentList.size()));
            holder.btn_post_answer.setText("Post a Comment");

            holder.layout_image.setVisibility(View.VISIBLE);
            holder.view_line.setVisibility(View.GONE);
            holder.layout_comment.setVisibility(View.GONE);
            holder.txt_question_mark.setVisibility(View.GONE);

            if (type.equals("image")) {
                holder.img_attachment.setVisibility(View.VISIBLE);
                holder.jzVideoPlayerStandard.setVisibility(View.GONE);
                Glide.with(getContext()).load(postFile).into(holder.img_attachment);
            }
            else if (type.equals("video")) {
                holder.img_attachment.setVisibility(View.GONE);
                holder.jzVideoPlayerStandard.setVisibility(View.VISIBLE);
                holder.jzVideoPlayerStandard.setUp(postFile.toString(), JZVideoPlayerStandard.SCREEN_LAYOUT_LIST, "");
                Glide.with(getContext()).load(postFile.toString()).into(holder.jzVideoPlayerStandard.thumbImageView);
            }
        }
//        if (fromUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
//            holder.btn_post_answer.setVisibility(View.GONE);
//            holder.btn_like.setVisibility(View.GONE);
//        }

        return itemView;
}

    static class AppInfoHolder
    {
        LinearLayout layout_wrapper;
        LinearLayout layout_more;
        AvatarView img_avatar;
        TextView txt_username, txt_post_time, txt_answers, txt_question, txt_like, txt_question_mark;
        Button btn_post_answer;
        ImageButton btn_like, btn_share;
        ImageView img_attachment;
        JZVideoPlayerStandard jzVideoPlayerStandard;

        TextView txt_answer, txt_post_time_user, txt_count;
        AvatarView img_avatar_user;
        RelativeLayout layout_comment, layout_image;
        View view_line;
    }
}
