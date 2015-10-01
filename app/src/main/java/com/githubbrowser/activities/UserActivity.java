package com.githubbrowser.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.githubbrowser.R;
import com.githubbrowser.fragments.ReposListFragment;
import com.githubbrowser.contentproviders.UserDataProvider;
import com.githubbrowser.utilities.DBHelper;
import com.githubbrowser.utilities.GitHubQuerySender;
import com.githubbrowser.utilities.ImageLoader;
import com.githubbrowser.utilities.UserDataInfo;
import com.githubbrowser.utilities.Utils;

import org.json.JSONException;

public class UserActivity extends AppCompatActivity implements GitHubQuerySender.QueryListener{

    public static final String USER_AVATAR_URL = "avatar_url";

    private SharedPreferences mSharedPrefs;
    private DBHelper mDBHelper;
    private GitHubQuerySender mQuerySender;
    private ImageLoader mImageLoader;
    private UserDataInfo mUserInfo;

    private TextView mTextUsername;
    private ImageView mImageView;
    private Button mButtonFollowers, mButtonFollowing, mButtonReposOwned, mButtonReposStarred;

    private static final int PAGER_PAGES_COUNT = 2;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mImageLoader = new ImageLoader(getApplicationContext());
        mDBHelper = new DBHelper(getApplicationContext());
        mSharedPrefs = getSharedPreferences(getPackageName(), 0);
        mQuerySender = new GitHubQuerySender();
        mQuerySender.setQueryListner(this);


        Cursor mCursorUserData = getContentResolver().query(UserDataProvider.CONTENT_USERDATA,
                null,
                null, null, null);
        if(mCursorUserData.moveToNext()){
            mUserInfo = new UserDataInfo(
                    mCursorUserData.getString(mCursorUserData.getColumnIndex(DBHelper.COLUMN_USER_LOGIN)),
                    mCursorUserData.getInt(mCursorUserData.getColumnIndex(DBHelper.COLUMN_USER_ID)),
                    mCursorUserData.getString(mCursorUserData.getColumnIndex(DBHelper.COLUMN_USER_AVATAR_URL)),
                    mCursorUserData.getInt(mCursorUserData.getColumnIndex(DBHelper.COLUMN_USER_PUBLIC_REPOS_COUNT)),
                    mCursorUserData.getInt(mCursorUserData.getColumnIndex(DBHelper.COLUMN_USER_FOLLOWERS_COUNT)),
                    mCursorUserData.getInt(mCursorUserData.getColumnIndex(DBHelper.COLUMN_USER_FOLLOWING_COUNT)),
                    mCursorUserData.getInt(mCursorUserData.getColumnIndex(DBHelper.COLUMN_USER_TOTAL_PRIVATE_REPOS_COUNT)),
                    mCursorUserData.getInt(mCursorUserData.getColumnIndex(DBHelper.COLUMN_USER_OWNED_PRIVATE_REPOS_COUNT))
            );
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mTextUsername = (TextView)findViewById(R.id.text_username);
        mTextUsername.setText(mUserInfo.getUserName());

        mImageView = (ImageView)findViewById(R.id.image_avatar);;
        mImageLoader.DisplayImage(mUserInfo.getUserAvatarURL(), mImageView);

        mButtonFollowers = (Button)findViewById(R.id.button_followers);
        mButtonFollowers.setText(getString(R.string.button_followers) + ": " + String.valueOf(mUserInfo.getFollowersCount()));
        mButtonFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUsers(1);
            }
        });

        mButtonFollowing = (Button)findViewById(R.id.button_following);
        mButtonFollowing.setText(getString(R.string.button_following) + ": " + String.valueOf(mUserInfo.getFollowingCount()));
        mButtonFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUsers(2);
            }
        });

        mButtonReposOwned = (Button)findViewById(R.id.button_repos_owned);
        mButtonReposOwned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(0, true);
            }
        });
        mButtonReposStarred = (Button)findViewById(R.id.button_repos_starred);
        mButtonReposStarred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(1, true);
            }
        });

        mPager = (ViewPager) findViewById(R.id.pager_repos);
        mPagerAdapter = new ReposPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    private void showUsers(int userType){
        Intent intent = new Intent(getApplicationContext(), SearchUsersActivity.class);
        intent.putExtra(SearchUsersActivity.ARG_USERS_TYPE, userType);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(getApplicationContext(), SearchUsersActivity.class);
            intent.putExtra(SearchUsersActivity.ARG_USERS_TYPE, 0);
            startActivity(intent);
        }
        if(id == R.id.action_logout){
            Intent intent = new Intent(UserActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            Utils.savePreferences(LoginActivity.LOGIN_AUTHORIZED, false, getApplicationContext());
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void queryFinished(GitHubQuerySender.QueryType queryType, String result, int responseCode) throws JSONException {
        if(result == null){
            Utils.showToast("Lost connection!\nGoing into offline mode...", getApplicationContext());
        }
    }

    private class ReposPagerAdapter extends FragmentStatePagerAdapter {
        public ReposPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return ReposListFragment.newInstance(position);
                case 1:
                    return ReposListFragment.newInstance(position);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return PAGER_PAGES_COUNT;
        }
    }
}
