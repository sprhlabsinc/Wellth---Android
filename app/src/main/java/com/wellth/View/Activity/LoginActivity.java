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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

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
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.wellth.Controller.AppController;
import com.wellth.Model.AppConfig;
import com.wellth.R;

import org.json.JSONObject;
import java.util.Arrays;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private int REQUEST_SING_UP = 10003;

    private MaterialEditText edit_email, edit_password;
    private Button btn_login, btn_signup, btn_forgot;

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
        setContentView(R.layout.activity_login);

        Tracker mTracker = AppController.getInstance().getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.signin));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.signin));
        toolbar.setVisibility(View.GONE);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        edit_email = (MaterialEditText) findViewById(R.id.edit_email);
        edit_password = (MaterialEditText) findViewById(R.id.edit_password);

        edit_email.addValidator(new RegexpValidator(AppConfig.EMAIL_ERROR_MESSAGE, AppConfig.emailPattern));
        edit_password.addValidator(new RegexpValidator(AppConfig.PASSWORD_ERROR_MESSAGE, AppConfig.passwordPattern));

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_signup = (Button) findViewById(R.id.btn_signup);
        btn_forgot = (Button) findViewById(R.id.btn_forgot);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit_email.validate() && edit_password.validate()) {
                    login(edit_email.getText().toString(), edit_password.getText().toString());
                }
            }
        });
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(i, REQUEST_SING_UP);
            }
        });
        btn_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ForgotActivity.class);
                startActivity(i);
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
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
            }
        });
        callbackManager = CallbackManager.Factory.create();
        loginWithFB();

        if (ParseUser.getCurrentUser() != null) {
            callMainActivity();
        }
    }

    private void login(String email, final String password) {

        final KProgressHUD pDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Log in ...")
                .setDetailsLabel("")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        ParseUser.logInInBackground(email, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                pDialog.dismiss();
                if (parseUser != null) {
                    callMainActivity();
                } else {
                    Snackbar.make(btn_login, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    private void callMainActivity() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
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
                        callMainActivity();
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

    private void signup(final String email, final String password, final String username, final String country, final int age, final String imageFile, final String type) {

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
                    Intent intent = new Intent(LoginActivity.this, HealthIssueActivity.class);
                    startActivityForResult(intent, REQUEST_SING_UP);
                }
                else {
                    Snackbar.make(btn_login, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
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
                                                                    callMainActivity();
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
                            parameters.putString("fields", "id, name, first_name, last_name, picture.type(large), email, link, birthday");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SING_UP) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    String result=data.getStringExtra("result");
                    if(result.equals("success")) {
                        callMainActivity();
                    }
                    break;
                case Activity.RESULT_CANCELED:

                    break;
                default:
                    break;
            }
        } else if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
