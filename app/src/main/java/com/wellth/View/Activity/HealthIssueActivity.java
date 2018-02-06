package com.wellth.View.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wellth.R;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HealthIssueActivity extends AppCompatActivity {

    private ArrayList<String> healthInfos = new ArrayList<String>();
    private Button btn_more;
    private EditText edit_health_issue1, edit_health_issue2, edit_health_issue3, edit_health_issue4, edit_health_issue5;
    private ImageView img_check1, img_check2, img_check3, img_check4, img_check5;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("");

        btn_more = (Button) findViewById(R.id.btn_more);
        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, getResources().getString(R.string.add_more), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        edit_health_issue1 = (EditText) findViewById(R.id.edit_health_issue1);
        edit_health_issue2 = (EditText) findViewById(R.id.edit_health_issue2);
        edit_health_issue3 = (EditText) findViewById(R.id.edit_health_issue3);
        edit_health_issue4 = (EditText) findViewById(R.id.edit_health_issue4);
        edit_health_issue5 = (EditText) findViewById(R.id.edit_health_issue5);

        img_check1 = (ImageView) findViewById(R.id.img_check1);
        img_check2 = (ImageView) findViewById(R.id.img_check2);
        img_check3 = (ImageView) findViewById(R.id.img_check3);
        img_check4 = (ImageView) findViewById(R.id.img_check4);
        img_check5 = (ImageView) findViewById(R.id.img_check5);

        edit_health_issue1.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    img_check1.setImageResource(R.drawable.ic_check_mark_1);
                }
                else {
                    img_check1.setImageResource(R.drawable.ic_check_mark);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        edit_health_issue2.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    img_check2.setImageResource(R.drawable.ic_check_mark_1);
                }
                else {
                    img_check2.setImageResource(R.drawable.ic_check_mark);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        edit_health_issue3.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    img_check3.setImageResource(R.drawable.ic_check_mark_1);
                }
                else {
                    img_check3.setImageResource(R.drawable.ic_check_mark);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        edit_health_issue4.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    img_check4.setImageResource(R.drawable.ic_check_mark_1);
                }
                else {
                    img_check4.setImageResource(R.drawable.ic_check_mark);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        edit_health_issue5.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    img_check5.setImageResource(R.drawable.ic_check_mark_1);
                }
                else {
                    img_check5.setImageResource(R.drawable.ic_check_mark);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
    }

    private void saveHealthissues() {
        String issue1 = edit_health_issue1.getText().toString().trim().toLowerCase();
        String issue2 = edit_health_issue2.getText().toString().trim().toLowerCase();
        String issue3 = edit_health_issue3.getText().toString().trim().toLowerCase();
        String issue4 = edit_health_issue4.getText().toString().trim().toLowerCase();
        String issue5 = edit_health_issue5.getText().toString().trim().toLowerCase();

        if (!issue1.equals("")) {
            healthInfos.add(issue1);
        }
        if (!issue2.equals("")) {
            healthInfos.add(issue2);
        }
        if (!issue3.equals("")) {
            healthInfos.add(issue3);
        }
        if (!issue4.equals("")) {
            healthInfos.add(issue4);
        }
        if (!issue5.equals("")) {
            healthInfos.add(issue5);
        }

        final KProgressHUD pDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Saving ...")
                .setDetailsLabel("")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", user.getUsername());
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    if (user != null) {
                        user.put("healthissue", healthInfos);
                        user.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                pDialog.dismiss();
                                if (e == null) {
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("result", "success");
                                    setResult(Activity.RESULT_OK, returnIntent);
                                    finish();
                                } else {
                                    Snackbar.make(btn_more, e.getMessage(), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }
                        });
                    }
                    else {
                        pDialog.dismiss();
                    }
                } else {
                    pDialog.dismiss();
                    Snackbar.make(btn_more, e.getMessage(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_next:
                saveHealthissues();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.next, menu);
        return true;
    }
}
