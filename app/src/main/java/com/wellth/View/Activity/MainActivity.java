package com.wellth.View.Activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.onesignal.shortcutbadger.ShortcutBadger;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wellth.Controller.AppController;
import com.wellth.Model.AppConfig;
import com.wellth.R;
import com.wellth.View.Fragment.ForumFragment;
import com.wellth.View.Fragment.FriendInviteFragment;
import com.wellth.View.Fragment.FriendRequestFragment;
import com.wellth.View.Fragment.HomeFragment;
import com.wellth.View.Fragment.ProfileFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.jzvd.JZVideoPlayer;
import tgio.parselivequery.BaseQuery;
import tgio.parselivequery.LiveQueryEvent;
import tgio.parselivequery.Subscription;
import tgio.parselivequery.interfaces.OnListener;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private ImageButton btn_home, btn_public_forum, btn_add_friends, btn_friends_requests, btn_profile;

    private HomeFragment homeFragment;
    private ForumFragment forumFragment;
    private FriendInviteFragment friendInviteFragment;
    private FriendRequestFragment friendRequestFragment;
    private ProfileFragment profileFragment;

    private Tracker mTracker;

    private static boolean activityStarted;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message != null) {
                if (message.contains("asked a question") || message.contains("shared a post")) {
                    if (mViewPager.getCurrentItem() != 0)
                        mViewPager.setCurrentItem(0, false);
                }
                else if (message.contains("friend request")) {
                    if (mViewPager.getCurrentItem() != 3)
                        mViewPager.setCurrentItem(3);
                }
            }
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (activityStarted
//                && getIntent() != null
//                && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) != 0) {
//            finish();
//            return;
//        }
        ShortcutBadger.removeCount(getApplicationContext());

        if (ParseUser.getCurrentUser() == null) {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            finish();
        }
        else {
            AppConfig.getProfileInfo();
            ParseUser user = ParseUser.getCurrentUser();
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", user.getUsername());
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (e == null) {
                        parseUser.put("token", AppConfig.token);
                        parseUser.saveInBackground();
                    }
                }
            });
        }
        mTracker = AppController.getInstance().getDefaultTracker();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name).toUpperCase());

        activityStarted = true;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        btn_home = (ImageButton) findViewById(R.id.btn_home);
        btn_public_forum = (ImageButton) findViewById(R.id.btn_public_forum);
        btn_add_friends = (ImageButton) findViewById(R.id.btn_add_friends);
        btn_friends_requests = (ImageButton) findViewById(R.id.btn_friends_requests);
        btn_profile = (ImageButton) findViewById(R.id.btn_profile);

        btn_home.setOnClickListener(this);
        btn_public_forum.setOnClickListener(this);
        btn_add_friends.setOnClickListener(this);
        btn_friends_requests.setOnClickListener(this);
        btn_profile.setOnClickListener(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateTab(position);
                JZVideoPlayer.releaseAllVideos();

                if (position == 0) {
                    mTracker.setScreenName(getResources().getString(R.string.home));
                }
                else if (position == 1) {
                    mTracker.setScreenName(getResources().getString(R.string.public_forum));
                }
                else if (position == 2) {
                    mTracker.setScreenName(getResources().getString(R.string.add_friends));
                }
                else if (position == 3) {
                    mTracker.setScreenName(getResources().getString(R.string.friends_requests));
                }
                else if (position == 4) {
                    mTracker.setScreenName(getResources().getString(R.string.profile));
                }
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        tabLayout.getTabAt(0).setCustomView(R.layout.custom_tab_item);
        tabLayout.getTabAt(1).setCustomView(R.layout.custom_tab_item);
        tabLayout.getTabAt(2).setCustomView(R.layout.custom_tab_item);
        tabLayout.getTabAt(3).setCustomView(R.layout.custom_tab_item);
        tabLayout.getTabAt(4).setCustomView(R.layout.custom_tab_item);

//        mViewPager.setCurrentItem(4);

        new Thread(new Runnable() {
            public void run() {
                Subscription sub = new BaseQuery.Builder("Post")
//                .addField("comments")
//                .addField("likes")
//                .addField("content")
//                .addField("fromUser")
                        .build()
                        .subscribe();
                sub.on(LiveQueryEvent.UPDATE, new OnListener() {
                    @Override
                    public void on(JSONObject object) {
                        JSONObject obj = null;
                        try {
                            obj = object.getJSONObject("object");
                            String objectId = obj.getString("objectId");

                            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Post");
                            query.include("fromUser");
                            query.include("comments.fromUser");
                            query.include("likes.fromUser");
                            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject post, ParseException e) {
                                    if (e == null) {
                                        AppConfig.updateObject = post;

                                        LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
                                        Intent intent = new Intent();
                                        intent.setAction(getResources().getString(R.string.update_post_real_time));
                                        mBroadcaster.sendBroadcast(intent);
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                sub.on(LiveQueryEvent.CREATE, new OnListener() {
                    @Override
                    public void on(JSONObject object) {
                        JSONObject obj = null;
                        try {
                            obj = object.getJSONObject("object");
                            String objectId = obj.getString("objectId");

                            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Post");
                            query.include("fromUser");
                            query.include("comments.fromUser");
                            query.include("likes.fromUser");
                            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject post, ParseException e) {
                                    if (e == null) {
                                        AppConfig.createObject = post;

                                        LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
                                        Intent intent = new Intent();
                                        intent.setAction(getResources().getString(R.string.create_post_real_time));
                                        mBroadcaster.sendBroadcast(intent);
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.click_notification));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((mBroadcastReceiver), filter);

        String message = getIntent().getStringExtra("message");
        if (message != null) {
            if (message.contains("asked a question") || message.contains("shared a post")) {
                if (mViewPager.getCurrentItem() != 0)
                    mViewPager.setCurrentItem(0, false);
            }
            else if (message.contains("friend request")) {
                if (mViewPager.getCurrentItem() != 3)
                    mViewPager.setCurrentItem(3);
            }
        }
    }

    private void updateTab(int pos){

        try {
            switch (pos) {
                case 0:
                    btn_home.setBackgroundResource(R.drawable.tab_home_selected);
                    btn_public_forum.setBackgroundResource(R.drawable.tab_forum_unselected);
                    btn_add_friends.setBackgroundResource(R.drawable.tab_friend_unselected);
                    btn_friends_requests.setBackgroundResource(R.drawable.tab_request_unselected);
                    btn_profile.setBackgroundResource(R.drawable.tab_profile_unselected);

                    break;

                case 1:
                    btn_home.setBackgroundResource(R.drawable.tab_home_unselected);
                    btn_public_forum.setBackgroundResource(R.drawable.tab_forum_selected);
                    btn_add_friends.setBackgroundResource(R.drawable.tab_friend_unselected);
                    btn_friends_requests.setBackgroundResource(R.drawable.tab_request_unselected);
                    btn_profile.setBackgroundResource(R.drawable.tab_profile_unselected);
                    break;

                case 2:
                    btn_home.setBackgroundResource(R.drawable.tab_home_unselected);
                    btn_public_forum.setBackgroundResource(R.drawable.tab_forum_unselected);
                    btn_add_friends.setBackgroundResource(R.drawable.tab_friend_selected);
                    btn_friends_requests.setBackgroundResource(R.drawable.tab_request_unselected);
                    btn_profile.setBackgroundResource(R.drawable.tab_profile_unselected);
                    break;

                case 3:
                    btn_home.setBackgroundResource(R.drawable.tab_home_unselected);
                    btn_public_forum.setBackgroundResource(R.drawable.tab_forum_unselected);
                    btn_add_friends.setBackgroundResource(R.drawable.tab_friend_unselected);
                    btn_friends_requests.setBackgroundResource(R.drawable.tab_request_selected);
                    btn_profile.setBackgroundResource(R.drawable.tab_profile_unselected);
                    break;

                case 4:
                    btn_home.setBackgroundResource(R.drawable.tab_home_unselected);
                    btn_public_forum.setBackgroundResource(R.drawable.tab_forum_unselected);
                    btn_add_friends.setBackgroundResource(R.drawable.tab_friend_unselected);
                    btn_friends_requests.setBackgroundResource(R.drawable.tab_request_unselected);
                    btn_profile.setBackgroundResource(R.drawable.tab_profile_selected);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btn_home) {
            mViewPager.setCurrentItem(0, false);
        }
        else if (view == btn_public_forum) {
            mViewPager.setCurrentItem(1, false);
        }
        else if (view == btn_add_friends) {
            mViewPager.setCurrentItem(2, false);
        }
        else if (view == btn_friends_requests) {
            mViewPager.setCurrentItem(3, false);
        }
        else if (view == btn_profile) {
            mViewPager.setCurrentItem(4, false);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    homeFragment = HomeFragment.newInstance();
                    return homeFragment;
                case 1:
                    forumFragment = ForumFragment.newInstance();
                    return forumFragment;
                case 2:
                    friendInviteFragment = FriendInviteFragment.newInstance();
                    return friendInviteFragment;
                case 3:
                    friendRequestFragment = FriendRequestFragment.newInstance();
                    return friendRequestFragment;
                case 4:
                    profileFragment = ProfileFragment.newInstance();
                    return profileFragment;
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Home";
                case 1:
                    return "Public Forum";
                case 2:
                    return "Add Friends";
                case 3:
                    return "Friends Requests";
                case 4:
                    return "Profile";
            }
            return null;
        }
    }
    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return;
        }
//        moveTaskToBack(true);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_profile:
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_privacy:
                intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_terms:
                intent = new Intent(getApplicationContext(), TermsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_contact:
                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setType("text/email");
                Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "wellthusers@gmail.com" });
                Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                Email.putExtra(Intent.EXTRA_TEXT, "Dear Wellth, " + "");
                startActivity(Intent.createChooser(Email, "Send Feedback:"));
                return true;
            case R.id.action_logout:
                ParseUser.logOut();

                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        JZVideoPlayer.releaseAllVideos();
    }
}
