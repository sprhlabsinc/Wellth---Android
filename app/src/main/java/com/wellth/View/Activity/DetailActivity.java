package com.wellth.View.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.wellth.Controller.AppController;
import com.wellth.Model.AppConfig;
import com.wellth.R;
import com.wellth.View.Adapter.AnswerAdapter;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;
import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    private int REQUEST_ANSWER = 10003;

    private AvatarView img_avatar;
    private TextView txt_username, txt_post_time, txt_answers, txt_question_mark, txt_question, txt_like;
    private ListView list_answer;
    private Button btn_post_answer;
    private ImageButton btn_like, btn_share;
    private RelativeLayout layout_image;
    private LinearLayout layout_wrapper;
    private JZVideoPlayerStandard jzVideoPlayerStandard;
    private ImageView img_attachment;
    private View view_bar;
    private ParseObject selectPost = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Tracker mTracker = AppController.getInstance().getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.details));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.details).toUpperCase());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        img_avatar = (AvatarView) findViewById(R.id.img_avatar);
        txt_username = (TextView) findViewById(R.id.txt_username);
        txt_post_time = (TextView) findViewById(R.id.txt_post_time);
        txt_answers = (TextView) findViewById(R.id.txt_answers);
        txt_question_mark = (TextView) findViewById(R.id.txt_question_mark);
        txt_question = (TextView) findViewById(R.id.txt_question);
        txt_like = (TextView) findViewById(R.id.txt_like);

        list_answer = (ListView) findViewById(R.id.list_answer);
        btn_like = (ImageButton) findViewById(R.id.btn_like);
        btn_share = (ImageButton) findViewById(R.id.btn_share);
        btn_post_answer = (Button) findViewById(R.id.btn_post_answer);
        layout_wrapper = (LinearLayout) findViewById(R.id.layout_wrapper);

        img_attachment = (ImageView) findViewById(R.id.img_attachment);
        layout_image = (RelativeLayout) findViewById(R.id.layout_image);
        jzVideoPlayerStandard = (JZVideoPlayerStandard) findViewById(R.id.videoplayer);
        view_bar = (View) findViewById(R.id.view_bar);

        btn_like.setOnClickListener(this);
        btn_share.setOnClickListener(this);
        btn_post_answer.setOnClickListener(this);
        img_attachment.setOnClickListener(this);

        layout_wrapper.setVisibility(View.VISIBLE);

        loadContent();
    }

    private void loadContent() {
        selectPost = AppConfig.selectPost;

        ParseUser fromUser = selectPost.getParseUser("fromUser");
        String photoUrl = fromUser.getString("photoUrl");
        String username = fromUser.getString("fullname");
        String usertype = fromUser.getString("type");
        String content = selectPost.getString("content");
        String type = selectPost.getString("type");
        Date updatedAt = selectPost.getUpdatedAt();
        ParseFile photoFile = fromUser.getParseFile("photo");
        File photo = null;
        if (photoFile != null) {
            try {
                photo = photoFile.getFile();
            } catch (com.parse.ParseException e1) {
                e1.printStackTrace();
            }
        }
        ParseFile attachment = selectPost.getParseFile("attachment");
        File postFile = null;
        if (attachment != null) {
            try {
                postFile = attachment.getFile();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        AvatarPlaceholder placeholder = new AvatarPlaceholder(username);
        Picasso.with(getApplicationContext())
                .load(photo)
                .placeholder(placeholder)
                .into(img_avatar);
        if (!usertype.equals("Email") && photo == null) {
            Picasso.with(getApplicationContext())
                    .load(photoUrl)
                    .placeholder(placeholder)
                    .into(img_avatar);
        }
        String time_ago = AppConfig.getTimeAgo(updatedAt, getApplicationContext());
        txt_post_time.setText(time_ago);
        txt_question.setText(content);
        txt_username.setText(username);

        List<ParseObject> likeList = selectPost.getList("likes");
        if (likeList == null) {
            likeList = new ArrayList<ParseObject>();
        }

        List<ParseObject> commentList = selectPost.getList("comments");
        if (commentList == null) {
            commentList = new ArrayList<ParseObject>();
        }
        btn_like.setImageResource(R.drawable.ic_like_1);
        for (int i = 0; i < likeList.size(); i++) {
            if (likeList.get(i).getParseUser("fromUser").getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                btn_like.setImageResource(R.drawable.ic_like);
                break;
            }
        }

        txt_like.setText(String.format("%d", likeList.size()));
        if (type.equals("text") || type.equals("forum")) {
            txt_answers.setText(String.format("%d Answers", commentList.size()));
            btn_post_answer.setText("Post an Answer");
            layout_image.setVisibility(View.GONE);
            view_bar.setVisibility(View.VISIBLE);
        } else {
            txt_answers.setText(String.format("%d Comments", commentList.size()));
            btn_post_answer.setText("Post a Comment");
            txt_question_mark.setVisibility(View.GONE);
            layout_image.setVisibility(View.VISIBLE);
            view_bar.setVisibility(View.GONE);

            if (type.equals("image")) {
                img_attachment.setVisibility(View.VISIBLE);
                jzVideoPlayerStandard.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(postFile).into(img_attachment);
            } else if (type.equals("video")) {
                img_attachment.setVisibility(View.GONE);
                jzVideoPlayerStandard.setVisibility(View.VISIBLE);
                jzVideoPlayerStandard.setUp(postFile.toString(), JZVideoPlayerStandard.SCREEN_LAYOUT_LIST, "");
                Glide.with(getApplicationContext()).load(postFile.toString()).into(jzVideoPlayerStandard.thumbImageView);
            }
        }
        AnswerAdapter listAdapter = new AnswerAdapter(commentList, getApplicationContext());
        list_answer.setAdapter(listAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JZVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public void onClick(View view) {
        if (selectPost == null) return;

        if (view == btn_like) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Like");
            query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
            query.whereEqualTo("toPost", selectPost);
            query.countInBackground(new CountCallback() {
                @Override
                public void done(int count, ParseException e) {
                    if (count == 0) {
                        final ParseObject object = new ParseObject("Like");
                        object.put("fromUser", ParseUser.getCurrentUser());
                        object.put("toPost", selectPost);
                        object.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Post");
                                    query.getInBackground(selectPost.getObjectId(), new GetCallback<ParseObject>() {
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
                                                                btn_like.setImageResource(R.drawable.ic_like);
                                                                Snackbar.make(btn_like, getResources().getString(R.string.message_like_success), Snackbar.LENGTH_LONG)
                                                                        .setAction("Action", null).show();
                                                            }
                                                        else {
                                                            Snackbar.make(btn_like, e.getMessage(), Snackbar.LENGTH_LONG)
                                                                    .setAction("Action", null).show();
                                                        }
                                                    }
                                                });
                                            }
                                            else {
                                                Snackbar.make(btn_like, e.getMessage(), Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        }
                                    });
                                }
                                else {
                                    Snackbar.make(btn_like, e.getMessage(), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }
                        });
                    }
                    else {
                        Snackbar.make(btn_like, getResources().getString(R.string.message_like_exist), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            });
        }
        else if (view == btn_post_answer) {
            Intent intent = new Intent(getApplicationContext(), AnswerActivity.class);
            startActivityForResult(intent, REQUEST_ANSWER);
        }
        else if (view == btn_share) {
            String content = selectPost.getString("content");
            String type = selectPost.getString("type");
            if (type.equals("image") || type.equals("video")) {
                ParseFile attachment = selectPost.getParseFile("attachment");
                File postFile = null;
                if (attachment != null) {
                    try {
                        postFile = attachment.getFile();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
                content = String.format("%s %s/%s", content, AppConfig.PARSE_URL_PATH, postFile.getName());
            }
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            sharingIntent.putExtra(Intent.EXTRA_TEXT, content);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
        else if (view == img_attachment) {
            ParseFile attachment = selectPost.getParseFile("attachment");
            File postFile = null;
            if (attachment != null) {
                try {
                    postFile = attachment.getFile();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
            if (postFile == null) return;

            Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
            intent.putExtra("image", (Serializable) postFile);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ANSWER) {
            finish();
        }
    }
}
