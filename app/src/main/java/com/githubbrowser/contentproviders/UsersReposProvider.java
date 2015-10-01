package com.githubbrowser.contentproviders;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.githubbrowser.utilities.DBHelper;

public class UsersReposProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "com.githubbrowser.usersrepos";

    public static final Uri CONTENT_REPOSITORIES = Uri.parse("content://" + PROVIDER_NAME + "/repositories" );
    public static final Uri CONTENT_USERS = Uri.parse("content://" + PROVIDER_NAME + "/users" );

    private static final int REPOSITORIES = 1;
    private static final int USERS = 2;

    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "repositories", REPOSITORIES);
        uriMatcher.addURI(PROVIDER_NAME, "users", USERS);
    }

    DBHelper mDB;

    @Override
    public boolean onCreate() {
        mDB = new DBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (uriMatcher.match(uri)){
            case USERS:
                cursor = mDB.getUsers(selection, selectionArgs);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case REPOSITORIES:
                cursor = mDB.getRepositories(selection, selectionArgs);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                cursor = null;
                break;
        }
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }
}
