package com.wellth.View.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wellth.Controller.AppController;
import com.wellth.Model.AppConfig;
import com.wellth.R;
import com.wellth.View.Adapter.ProfileHealthIssueAdapter;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileHealthIssueActivity extends AppCompatActivity {

    private ListView list_health;
    private ProfileHealthIssueAdapter adapter;
    private PtrClassicFrameLayout ptrFrame;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_healthissue);

        Tracker mTracker = AppController.getInstance().getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.health_issue));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.health_issue));

        list_health = (ListView) findViewById(R.id.list_health);
        list_health.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                long viewId = view.getId();
                final String issue = (String) (list_health.getItemAtPosition(position));
                if (viewId == R.id.delete_but) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ProfileHealthIssueActivity.this);
                    alert.setTitle("Are you sure to delete this health issue?");

                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteIssue(position);
                        }
                    });
                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });
                    alert.show();
                }
                else if (viewId == R.id.edit_but) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ProfileHealthIssueActivity.this);
                    final EditText edit_health_issue = new EditText(ProfileHealthIssueActivity.this);
                    edit_health_issue.setHint("eg : Hairfall");
                    edit_health_issue.setText(AppConfig.userInfo.issueList.get(position));
                    alert.setTitle("Change your health or body issue.");

                    alert.setView(edit_health_issue);

                    alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String issue = edit_health_issue.getText().toString().trim().toLowerCase();
                            if (!issue.equals("")) {
                                changeIssue(position, issue);
                            }
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });
                    alert.show();
                }
            }
        });
        adapter = new ProfileHealthIssueAdapter(AppConfig.userInfo.issueList, getApplicationContext());
        list_health.setAdapter(adapter);
        list_health.setTextFilterEnabled(true);

//        ptrFrame = (PtrClassicFrameLayout) findViewById(R.id.rotate_header_list_view_frame);
//        ptrFrame.setLastUpdateTimeRelateObject(this);
//        ptrFrame.setPtrHandler(new PtrHandler() {
//            @Override
//            public void onRefreshBegin(PtrFrameLayout frame) {
//                if (isLoadPage) {
//                    isLoadPage = false;
//                    getHealthissues();
//                } else {
//                    ptrFrame.refreshComplete();
//                }
//
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
    }

    private void deleteIssue(int position) {
        AppConfig.userInfo.issueList.remove(position);
        list_health.setAdapter(adapter);

        updateData();
    }

    private void changeIssue(int position, String issue) {
        AppConfig.userInfo.issueList.set(position, issue);
        list_health.setAdapter(adapter);

        updateData();
    }

    private void updateData() {
        final KProgressHUD pDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Saving ...")
                .setDetailsLabel("")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        ParseUser user = ParseUser.getCurrentUser();
        user.put("healthissue", AppConfig.userInfo.issueList);
        user.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                pDialog.dismiss();
                if (e == null) {

                }
                else {
                    Snackbar.make(list_health, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.update_profile));
        mBroadcaster.sendBroadcast(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_add:
                AlertDialog.Builder alert = new AlertDialog.Builder(ProfileHealthIssueActivity.this);
                final EditText edit_health_issue = new EditText(this);
                edit_health_issue.setHint("eg : Hairfall");
                alert.setTitle("Add your health or body issue.");

                alert.setView(edit_health_issue);

                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String issue = edit_health_issue.getText().toString().trim().toLowerCase();
                        if (!issue.equals("")) {
                            AppConfig.userInfo.issueList.add(issue);
                            adapter.notifyDataSetChanged();
                            updateData();
                        }
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                alert.show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add, menu);
        return true;
    }
}
