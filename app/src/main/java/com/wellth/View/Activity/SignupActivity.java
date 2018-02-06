package com.wellth.View.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.CountryPickerListener;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.wellth.Controller.AppController;
import com.wellth.Model.AppConfig;
import com.wellth.R;

import org.json.JSONObject;
import java.util.Arrays;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignupActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static int REQUEST_HEALTH_ISSUE = 1001;
    private List<String> permissions = Arrays.asList("public_profile", "email");

    private MaterialEditText edit_email, edit_username, edit_password, edit_confirm_password, edit_age;
    private Button btn_back, btn_signup, btn_country, btn_privacy;
    private ImageButton btn_country_why, btn_age_why;

    private Button btn_google;
    private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_SIGN_IN = 9001;

    private Button btn_facebook;
    private CallbackManager callbackManager;
    private static final int FACEBOOK_SIGN_IN = 9002;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Tracker mTracker = AppController.getInstance().getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.signup));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.signup));
        toolbar.setVisibility(View.GONE);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        edit_email = (MaterialEditText) findViewById(R.id.edit_email);
        edit_username = (MaterialEditText) findViewById(R.id.edit_username);
        edit_password = (MaterialEditText) findViewById(R.id.edit_password);
        edit_confirm_password = (MaterialEditText) findViewById(R.id.edit_confirm_password);
        edit_age = (MaterialEditText) findViewById(R.id.edit_age);

        edit_username.addValidator(new RegexpValidator(AppConfig.USERNAME_ERROR_MESSAGE, AppConfig.namePattern));
        edit_email.addValidator(new RegexpValidator(AppConfig.EMAIL_ERROR_MESSAGE, AppConfig.emailPattern));
        edit_password.addValidator(new RegexpValidator(AppConfig.PASSWORD_ERROR_MESSAGE, AppConfig.passwordPattern));

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_signup = (Button) findViewById(R.id.btn_signup);
        btn_country = (Button) findViewById(R.id.btn_country);

        btn_privacy = (Button) findViewById(R.id.btn_privacy);

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

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit_email.validate() && edit_username.validate() && edit_password.validate()) {
                    if (!edit_confirm_password.getText().toString().equals(edit_password.getText().toString())) {
                        edit_confirm_password.setError(AppConfig.PASSWORD_MATCH_ERROR_MESSAGE);
                        return;
                    }
                    if (!edit_age.getText().toString().equals("") && Integer.parseInt(edit_age.getText().toString()) > 100) {
                        edit_age.setError(AppConfig.AGE_ERROR_MESSAGE);
                        return;
                    }
                    int age = 0;
                    if (!edit_age.getText().toString().equals(""))
                        age = Integer.parseInt(edit_age.getText().toString());
                    signup(edit_email.getText().toString(), edit_password.getText().toString(), edit_username.getText().toString(),
                            btn_country.getText().toString(), age, null, "Email");
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
        btn_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TermsActivity.class);
                startActivity(intent);
            }
        });

        btn_google = (Button) findViewById(R.id.btn_google);
        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btn_facebook = (Button) findViewById(R.id.btn_facebook);
        btn_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(SignupActivity.this, Arrays.asList("public_profile", "email"));
            }
        });
        callbackManager = CallbackManager.Factory.create();
        loginWithFB();
    }

    private void signup(String email, final String password, final String username, final String country, final int age, final String imageFile, final String type) {
        final KProgressHUD pDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Sign up ...")
                .setDetailsLabel("")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        final ParseUser user = new ParseUser();
        if (type.equals("Email")) {
            user.setUsername(email);
        } else if (type.equals("Facebook")) {
            user.setUsername(String.format("fb%s", password)); //social id
        }
        else if (type.equals("Google")) {
            user.setUsername(String.format("gg%s", password)); //social id
        }
        user.setPassword(password);
        user.setEmail(email);
        user.put("country", country);
        user.put("age", age);
        user.put("type", type);
        user.put("fullname", username);
        user.put("token", AppConfig.token);
        if (imageFile != null)
            user.put("photoUrl", imageFile);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                pDialog.dismiss();
                if (e == null) {
                    Intent intent = new Intent(SignupActivity.this, HealthIssueActivity.class);
                    startActivityForResult(intent, REQUEST_HEALTH_ISSUE);
                }
                else {
                    Snackbar.make(btn_signup, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_HEALTH_ISSUE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    String result=data.getStringExtra("result");
                    if(result.equals("success")) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result","success");
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                    break;
                case Activity.RESULT_CANCELED:

                    break;
                default:
                    break;
            }
        }
        else if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            final String email = acct.getEmail();
            final String givenName = acct.getGivenName();
            final String familyName = acct.getFamilyName();
            final String socialId = acct.getId();
            Uri photoUrl = acct.getPhotoUrl();

            String imageFile = null;
            if (photoUrl != null)
                imageFile = photoUrl.getPath();

            final String finalImageFile = imageFile;
            ParseUser.logInInBackground(String.format("gg%s", socialId), socialId, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (parseUser != null) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result","success");
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    } else {
                        signup(email, socialId, String.format("%s %s", givenName, familyName), "", 0, finalImageFile, "Google");
                    }
                }
            });


        } else {
            Snackbar.make(btn_signup, "Login failed.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public void loginWithFB(){
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken accessToken = AccessToken.getCurrentAccessToken();
                        if(accessToken != null){
                            GraphRequest request = GraphRequest.newMeRequest(
                                    accessToken,
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(
                                                JSONObject object,
                                                GraphResponse response) {
                                            if (response != null) {
                                                try {
                                                    JSONObject data = response.getJSONObject();
                                                    if (data.has("picture")) {
                                                        final String email = data.getString("email");
                                                        final String firstName = data.getString("first_name");
                                                        final String lastName = data.getString("last_name");
                                                        final String image = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                                        final String link = data.getString("link");
                                                        final String socialId = data.getString("id");

                                                        ParseUser.logInInBackground(String.format("fb%s", socialId), socialId, new LogInCallback() {
                                                            @Override
                                                            public void done(ParseUser parseUser, ParseException e) {
                                                                if (parseUser != null) {
                                                                    Intent returnIntent = new Intent();
                                                                    returnIntent.putExtra("result","success");
                                                                    setResult(Activity.RESULT_OK,returnIntent);
                                                                    finish();
                                                                } else {
                                                                    signup(email, socialId, String.format("%s %s", firstName, lastName), "", 0, image, "Facebook");
                                                                }
                                                            }
                                                        });
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id, name, first_name, last_name, picture.type(large), email, link");
                            request.setParameters(parameters);
                            request.executeAsync();
                        }
                    }

                    @Override
                    public void onCancel() {
                        Snackbar.make(btn_signup, "Login failed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Snackbar.make(btn_signup, exception.getMessage(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
    }
}
