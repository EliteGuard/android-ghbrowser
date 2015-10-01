package com.githubbrowser.activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.githubbrowser.R;
import com.githubbrowser.contentproviders.UserDataProvider;
import com.githubbrowser.contentproviders.UsersReposProvider;
import com.githubbrowser.utilities.DBHelper;
import com.githubbrowser.utilities.GitHubQuerySender;
import com.githubbrowser.utilities.ImageLoader;
import com.githubbrowser.utilities.RepoInfo;
import com.githubbrowser.utilities.UserInfo;
import com.githubbrowser.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Iterator;

public class RepositoryActivity extends AppCompatActivity implements GitHubQuerySender.QueryListener{

    public static final String ARG_USER_NAME = "arg_user_name";
    public static final String ARG_REPO_NAME = "arg_repo_name";
    public static final String ARG_REPO_ID = "arg_repo_id";

    private String mRepoName;
    private int mRepoId;
    private RepoInfo mRepoInfo;
    private UserInfo mOwnerInfo;
    private boolean mStarred = false;

    private DBHelper mDBHelper;
    private ImageLoader mImageLoader;
    private GitHubQuerySender mQuerySender;

    MenuItem mStar;

    ImageView mImageOwnerAvatar;
    Button mButtonContributors;
    TextView mTextOwnerName, mTextCommits, mTextBranches, mTextReleases, mTextLanguages, mTextStar, mTextFork;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRepoId = getIntent().getIntExtra(ARG_REPO_ID, 0);
        mRepoName = getIntent().getStringExtra(ARG_REPO_NAME);
        if(mRepoId==0){
            finish();
        }
        mDBHelper = new DBHelper(getApplicationContext());
        mImageLoader = new ImageLoader(getApplicationContext());
        mQuerySender = new GitHubQuerySender();
        mQuerySender.setQueryListner(this);

        getLocalRepoInfo();

        launchQueries();

        getSupportActionBar().setTitle(mRepoName);

        setContentView(R.layout.activity_repository);

