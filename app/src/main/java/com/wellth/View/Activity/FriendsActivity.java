package com.wellth.View.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wellth.Controller.AppController;
import com.wellth.Model.AppConfig;
import com.wellth.R;
import com.wellth.View.Adapter.FriendAdapter;
import com.wellth.View.Adapter.IssueAdapter;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FriendsActivity extends AppCompatActivity {

    private ListView list_friend;
    private FriendAdapter adapter;
    private PtrClassicFrameLayout ptrFrame;
    private ArrayList<ParseUser> friendList = new ArrayList<ParseUser>();
    private int count = 0;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Tracker mTracker = AppController.getInstance().getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.health_friends));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        ptrFrame = (PtrClassicFrameLayout) findViewById(R.id.rotate_header_list_view_frame);
//        ptrFrame.setLastUpdateTimeRelateObject(this);
//        ptrFrame.setPtrHandler(new PtrHandler() {
//            @Override
//            public void onRefreshBegin(PtrFrameLayout frame) {
//                getFriends();
//            }
//
//            @Override
//            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
//                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
//            }
//        });
//        ptrFrame.setResistance(1.7f);
//        ptrFrame.setRatioOfHeaderHeightToRefresh(1.2f);
//        ptrFrame.setDurationToClose(200);
//        ptrFrame.setDurationToCloseHeader(1000);
//        // default is false
//        ptrFrame.setPullToRefresh(false);
//        // default is true
//        ptrFrame.setKeepHeaderWhenRefresh(true);

        list_friend = (ListView) findViewById(R.id.list_friend);
        list_friend.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                long viewId = view.getId();
                final ParseUser fromUser =(ParseUser) (list_friend.getItemAtPosition(position));

                if (viewId == R.id.btn_health_issue) {
                    final Dialog customDialog;
                    LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
                    View customView = inflater.inflate(R.layout.health_issue_dialog, null);

                    customDialog = new Dialog(FriendsActivity.this);
                    customDialog.setContentView(customView);
                    customDialog.setTitle(getResources().getString(R.string.health_issue));
                    Window window = customDialog.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    customDialog.show();

                    List<String> healthList = fromUser.getList("healthissue");

                    final ListView list_issue = (ListView) customDialog.findViewById(R.id.list_healthitem);
                    final IssueAdapter listAdapter = new IssueAdapter(healthList, getApplicationContext());
                    list_issue.setAdapter(listAdapter);

                    list_issue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view1, int position1, long id) {
                            long viewId = view1.getId();
                            String health = (String) (list_issue.getItemAtPosition(position1));
                        }
                    });
                    ImageButton btnClose = (ImageButton) customDialog.findViewById(R.id.btnClose);
                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            customDialog.dismiss();
                        }
                    });
                }
                else if (viewId == R.id.delete_but) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(FriendsActivity.this);
                    alert.setTitle("Are you sure to delete this friend?");

                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteFriend(position);
                        }
                    });
                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });
                    alert.show();
                }
            }
        });
        getFriends();
    }

    private void deleteFriend(final int position) {
        final KProgressHUD pDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Deleting ...")
                .setDetailsLabel("")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        final ParseUser selUser = friendList.get(position);
        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Follower");
        query1.whereEqualTo("toUser", ParseUser.getCurrentUser());
        query1.whereEqualTo("fromUser", selUser);

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Follower");
        query2.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        query2.whereEqualTo("toUser", selUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query3 = ParseQuery.or(queries);
        query3.include("fromUser");
        query3.include("toUser");
        query3.whereEqualTo("status", "accept");
        query3.orderByDescending("updatedAt");
        query3.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < parseObjects.size(); i ++) {
                        ParseObject object = parseObjects.get(i);
                        object.put("status", "delete");
                    }
                    ParseObject.saveAllInBackground(parseObjects, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            pDialog.dismiss();

                            if (e == null) {
                                LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
                                Intent intent = new Intent();
                                intent.setAction(getResources().getString(R.string.update_profile));
                                mBroadcaster.sendBroadcast(intent);

                                friendList.remove(position);
                                getSupportActionBar().setTitle(String.format("%s HEALTH FRIENDS", friendList.size()));
                                list_friend.setAdapter(adapter);
                                AppConfig.sendPush(selUser.getString("token"), String.format("%s just deleted the contract with you.", ParseUser.getCurrentUser().getString("fullname")));
                            }
                            else {
                                Snackbar.make(list_friend, e.getMessage(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    });
                }
                else {
                    Snackbar.make(list_friend, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    pDialog.dismiss();
                }
            }
        });
    }

    public void getFriends() {
        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Follower");
        query1.whereEqualTo("toUser", ParseUser.getCurrentUser());

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Follower");
        query2.whereEqualTo("fromUser", ParseUser.getCurrentUser());

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query3 = ParseQuery.or(queries);
        query3.include("fromUser");
        query3.include("toUser");
        query3.whereEqualTo("status", "accept");
        query3.orderByDescending("updatedAt");
        query3.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException error) {
                if (parseObjects != null) {
                    friendList.clear();
                    for (int j = 0; j < parseObjects.size(); j++) {
                        ParseObject object = parseObjects.get(j);
                        ParseUser fromUser = object.getParseUser("fromUser");
                        ParseUser toUser = object.getParseUser("toUser");

                        if (fromUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                            boolean is_add = true;
                            for (int i = 0; i < friendList.size(); i ++) {
                                if (friendList.get(i).getUsername().equals(toUser.getUsername())) {
                                    is_add = false;
                                    break;
                                }
                            }
                            if (is_add)
                                friendList.add(toUser);
                        }
                        else {
                            boolean is_add = true;
                            for (int i = 0; i < friendList.size(); i ++) {
                                if (friendList.get(i).getUsername().equals(fromUser.getUsername())) {
                                    is_add = false;
                                    break;
                                }
                            }
                            if (is_add)
                                friendList.add(fromUser);
                        }
                    }
                    adapter = new FriendAdapter(friendList, getApplicationContext());
                    list_friend.setAdapter(adapter);

                    getSupportActionBar().setTitle(String.format("%s HEALTH FRIENDS", friendList.size()));
                    LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
                    Intent intent = new Intent();
                    intent.setAction(getResources().getString(R.string.update_profile));
                    mBroadcaster.sendBroadcast(intent);
                }
//                ptrFrame.refreshComplete();
            }
        });
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
//        getMenuInflater().inflate(R.menu.next, menu);
        return true;
    }
}
