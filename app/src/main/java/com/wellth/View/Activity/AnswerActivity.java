package com.wellth.View.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.wellth.Controller.AppController;
import com.wellth.Model.AppConfig;
import com.wellth.R;

import java.util.ArrayList;
import java.util.List;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AnswerActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edit_answer;
    private AvatarView img_avatar;
    private TextView txt_username, txt_location, txt_age, txt_answer, txt_question_mark;
    private Button btn_post;
    private ParseObject selectPost = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void updateStatus() {
        AvatarPlaceholder placeholder = new AvatarPlaceholder(AppConfig.userInfo.username);
        Picasso.with(getApplicationContext())
                .load(AppConfig.userInfo.photo)
                .placeholder(placeholder)
                .into(img_avatar);
        if (!AppConfig.userInfo.type.equals("Email") && AppConfig.userInfo.photo == null) {
            Picasso.with(getApplicationContext())
                    .load(AppConfig.userInfo.photoUrl)
                    .placeholder(placeholder)
                    .into(img_avatar);
        }
        txt_username.setText(AppConfig.userInfo.username);
        String country = AppConfig.userInfo.country;
        if (country == null || country.equals("")) {
            txt_location.setVisibility(View.GONE);
        }
        else {
            txt_location.setText(country);
            txt_location.setVisibility(View.VISIBLE);
        }
        if (AppConfig.userInfo.age == 0) {
            txt_age.setVisibility(View.GONE);
        }
        else {
            txt_age.setVisibility(View.VISIBLE);
            txt_age.setText(String.format("Age: %d", AppConfig.userInfo.age));
        }

        selectPost = AppConfig.selectPost;
        String type = selectPost.getString("type");
        String content = selectPost.getString("content");
        if (type.equals("text") || type.equals("forum")) {
            txt_question_mark.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(getResources().getString(R.string.answer_question).toUpperCase());
        }
        else {
            txt_question_mark.setVisibility(View.GONE);
            getSupportActionBar().setTitle(getResources().getString(R.string.comment_post).toUpperCase());
        }
        txt_answer.setText(content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        Tracker mTracker = AppController.getInstance().getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.answer_question));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        edit_answer = (EditText) findViewById(R.id.edit_answer);
        img_avatar = (AvatarView) findViewById(R.id.img_avatar);
        txt_username = (TextView) findViewById(R.id.txt_username);
        txt_location = (TextView) findViewById(R.id.txt_location);
        txt_age = (TextView) findViewById(R.id.txt_age);
        txt_answer = (TextView) findViewById(R.id.txt_answer);
        txt_question_mark = (TextView) findViewById(R.id.txt_question_mark);

        btn_post = (Button) findViewById(R.id.btn_post);

        btn_post.setOnClickListener(this);

        updateStatus();
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
        if (view == btn_post) {
            String type = selectPost.getString("type");
            if (type.equals("text")) {
                postComment("Please enter answer.", "You just have answered successfully!");
            }
            else {
                postComment("Please enter comment.", "You just have commented successfully!");
            }
        }
    }

    private void postComment(String confirmMessage, final String completeMessage) {
        String content = edit_answer.getText().toString();
        if (content.equals("")) {
            Snackbar.make(btn_post, confirmMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        final KProgressHUD pDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Posting ...")
                .setDetailsLabel("")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        final ParseObject object = new ParseObject("Comment");
        object.put("fromUser", ParseUser.getCurrentUser());
        object.put("toPost", selectPost);
        object.put("content", content);
        object.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Post");
                    query.getInBackground(selectPost.getObjectId(), new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject post, ParseException e) {
                            if (e == null) {
                                List<ParseObject> commentList = post.getList("comments");

                                if (commentList == null) {
                                    commentList = new ArrayList<ParseObject>();
                                }
                                commentList.add(object);
                                post.put("comments", commentList);
                                post.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            pDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), completeMessage,
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        else {
                                            pDialog.dismiss();
                                            Snackbar.make(btn_post, e.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                });
                            }
                            else {
                                pDialog.dismiss();
                                Snackbar.make(btn_post, e.getMessage(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    });
                }
                else {
                    pDialog.dismiss();
                    Snackbar.make(btn_post, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }
}
