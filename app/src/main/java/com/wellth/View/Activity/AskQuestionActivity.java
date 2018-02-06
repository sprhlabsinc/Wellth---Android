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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.wellth.Controller.AppController;
import com.wellth.Model.AppConfig;
import com.wellth.R;

import java.util.ArrayList;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AskQuestionActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edit_question;
    private AvatarView img_avatar;
    private TextView txt_username, txt_location, txt_age;
    private Button btn_post;

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
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_question);

        Tracker mTracker = AppController.getInstance().getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.ask_question));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.ask_question).toUpperCase());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        edit_question = (EditText) findViewById(R.id.edit_question);
        img_avatar = (AvatarView) findViewById(R.id.img_avatar);
        txt_username = (TextView) findViewById(R.id.txt_username);
        txt_location = (TextView) findViewById(R.id.txt_location);
        txt_age = (TextView) findViewById(R.id.txt_age);

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
            postQuestion();
        }
    }

    private void postQuestion() {
        String content = edit_question.getText().toString();
        if (content.equals("")) {
            Snackbar.make(btn_post, "Please enter question.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        final String type = getIntent().getStringExtra("type");
        final KProgressHUD pDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Posting ...")
                .setDetailsLabel("")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        ParseObject object = new ParseObject("Post");
        object.put("fromUser", ParseUser.getCurrentUser());
        object.put("content", content);
        object.put("type", type);
        object.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                pDialog.dismiss();
                if (e == null) {
                    if (type.equals("text")) {
                        ArrayList<ParseUser> friendList = AppConfig.userInfo.friendList;
                        if (friendList != null && friendList.size() != 0) {
                            ArrayList<String> toUsers = new ArrayList<String>();
                            for (int i = 0; i < friendList.size(); i++) {
                                ParseUser toUser = friendList.get(i);
                                String token = toUser.getString("token");
                                if (token != null && !token.equals("") && !token.equals(AppConfig.token) && !AppConfig.hasString(toUsers, token)) {
                                    toUsers.add(token);
                                }
                            }
                            AppConfig.sendPushFriends(toUsers, String.format("%s has asked a question.", ParseUser.getCurrentUser().getString("fullname")));
                        }
                    }
                    Toast.makeText(getApplicationContext(),"You just have posted successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Snackbar.make(btn_post, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }
}
