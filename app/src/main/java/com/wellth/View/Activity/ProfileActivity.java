package com.wellth.View.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.CountryPickerListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
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
import java.util.List;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_MEDIA = 3001;

    private MaterialEditText edit_email, edit_username, edit_password, edit_confirm_password, edit_age;
    private Button btn_update, btn_country;
    private ImageButton btn_country_why, btn_age_why;
    private AvatarView img_avatar;
    private AvatarPlaceholder placeholder;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Tracker mTracker = AppController.getInstance().getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.edit_profile));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.edit_profile).toUpperCase());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        img_avatar = (AvatarView) findViewById(R.id.img_avatar);
        edit_email = (MaterialEditText) findViewById(R.id.edit_email);
        edit_username = (MaterialEditText) findViewById(R.id.edit_username);
        edit_password = (MaterialEditText) findViewById(R.id.edit_password);
        edit_confirm_password = (MaterialEditText) findViewById(R.id.edit_confirm_password);
        edit_age = (MaterialEditText) findViewById(R.id.edit_age);

        btn_update = (Button) findViewById(R.id.btn_update);
        btn_country = (Button) findViewById(R.id.btn_country);

        btn_country_why = (ImageButton) findViewById(R.id.btn_country_why);
        btn_age_why = (ImageButton) findViewById(R.id.btn_age_why);

        btn_country_why.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, getResources().getString(R.string.country_why), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        btn_age_why.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, getResources().getString(R.string.age_why), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(edit_email.validate() && edit_username.validate() && edit_password.validate()) {
                if(edit_email.validate() && edit_username.validate()) {
//                    if (!edit_confirm_password.getText().toString().equals(edit_password.getText().toString())) {
//                        edit_confirm_password.setError(AppConfig.PASSWORD_MATCH_ERROR_MESSAGE);
//                        return;
//                    }
                    if (!edit_age.getText().toString().equals("") && Integer.parseInt(edit_age.getText().toString()) > 100) {
                        edit_age.setError(AppConfig.AGE_ERROR_MESSAGE);
                        return;
                    }
                    int age = 0;
                    if (!edit_age.getText().toString().equals(""))
                        age = Integer.parseInt(edit_age.getText().toString());
                    updateProfile(edit_username.getText().toString(), btn_country.getText().toString(), age);
                }
            }
        });
        btn_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CountryPicker picker = CountryPicker.newInstance("Select Country");  // dialog title
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                        picker.dismiss();
                        btn_country.setText(name);
                    }
                });
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });

        edit_username.addValidator(new RegexpValidator(AppConfig.USERNAME_ERROR_MESSAGE, AppConfig.namePattern));
        edit_email.addValidator(new RegexpValidator(AppConfig.EMAIL_ERROR_MESSAGE, AppConfig.emailPattern));
        edit_password.addValidator(new RegexpValidator(AppConfig.PASSWORD_ERROR_MESSAGE, AppConfig.passwordPattern));

        edit_email.setEnabled(false);
        edit_email.setText(AppConfig.userInfo.email);
        edit_username.setText(AppConfig.userInfo.username);
        edit_password.setText("xxxxxx");
        edit_confirm_password.setText("xxxxxx");
        btn_country.setText(AppConfig.userInfo.country);

        edit_password.setVisibility(View.GONE);
        edit_confirm_password.setVisibility(View.GONE);

        if (AppConfig.userInfo.age == 0) {
            edit_age.setText("");
        }
        else {
            edit_age.setText(String.format("%d", AppConfig.userInfo.age));
        }

        placeholder = new AvatarPlaceholder(AppConfig.userInfo.username);
        Picasso.with(this)
                .load(AppConfig.userInfo.photo)
                .placeholder(placeholder)
                .into(img_avatar);
        if (!AppConfig.userInfo.type.equals("Email") && AppConfig.userInfo.photo == null) {
            Picasso.with(getApplicationContext())
                    .load(AppConfig.userInfo.photoUrl)
                    .placeholder(placeholder)
                    .into(img_avatar);
        }
        img_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaOptions.Builder builder = new MediaOptions.Builder();
                MediaOptions options = builder.setIsCropped(true).setFixAspectRatio(true)
                        .build();
                MediaPickerActivity.open(ProfileActivity.this, REQUEST_MEDIA, options);
            }
        });
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
                        File image = new File(mediaItem.getPathCropped(this));
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                        int height = bitmap.getHeight();
                        int width = bitmap.getWidth();
                        int limit = 200;
                        if (width < height && width > limit) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, limit, limit * height / width, true);
                        }
                        else if (height > limit) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, limit * width / height, limit, true);
                        }
                        img_avatar.setImageBitmap(bitmap);

                        File imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + "avatar.jpg");
                        OutputStream os = null;
                        try {
                            os = new BufferedOutputStream(new FileOutputStream(imageFile));
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                            os.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        updateProfileImage(imageFile);

                        break;
                    }
                }
            }
        }
    }

    private void updateProfileImage(final File imageFile) {
        ParseUser user = ParseUser.getCurrentUser();
        BufferedInputStream in = null;
        byte[] bytes = new byte[0];
        try {
            in = new BufferedInputStream(new FileInputStream(imageFile));
            bytes = AppConfig.getBytes(in);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        final ParseFile file = new ParseFile(imageFile.getName() , bytes);
        user.put("photo", file);
        user.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    try {
                        AppConfig.userInfo.photo = file.getFile();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
                    Intent intent = new Intent();
                    intent.setAction(getResources().getString(R.string.update_profile));
                    mBroadcaster.sendBroadcast(intent);

                    Snackbar.make(btn_update, getResources().getString(R.string.message_profile_image_changed), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    Snackbar.make(btn_update, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    private void updateProfile(final String username, final String country, final int age) {
        final KProgressHUD pDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Updating profile ...")
                .setDetailsLabel("")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        ParseUser user = ParseUser.getCurrentUser();
        user.put("fullname", username);
        user.put("country", country);
        user.put("age", age);
        user.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                pDialog.dismiss();
                if (e == null) {
                    AppConfig.userInfo.username = username;
                    AppConfig.userInfo.country = country;
                    AppConfig.userInfo.age = age;

                    LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
                    Intent intent = new Intent();
                    intent.setAction(getResources().getString(R.string.update_profile));
                    mBroadcaster.sendBroadcast(intent);

                    Snackbar.make(btn_update, getResources().getString(R.string.message_profile_update_success), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    Snackbar.make(btn_update, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });


//        ParseQuery<ParseUser> query = ParseUser.getQuery();
//        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
//            public void done(ParseUser user, ParseException e) {
//                if (e == null) {
//                    user.put("fullname", username);
//                    user.put("country", country);
//                    user.put("age", age);
//                    user.saveInBackground(new SaveCallback() {
//                        public void done(ParseException e) {
//                            pDialog.dismiss();
//                            if (e == null) {
//                                AppConfig.userInfo.username = username;
//                                AppConfig.userInfo.country = country;
//                                AppConfig.userInfo.age = age;
//
//                                LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
//                                Intent intent = new Intent();
//                                intent.setAction(getResources().getString(R.string.update_profile));
//                                mBroadcaster.sendBroadcast(intent);
//
//                                Snackbar.make(btn_update, getResources().getString(R.string.message_profile_update_success), Snackbar.LENGTH_LONG)
//                                        .setAction("Action", null).show();
//                            }
//                            else {
//                                Snackbar.make(btn_update, e.getMessage(), Snackbar.LENGTH_LONG)
//                                        .setAction("Action", null).show();
//                            }
//                        }
//                    });
//                } else {
//                    pDialog.dismiss();
//                    Snackbar.make(btn_update, e.getMessage(), Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
