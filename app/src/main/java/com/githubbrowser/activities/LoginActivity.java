package com.githubbrowser.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.githubbrowser.R;
import com.githubbrowser.utilities.DBHelper;
import com.githubbrowser.utilities.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity{

    public static final String LOGIN_USERNAME = "login_username";
    public static final String LOGIN_PASSWORD = "login_password";
    public static final String LOGIN_AUTHORIZED = "login_authorized";

    private UserLoginTask mAuthTask = null;
    private DBHelper mDBHelper = null;

    // UI references
    private EditText mUserView, mPasswordView;
    private ProgressBar mProgressBar;
    private LinearLayout mLoginFormView;
    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPrefs = getSharedPreferences(getApplicationContext().getPackageName(), 0);
        mDBHelper = new DBHelper(getApplicationContext());
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mLoginFormView = (LinearLayout) findViewById(R.id.login_form);
        mUserView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProgressBar = (ProgressBar)findViewById(R.id.login_progress);

        if(mSharedPrefs.getBoolean(LOGIN_AUTHORIZED, false)){
            if(Utils.hasInternetAccess(getApplicationContext())){
                mAuthTask = new UserLoginTask(mSharedPrefs.getString(LOGIN_USERNAME, ""), mSharedPrefs.getString(LOGIN_PASSWORD, ""));
                mAuthTask.execute((Void) null);
            }else{
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(intent);
                finish();
            }
        }else{
            mLoginFormView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mUsername = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            String newUrl = "https://api.github.com/user";
            try {
                if (mUsername != null && mPassword != null) {

                    URL connURL = new URL(newUrl);
                    HttpURLConnection connection = (HttpURLConnection) connURL.openConnection();
                    connection.setConnectTimeout(15000);
                    String sUserCode = mUsername + ":" + mPassword;
                    String authString = "Basic " + Base64.encodeToString(sUserCode.getBytes(), Base64.DEFAULT);
                    connection.setRequestProperty("Authorization", authString);

                    int responseCode = connection.getResponseCode();
                    if(connection.getResponseCode()== HttpsURLConnection.HTTP_OK){
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuffer sb = new StringBuffer();
                        int ch;
                        while ((ch = br.read()) != -1) {
                            sb.append((char) ch);
                        }
                        result = sb.toString();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
                if(mSharedPrefs.getBoolean(LOGIN_AUTHORIZED, false)) {
                    if (Utils.hasInternetAccess(getApplicationContext())) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showToast("Invalid username or password", getApplicationContext());
                                mLoginFormView.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            try {
                if (result!=null) {
                    JSONObject res = new JSONObject(result);
                    Utils.savePreferences(LOGIN_AUTHORIZED, true, getApplicationContext());
                    Utils.savePreferences(LOGIN_USERNAME, mUsername, getApplicationContext());
                    Utils.savePreferences(LOGIN_PASSWORD, mPassword, getApplicationContext());
                    /*Utils.savePreferences(UserActivity.USER_AVATAR_URL, res.getString(UserActivity.USER_AVATAR_URL), getApplicationContext());
                    Utils.savePreferences(UserActivity.USER_DATA_JSON, res.toString(), getApplicationContext());*/
                    mDBHelper.setUserData(
                            res.getString("login"),
                            res.getInt("id"),
                            res.getString("avatar_url"),
                            res.getInt("public_repos"),
                            res.getInt("followers"),
                            res.getInt("following"),
                            res.getInt("total_private_repos"),
                            res.getInt("owned_private_repos")
                    );
                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    //mPasswordView.setError(getString(R.string.error_incorrect_password));
                    //mPasswordView.requestFocus();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