        mImageOwnerAvatar = (ImageView)findViewById(R.id.image_avatar);
        mImageLoader.DisplayImage(mOwnerInfo.getUserAvatarURL(), mImageOwnerAvatar);
        mTextOwnerName = (TextView)findViewById(R.id.text_username);
        mTextOwnerName.setText(mOwnerInfo.getUserName());
        mButtonContributors = (Button)findViewById(R.id.button_contributors);
        mButtonContributors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContributors(mOwnerInfo.getUserName(), mRepoInfo.getRepoName(), mRepoInfo.getRepoId());
            }
        });
        mTextCommits = (TextView)findViewById(R.id.text_commits);
        mTextBranches = (TextView)findViewById(R.id.text_branches);
        mTextReleases = (TextView)findViewById(R.id.text_releases);
        mTextLanguages = (TextView)findViewById(R.id.text_languages);

        mTextStar = (TextView)findViewById(R.id.text_star);
        mTextStar.append(String.valueOf(mRepoInfo.getRepoStarCount()));
        mTextFork = (TextView)findViewById(R.id.text_fork);
        mTextFork.append(String.valueOf(mRepoInfo.getRepoForkCount()));

        loadLocalInfo();
    }

    private void showContributors(String userName, String repoName, int repoId){
        Intent intent = new Intent(getApplicationContext(), SearchUsersActivity.class);
        intent.putExtra(SearchUsersActivity.ARG_USERS_TYPE, 3);
        intent.putExtra(ARG_USER_NAME, userName);
        intent.putExtra(ARG_REPO_NAME, repoName);
        intent.putExtra(ARG_REPO_ID, repoId);
        startActivity(intent);
    }

    private void loadLocalInfo(){
        mButtonContributors.setText(getString(R.string.text_contributors) + mRepoInfo.getRepoContributorCount());
        mTextCommits.append(String.valueOf(mRepoInfo.getRepoCommitsCount()));
        mTextBranches.append(String.valueOf(mRepoInfo.getRepoBranchesCount()));
        mTextReleases.append(String.valueOf(mRepoInfo.getRepoReleasesCount()));
        mTextLanguages.append(String.valueOf(mRepoInfo.getRepoLanguages()));
    }

    private void getLocalRepoInfo(){
        Cursor cursorRepoData = getContentResolver().query(UsersReposProvider.CONTENT_REPOSITORIES, null,
                DBHelper.COLUMN_REPOS_ID+"=?",
                new String[]{String.valueOf(mRepoId)}, null);
        if(cursorRepoData.moveToNext()){
            mRepoInfo = new RepoInfo(
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_ID)),
                    cursorRepoData.getString(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_NAME)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_OWNED)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_STARRED)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_OWNER)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_COMMITS_COUNT)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_BRANCHES_COUNT)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_RELEASES_COUNT)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_STAR_COUNT)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_FORK_COUNT)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_CONTRIBUTOR_COUNT)),
                    cursorRepoData.getInt(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_SIZE)),
                    cursorRepoData.getString(cursorRepoData.getColumnIndex(DBHelper.COLUMN_REPOS_LANGUAGES))
            );
            getLocalOwnerInfo();
        }
    }

    private void getLocalOwnerInfo(){
        Cursor cursorUserData = getContentResolver().query(UsersReposProvider.CONTENT_USERS, null,
                DBHelper.COLUMN_USERS_ID+"=?",
                new String[]{String.valueOf(mRepoInfo.getRepoOwner())}, null);
        if(cursorUserData.moveToNext()){
            mOwnerInfo = new UserInfo(
                    cursorUserData.getString(cursorUserData.getColumnIndex(DBHelper.COLUMN_USERS_NAME)),
                    cursorUserData.getInt(cursorUserData.getColumnIndex(DBHelper.COLUMN_USERS_ID)),
                    cursorUserData.getString(cursorUserData.getColumnIndex(DBHelper.COLUMN_USERS_AVATAR_URL))
            );
        }
    }

    private void launchQueries(){
        mQuerySender.sendUserQuery(getApplicationContext(),
                GitHubQuerySender.QueryType.GET_REPO_CONTRIBUTORS,
                "repos/" + mOwnerInfo.getUserName() + "/" + mRepoName + "/contributors");
        mQuerySender.sendUserQuery(getApplicationContext(),
                GitHubQuerySender.QueryType.GET_REPO_COMMITS,
                "repos/" + mOwnerInfo.getUserName() + "/" + mRepoName + "/commits");
        mQuerySender.sendUserQuery(getApplicationContext(),
                GitHubQuerySender.QueryType.GET_REPO_BRANCHES,
                "repos/" + mOwnerInfo.getUserName() + "/" + mRepoName + "/branches");
        mQuerySender.sendUserQuery(getApplicationContext(),
                GitHubQuerySender.QueryType.GET_REPO_RELEASES,
                "repos/" + mOwnerInfo.getUserName() + "/" + mRepoName + "/releases");
        mQuerySender.sendUserQuery(getApplicationContext(),
                GitHubQuerySender.QueryType.GET_REPO_LANGUAGES,
                "repos/" + mOwnerInfo.getUserName() + "/" + mRepoName + "/languages");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_repository, menu);

        mStar = menu.findItem(R.id.action_star);
        if (mRepoInfo.getRepoStarred() == 1) {
            mStar.setIcon(R.drawable.star_on_normal_holo_light);
        } else {
            mStar.setIcon(R.drawable.star_off_normal_holo_light);
        }

        mQuerySender.sendUserQuery(getApplicationContext(),
                GitHubQuerySender.QueryType.GET_REPO_STAR,
                "user/starred/" + mOwnerInfo.getUserName() + "/" + mRepoInfo.getRepoName());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_star) {
            if(mStarred){
                //item.setIcon(R.drawable.star_on_normal_holo_light);
                mQuerySender.sendUserQuery(getApplicationContext(), "DELETE",
                        GitHubQuerySender.QueryType.DELETE_REPO_UNSTAR,
                        "user/starred/" + mOwnerInfo.getUserName() + "/" + mRepoInfo.getRepoName());
            }else{
                //item.setIcon(R.drawable.star_off_normal_holo_light);
                mQuerySender.sendUserQuery(getApplicationContext(), "PUT",
                        GitHubQuerySender.QueryType.PUT_REPO_STAR,
                        "user/starred/" + mOwnerInfo.getUserName() + "/" + mRepoInfo.getRepoName());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void queryFinished(GitHubQuerySender.QueryType queryType, String result, int responseCode) throws JSONException {
        if(queryType == GitHubQuerySender.QueryType.GET_REPO_CONTRIBUTORS ){
            if(result!=null){
                JSONArray contribs = new JSONArray(result);
                mDBHelper.updateRepo(mRepoInfo.getRepoId(), DBHelper.COLUMN_REPOS_CONTRIBUTOR_COUNT, String.valueOf(contribs.length()));
                mButtonContributors.setText(getString(R.string.text_contributors) + String.valueOf(contribs.length()));
                for(int i=0; i<contribs.length(); i++) {
                    mDBHelper.insertContributor(contribs.getJSONObject(i).getString("login"),
                            contribs.getJSONObject(i).getInt("id"),
                            contribs.getJSONObject(i).getString("avatar_url"),
                            mRepoInfo.getRepoId());
                }
            }else{
                mButtonContributors.setText(getString(R.string.text_contributors) + "0");
            }
        }
        if(queryType == GitHubQuerySender.QueryType.GET_REPO_COMMITS ){
            if(result!=null){
                JSONArray commits = new JSONArray(result);
                mDBHelper.updateRepo(mRepoInfo.getRepoId(), DBHelper.COLUMN_REPOS_COMMITS_COUNT, String.valueOf(commits.length()));
                mTextCommits.setText(getString(R.string.text_commits)+String.valueOf(commits.length()));
            }else{
                mTextCommits.setText(getString(R.string.text_commits)+"0");
            }
        }
        if(queryType == GitHubQuerySender.QueryType.GET_REPO_BRANCHES ){
            if(result!=null){
                JSONArray branches = new JSONArray(result);
                mDBHelper.updateRepo(mRepoInfo.getRepoId(), DBHelper.COLUMN_REPOS_BRANCHES_COUNT, String.valueOf(branches.length()));
                mTextBranches.setText(getString(R.string.text_branches) + String.valueOf(branches.length()));
            }else{
                mTextBranches.setText(getString(R.string.text_branches)+"0");
            }
        }
        if(queryType == GitHubQuerySender.QueryType.GET_REPO_RELEASES ){
            if(result!=null){
                JSONArray rels = new JSONArray(result);
                mDBHelper.updateRepo(mRepoInfo.getRepoId(), DBHelper.COLUMN_REPOS_RELEASES_COUNT, String.valueOf(rels.length()));
                mTextReleases.setText(getString(R.string.text_releases) + String.valueOf(rels.length()));
            }else{
                mTextReleases.setText(getString(R.string.text_releases)+"0");
            }
        }
        if(queryType == GitHubQuerySender.QueryType.GET_REPO_LANGUAGES ){
            if(result!=null){
                JSONObject langs = new JSONObject(result);
                Iterator<String> iter = langs.keys();
                StringBuffer sb = new StringBuffer();
                while (iter.hasNext()) {
                    String key = iter.next();
                    sb.append(key);
                    sb.append(", ");
                }
                if(sb.length()>2) {
                    sb.substring(0, sb.length() - 2);
                }
                mTextLanguages.setText(getString(R.string.text_languages) + sb.toString());
                mDBHelper.updateRepo(mRepoInfo.getRepoId(), DBHelper.COLUMN_REPOS_LANGUAGES, sb.toString());
            }else{
                mTextLanguages.append(" Not specified");
            }
        }
        if(queryType == GitHubQuerySender.QueryType.GET_REPO_STAR ){
            if(responseCode == HttpURLConnection.HTTP_NO_CONTENT){
                mDBHelper.updateRepo(mRepoInfo.getRepoId(), DBHelper.COLUMN_REPOS_STARRED, 1);
                mStar.setIcon(R.drawable.star_on_normal_holo_light);
            }else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND){
                mDBHelper.updateRepo(mRepoInfo.getRepoId(), DBHelper.COLUMN_REPOS_STARRED, 0);
                mStar.setIcon(R.drawable.star_off_normal_holo_light);
            }
        }
        if(queryType == GitHubQuerySender.QueryType.PUT_REPO_STAR){
            if(responseCode == HttpURLConnection.HTTP_NO_CONTENT){
                mStar.setIcon(R.drawable.star_on_normal_holo_light);
                mDBHelper.updateRepo(mRepoInfo.getRepoId(), DBHelper.COLUMN_REPOS_STARRED, 1);
                mStarred=true;
            }
        }
        if(queryType == GitHubQuerySender.QueryType.DELETE_REPO_UNSTAR){
            if(responseCode == HttpURLConnection.HTTP_NO_CONTENT){
                mStar.setIcon(R.drawable.star_off_normal_holo_light);
                mDBHelper.updateRepo(mRepoInfo.getRepoId(), DBHelper.COLUMN_REPOS_STARRED, 0);
                mStarred=false;
            }
        }
    }
}
