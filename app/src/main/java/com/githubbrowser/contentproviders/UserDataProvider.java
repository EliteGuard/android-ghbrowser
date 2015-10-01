package com.githubbrowser.contentproviders;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.githubbrowser.utilities.DBHelper;

public class UserDataProvider extends ContentProvider{
    public static final String PROVIDER_NAME = "com.githubbrowser.userdata";

    public static final Uri CONTENT_USERDATA = Uri.parse("content://" + PROVIDER_NAME + "/userdata" );

    private static final int USERDATA = 1;

    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "userdata", USERDATA);
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

        switch (uriMatcher.match(uri)){
            case USERDATA:
                return mDB.getUserData();
            default :
                return null;
        }
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
