package com.githubbrowser.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.githubbrowser.R;
import com.githubbrowser.activities.RepositoryActivity;
import com.githubbrowser.activities.SearchUsersActivity;
import com.githubbrowser.contentproviders.UsersReposProvider;
import com.githubbrowser.utilities.DBHelper;
import com.githubbrowser.utilities.GitHubQuerySender;
import com.githubbrowser.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReposListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
    GitHubQuerySender.QueryListener{

    private static final String ARG_LOADER_ID = "loader_id";

    private int mLoaderId;

    private DBHelper mDBHelper;
    private GitHubQuerySender mQuerySender;

    SimpleCursorAdapter mAdapter;
    ListView mListViewRepos;

    public static ReposListFragment newInstance(int loaderId) {
        ReposListFragment fragment = new ReposListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LOADER_ID, loaderId);
        fragment.setArguments(args);
        return fragment;
    }

    public ReposListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLoaderId = getArguments().getInt(ARG_LOADER_ID);
        }

        mDBHelper = new DBHelper(getActivity());
        mQuerySender = new GitHubQuerySender();
        mQuerySender.setQueryListner(this);

        switch (mLoaderId){
            case 0:
                getLoaderManager().initLoader(mLoaderId, null, this);
                mQuerySender.sendUserQuery(getActivity(), GitHubQuerySender.QueryType.GET_USER_REPOS, "user/repos");
                break;
            case 1:
                getLoaderManager().initLoader(mLoaderId, null, this);
                mQuerySender.sendUserQuery(getActivity(), GitHubQuerySender.QueryType.GET_USER_STARRED, "user/starred");
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_repos_list, container, false);

        mListViewRepos = (ListView) rootView.findViewById(R.id.list_repos);
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.layout_list_repos_item,
                null,
                new String[] { DBHelper.COLUMN_REPOS_NAME, DBHelper.COLUMN_REPOS_ID},
                new int[] { R.id.name,}, 0);
        mListViewRepos.setAdapter(mAdapter);
        mListViewRepos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Cursor cursor = (Cursor) mAdapter.getItem(position);
                cursor.moveToPosition(position);
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_REPOS_NAME));
                int id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REPOS_ID));

                Intent intent = new Intent(getActivity(), RepositoryActivity.class);
                intent.putExtra(RepositoryActivity.ARG_REPO_NAME, name);
                intent.putExtra(RepositoryActivity.ARG_REPO_ID, id);
                startActivity(intent);
            }
        });

        /** Creating a loader for populating listview from sqlite database */
        /** This statement, invokes the method onCreatedLoader() */
        //getActivity().getSupportLoaderManager().initLoader(mLoaderId, null, this);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Cursor cursor;
        CursorLoader loader;
        switch (mLoaderId){
            case 0:
                loader = new CursorLoader(getActivity(), UsersReposProvider.CONTENT_REPOSITORIES,
                        new String[]{DBHelper.COLUMN_REPOS_NAME},
                        DBHelper.COLUMN_REPOS_OWNED+"==1",
                        null,
                        DBHelper.COLUMN_REPOS_ID+" DESC");
                break;
            case 1:
                loader = new CursorLoader(getActivity(), UsersReposProvider.CONTENT_REPOSITORIES,
                        new String[]{DBHelper.COLUMN_REPOS_NAME},
                        DBHelper.COLUMN_REPOS_STARRED+"==1",
                        null,
                        DBHelper.COLUMN_REPOS_ID+" DESC");
                break;
            default:
                loader = null;
                break;
        }
        return loader;

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
    public void queryFinished(GitHubQuerySender.QueryType queryType, String result, int responsCode) throws JSONException {
        if(result == null){
            Utils.showToast("Lost connection!\nGoing into offline mode...", getActivity().getApplicationContext());
        }
        if(queryType == GitHubQuerySender.QueryType.GET_USER_REPOS && result!=null){
            JSONArray repos = new JSONArray(result);
            for(int i = 0; i < repos.length(); i++){
                mDBHelper.insertOwnedRepository(
                        repos.getJSONObject(i).getString("name"),
                        repos.getJSONObject(i).getInt("id"),
                        repos.getJSONObject(i).getJSONObject("owner").getInt("id"),
                        repos.getJSONObject(i).getInt("stargazers_count"),
                        repos.getJSONObject(i).getInt("forks_count"),
                        repos.getJSONObject(i).getLong("size"));
                mDBHelper.insertUser(
                        repos.getJSONObject(i).getJSONObject("owner").getString("login"),
                        repos.getJSONObject(i).getJSONObject("owner").getInt("id"),
                        repos.getJSONObject(i).getJSONObject("owner").getString("avatar_url"));
            }
        }
        if(queryType == GitHubQuerySender.QueryType.GET_USER_STARRED && result!=null){
            JSONArray repos = new JSONArray(result);
            for(int i = 0; i < repos.length(); i++){
                mDBHelper.insertStarredRepository(
                        repos.getJSONObject(i).getString("name"),
                        repos.getJSONObject(i).getInt("id"),
                        repos.getJSONObject(i).getJSONObject("owner").getInt("id"),
                        repos.getJSONObject(i).getInt("stargazers_count"),
                        repos.getJSONObject(i).getInt("forks_count"),
                        repos.getJSONObject(i).getLong("size"));
                mDBHelper.insertUser(
                        repos.getJSONObject(i).getJSONObject("owner").getString("login"),
                        repos.getJSONObject(i).getJSONObject("owner").getInt("id"),
                        repos.getJSONObject(i).getJSONObject("owner").getString("avatar_url"));
            }
        }
        getLoaderManager().restartLoader(mLoaderId, null, this);
    }

}
