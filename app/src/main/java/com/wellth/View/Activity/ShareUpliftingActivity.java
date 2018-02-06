package com.wellth.View.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.wellth.Controller.AppController;
import com.wellth.Model.AppConfig;
import com.wellth.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;
import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

public class ShareUpliftingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_MEDIA = 3001;

    private EditText edit_question;
    private AvatarView img_avatar;
    private TextView txt_username, txt_location, txt_age;
    private Button btn_image, btn_post;
    private ImageView img_attachment;
    private JZVideoPlayerStandard jzVideoPlayerStandard;
    private RelativeLayout layout_image;

    private MediaItem selectMedia = null;

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
        setContentView(R.layout.activity_share_uplifting);

        Tracker mTracker = AppController.getInstance().getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.share_uplifting));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.share_uplifting).toUpperCase());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        edit_question = (EditText) findViewById(R.id.edit_question);
        img_avatar = (AvatarView) findViewById(R.id.img_avatar);
        txt_username = (TextView) findViewById(R.id.txt_username);
        txt_location = (TextView) findViewById(R.id.txt_location);
        txt_age = (TextView) findViewById(R.id.txt_age);
        img_attachment = (ImageView) findViewById(R.id.img_attachment);
        layout_image = (RelativeLayout) findViewById(R.id.layout_image);

        btn_image = (Button) findViewById(R.id.btn_image);
        btn_post = (Button) findViewById(R.id.btn_post);

        btn_image.setOnClickListener(this);
        btn_post.setOnClickListener(this);

        jzVideoPlayerStandard = (JZVideoPlayerStandard) findViewById(R.id.videoplayer);
        layout_image.setVisibility(View.GONE);

        img_attachment.setOnClickListener(this);
        updateStatus();
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
        if (view == btn_image) {
            MediaOptions.Builder builder = new MediaOptions.Builder();
            MediaOptions options = builder.canSelectBothPhotoVideo()
                    .setIsCropped(true).setFixAspectRatio(false)
                    .canSelectMultiPhoto(false).canSelectMultiVideo(false)
                    .build();

            MediaPickerActivity.open(this, REQUEST_MEDIA, options);
        }
        else if (view == btn_post) {
            postQuestion();
        }
        else if (view == img_attachment) {
            if (selectMedia == null) return;

            File postFile = new File(selectMedia.getPathCropped(this));

            Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
            intent.putExtra("image", (Serializable) postFile);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA) {
            if (resultCode == RESULT_OK) {
                List<MediaItem> selectedList = MediaPickerActivity
                        .getMediaItemSelected(data);
                if (selectedList != null) {
                    for (MediaItem mediaItem : selectedList) {
                        selectMedia = mediaItem;
                        layout_image.setVisibility(View.VISIBLE);
                        if (mediaItem.isPhoto()) {
                            img_attachment.setVisibility(View.VISIBLE);
                            jzVideoPlayerStandard.setVisibility(View.GONE);
                            Glide.with(this).load(mediaItem.getUriCropped()).into(img_attachment);
                        }
                        else {
                            img_attachment.setVisibility(View.GONE);
                            jzVideoPlayerStandard.setVisibility(View.VISIBLE);
                            jzVideoPlayerStandard.setUp(mediaItem.getPathOrigin(this), JZVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");
                            Glide.with(this).load(mediaItem.getUriOrigin()).into(jzVideoPlayerStandard.thumbImageView);
                        }

                        break;
                    }
                }
            }
        }
    }

    private void postQuestion() {
        final String content = edit_question.getText().toString();
        if (selectMedia == null) {
            Snackbar.make(btn_post, "Please select image/video.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        if (content.equals("")) {
            Snackbar.make(btn_post, "Please enter description.", Snackbar.LENGTH_LONG)
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
        final String type = selectMedia.isPhoto() ? "image" : "video";
        File attachment = null;
        if (selectMedia.isPhoto()) {
            attachment = new File(selectMedia.getPathCropped(this));
        }
        else {
            attachment = new File(selectMedia.getPathOrigin(this));
        }
        File imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + "attach.jpg");
        int limit = 500;
        if (selectMedia.isPhoto()) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(attachment.getAbsolutePath(),bmOptions);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            if (width < height && width > limit) {
                bitmap = Bitmap.createScaledBitmap(bitmap, limit, limit * height / width, true);
            }
            else if (height > limit) {
                bitmap = Bitmap.createScaledBitmap(bitmap, limit * width / height, limit, true);
            }

            OutputStream os = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(imageFile));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                os.close();
                attachment = imageFile;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (selectMedia.isVideo() && attachment.length() > 5000000) {
            Snackbar.make(btn_post, "Please take short video less than 20s.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            pDialog.dismiss();
            return;
        }
        BufferedInputStream in = null;
        byte[] bytes = new byte[0];
        try {
            in = new BufferedInputStream(new FileInputStream(attachment));
            bytes = AppConfig.getBytes(in);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        final ParseFile file = new ParseFile(attachment.getName() , bytes);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseObject object = new ParseObject("Post");
                    object.put("fromUser", ParseUser.getCurrentUser());
                    object.put("content", content);
                    object.put("attachment", file);
                    object.put("type", type);
                    object.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            pDialog.dismiss();
                            if (e == null) {
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
                                    AppConfig.sendPushFriends(toUsers, String.format("%s has shared a post.", ParseUser.getCurrentUser().getString("fullname")));
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
                else {
                    pDialog.dismiss();
                    Snackbar.make(btn_post, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }
}
