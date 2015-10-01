package com.githubbrowser.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import com.githubbrowser.activities.LoginActivity;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GitHubQuerySender {
    private AsyncTask mQueryTask = null;
    private QueryListener mQueryListner;

    public void sendUserQuery(Context context, QueryType queryType, String query){
        mQueryTask = new QueryTask(context, queryType).execute(query);
    }

    public void sendUserQuery(Context context, String requestMethod, QueryType queryType, String query){
        mQueryTask = new QueryTask(context, requestMethod, queryType).execute(query);
    }

    public enum QueryType {
        GET_USER_FOLLOWERS,
        GET_USER_FOLLOWING,
        GET_USERS_SEARCH,
        GET_USER_REPOS,
        GET_USER_STARRED,
        GET_REPO_CONTRIBUTORS,
        GET_REPO_COMMITS,
        GET_REPO_BRANCHES,
        GET_REPO_RELEASES,
        GET_REPO_LANGUAGES,
        GET_REPO_STAR,
        PUT_REPO_STAR,
        DELETE_REPO_UNSTAR
    }

    public void setQueryListner(QueryListener listener){
        this.mQueryListner = listener;
    }

     class QueryTask extends AsyncTask<String, Void, String> {
         private final Context mContext;
         private final QueryType mQueryType;
         private String mRequestMethod;
         int responseCode;

         QueryTask(Context ctx,QueryType queryType){
             mContext = ctx;
             mQueryType = queryType;
         }

         QueryTask(Context ctx, String requestMethod, QueryType queryType){
             mContext = ctx;
             mQueryType = queryType;
             mRequestMethod = requestMethod;
         }

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            String newUrl = "https://api.github.com/";
            newUrl+= params[0];
            try {
                SharedPreferences sharedPrefs = mContext.getSharedPreferences(mContext.getPackageName(), 0);
                URL connURL = new URL(newUrl);
                HttpURLConnection connection = (HttpURLConnection) connURL.openConnection();
                if(mRequestMethod!=null) {
                    connection.setRequestMethod(mRequestMethod);
                }
                connection.setConnectTimeout(30000);
                String sUserCode = sharedPrefs.getString(LoginActivity.LOGIN_USERNAME, "") + ":" + sharedPrefs.getString(LoginActivity.LOGIN_PASSWORD, "");
                String authString = "Basic " + Base64.encodeToString(sUserCode.getBytes(), Base64.DEFAULT);
                connection.setRequestProperty("Authorization", authString);

                responseCode = connection.getResponseCode();
                if(connection.getResponseCode()== HttpsURLConnection.HTTP_OK){
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    int ch;
                    while ((ch = br.read()) != -1) {
                        sb.append((char) ch);
                    }
                    result = sb.toString();
                }

            } catch (Exception e) {
                e.printStackTrace();
                /*if (mQueryListner!=null) {
                    try {
                        mQueryListner.queryFinished(mQueryType, result);
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }*/
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            mQueryTask = null;

            if (mQueryListner!=null) {
                try {
                    mQueryListner.queryFinished(mQueryType, result, responseCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            mQueryTask = null;
        }
    }

    public interface QueryListener {
        public void queryFinished(QueryType queryType, String result, int code) throws JSONException;
    }
}
