package com.githubbrowser.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;

import com.githubbrowser.R;
import com.githubbrowser.contentproviders.UsersReposProvider;
import com.githubbrowser.utilities.DBHelper;
import com.githubbrowser.utilities.GitHubQuerySender;
import com.githubbrowser.utilities.ImageLoader;
import com.githubbrowser.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchUsersActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        GitHubQuerySender.QueryListener{

    public static final String ARG_USERS_TYPE = "show_user_type";

    private DBHelper mDBHelper;
    private GitHubQuerySender mQuerySender;
    private ImageLoader mImageLoader;

    ListView mListViewUsers;
    SimpleCursorAdapter mAdapter;
    private String mSearchText;

    int mUserType;
    String mUserName;
    String mRepoName;
    int mRepoId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDBHelper = new DBHelper(getApplicationContext());
        mQuerySender = new GitHubQuerySender();
        mQuerySender.setQueryListner(this);
        mImageLoader = new ImageLoader(getApplicationContext());

        setContentView(R.layout.activity_users_search);


        mListViewUsers = (ListView)findViewById(R.id.list_users);
        mAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.layout_list_users,
                null,
                new String[] { DBHelper.COLUMN_USERS_NAME, DBHelper.COLUMN_USERS_AVATAR_URL},
                new int[] { R.id.name, R.id.avatar}, 0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex){
                if(view.getId() == R.id.avatar){
                    mImageLoader.DisplayImage(
                            cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_USER_AVATAR_URL)),
                            (ImageView)view);
                    return true;
                }
                return false;
            }
        });
        mListViewUsers.setAdapter(mAdapter);
        handleIntent(getIntent());

        mSearchText = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_users_search, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        if(mUserType!=0) {
            SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    mAdapter.getFilter().filter(newText);
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    mAdapter.getFilter().filter(query);
                    return true;
                }
            };
            searchView.setOnQueryTextListener(textChangeListener);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_logout){
            Intent intent = new Intent(SearchUsersActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            Utils.savePreferences(LoginActivity.LOGIN_AUTHORIZED, false, getApplicationContext());
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        mUserType = intent.getIntExtra(ARG_USERS_TYPE, 0);
        switch (mUserType){
            case 0:
                if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                    mSearchText = intent.getStringExtra(SearchManager.QUERY);
                    if(Utils.hasInternetAccess(getApplicationContext())) {
                        String query = "search/users?q=" + intent.getStringExtra(SearchManager.QUERY) + "in:login";
                        mQuerySender.sendUserQuery(getApplicationContext(), GitHubQuerySender.QueryType.GET_USERS_SEARCH, query);
                    }else{
                        getLoaderManager().restartLoader(0, null, this);
                    }
                }
                break;
            case 1:
                if(Utils.hasInternetAccess(getApplicationContext())){
                    mQuerySender.sendUserQuery(getApplicationContext(), GitHubQuerySender.QueryType.GET_USER_FOLLOWERS, "user/followers");
                }else{
                    getLoaderManager().initLoader(0, null, this);
                }
                mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String partialValue = constraint.toString();
                        return mDBHelper.getUsers(
                                DBHelper.COLUMN_USERS_NAME + " LIKE ? AND " + DBHelper.COLUMN_USERS_FOLLOWER + "==1",
                                new String[]{partialValue + "%"});

                    }
                });
                getLoaderManager().restartLoader(0, null, this);
                break;
            case 2:
                if(Utils.hasInternetAccess(getApplicationContext())){
                    mQuerySender.sendUserQuery(getApplicationContext(), GitHubQuerySender.QueryType.GET_USER_FOLLOWING, "user/following");
                }else{
                    getLoaderManager().initLoader(0, null, this);
                }
                mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String partialValue = constraint.toString();
                        return mDBHelper.getUsers(
                                DBHelper.COLUMN_USERS_NAME+" LIKE ? AND "+DBHelper.COLUMN_USERS_FOLLOWING+"==1",
                                new String[]{partialValue+"%"});
                    }
                });
                getLoaderManager().restartLoader(0, null, this);
                break;
            case 3:
                mUserName = intent.getStringExtra(RepositoryActivity.ARG_USER_NAME);
                mRepoName = intent.getStringExtra(RepositoryActivity.ARG_REPO_NAME);
                mRepoId = intent.getIntExtra(RepositoryActivity.ARG_REPO_ID, 0);
                if(Utils.hasInternetAccess(getApplicationContext())){
                    mQuerySender.sendUserQuery(getApplicationContext(), GitHubQuerySender.QueryType.GET_REPO_CONTRIBUTORS,
                            "repos"+"/"+mUserName+"/"+mRepoName+"/"+"contributors");
                }else{
                    getLoaderManager().initLoader(0, null, this);
                }
                mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String partialValue = constraint.toString();
                        return mDBHelper.getUsers(
                                DBHelper.COLUMN_USERS_NAME+" LIKE ? AND "+DBHelper.COLUMN_USERS_CONTRIBUTOR_TO + "==?",
                                new String[]{partialValue+"%", String.valueOf(mRepoId)});
                    }
                });
                getLoaderManager().restartLoader(0, null, this);
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        switch (mUserType){
            case 0:
                cursorLoader = new CursorLoader(getApplicationContext(), UsersReposProvider.CONTENT_USERS,
                        new String[]{
                                DBHelper.COLUMN_USERS_NAME,
                                DBHelper.COLUMN_USERS_ID,
                                DBHelper.COLUMN_USERS_AVATAR_URL
                        },
                        DBHelper.COLUMN_USERS_NAME+" LIKE ?",
                        new String[] {"%"+ mSearchText+ "%" },
                        DBHelper.COLUMN_USERS_NAME+" DESC");
                break;
            case 1:
                cursorLoader = new CursorLoader(getApplicationContext(), UsersReposProvider.CONTENT_USERS,
                        new String[]{
                                DBHelper.COLUMN_USERS_NAME,
                                DBHelper.COLUMN_USERS_ID,
                                DBHelper.COLUMN_USERS_AVATAR_URL
                        },
                        DBHelper.COLUMN_USERS_FOLLOWER+"==1",
                        null,
                        DBHelper.COLUMN_USERS_NAME+" DESC");
                break;
            case 2:
                cursorLoader = new CursorLoader(getApplicationContext(), UsersReposProvider.CONTENT_USERS,
                        new String[]{
                                DBHelper.COLUMN_USERS_NAME,
                                DBHelper.COLUMN_USERS_ID,
                                DBHelper.COLUMN_USERS_AVATAR_URL
                        },
                        DBHelper.COLUMN_USERS_FOLLOWING+"==1",
                        null,
                        DBHelper.COLUMN_USERS_NAME+" DESC");
                break;
            case 3:
                cursorLoader = new CursorLoader(getApplicationContext(), UsersReposProvider.CONTENT_USERS,
                        new String[]{
                                DBHelper.COLUMN_USERS_NAME,
                                DBHelper.COLUMN_USERS_ID,
                                DBHelper.COLUMN_USERS_AVATAR_URL
                        },
                        DBHelper.COLUMN_USERS_CONTRIBUTOR_TO+"==?",
                        new String[]{String.valueOf(mRepoId)},
                        DBHelper.COLUMN_USERS_NAME+" DESC");
                break;
            default:
                cursorLoader = null;
                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void queryFinished(GitHubQuerySender.QueryType queryType, String result, int responseCode) throws JSONException {
        if(queryType == GitHubQuerySender.QueryType.GET_USERS_SEARCH && result!=null){
            JSONObject res = new JSONObject(result);
            JSONArray users = res.getJSONArray("items");
            for(int i = 0; i < users.length(); i++){
                mDBHelper.insertUser(users.getJSONObject(i).getString("login"),
                        users.getJSONObject(i).getInt("id"),
                        users.getJSONObject(i).getString("avatar_url"));
            }
        }
        if(queryType == GitHubQuerySender.QueryType.GET_USER_FOLLOWERS && result!=null){
            JSONArray followers = new JSONArray(result);
            for(int i=0; i< followers.length(); i++ ){
                mDBHelper.insertFollower(
                        followers.getJSONObject(i).getString("login"),
                        followers.getJSONObject(i).getInt("id"),
                        followers.getJSONObject(i).getString("avatar_url")
                );
            }
        }
        if(queryType == GitHubQuerySender.QueryType.GET_USER_FOLLOWING && result!=null){
            JSONArray following = new JSONArray(result);
            for(int i=0; i< following.length(); i++ ){
                mDBHelper.insertFollowing(
                        following.getJSONObject(i).getString("login"),
                        following.getJSONObject(i).getInt("id"),
                        following.getJSONObject(i).getString("avatar_url")
                );
            }
        }
        if(queryType == GitHubQuerySender.QueryType.GET_REPO_CONTRIBUTORS && result!=null){
            JSONArray following = new JSONArray(result);
            for(int i=0; i< following.length(); i++ ){
                mDBHelper.insertContributor(
                        following.getJSONObject(i).getString("login"),
                        following.getJSONObject(i).getInt("id"),
                        following.getJSONObject(i).getString("avatar_url"),
                        mRepoId
                );
            }
        }
        getLoaderManager().restartLoader(0, null, this);
    }
}
