package com.githubbrowser.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    /** Database name */
    private static String DBNAME = "ghbrowser_db";

    /** Version number of the database */
    private static int VERSION = 1;

    public static final String TABLE_REPOS = "t_repos";
    public static final String COLUMN_REPOS_KEYID = "_id";
    public static final String COLUMN_REPOS_NAME = "c_repos_name";
    public static final String COLUMN_REPOS_ID = "c_id";
    public static final String COLUMN_REPOS_STARRED = "c_starred";
    public static final String COLUMN_REPOS_OWNED = "c_owned";

    public static final String COLUMN_REPOS_OWNER = "c_owner";
    public static final String COLUMN_REPOS_COMMITS_COUNT = "c_commits_count";
    public static final String COLUMN_REPOS_BRANCHES_COUNT = "c_branches_count";
    public static final String COLUMN_REPOS_RELEASES_COUNT = "c_releases_count";
    public static final String COLUMN_REPOS_STAR_COUNT = "c_star_count";
    public static final String COLUMN_REPOS_FORK_COUNT = "c_fork_count";
    public static final String COLUMN_REPOS_CONTRIBUTOR_COUNT = "c_contrib_count";
    public static final String COLUMN_REPOS_SIZE = "c_size";
    public static final String COLUMN_REPOS_LANGUAGES = "c_languages";
    //public static final String COLUMN_REPOS_ = "c_";


    public static final String TABLE_USERS = "t_users";
    public static final String COLUMN_USERS_KEYID = "_id";
    public static final String COLUMN_USERS_NAME = "c_login";
    public static final String COLUMN_USERS_ID = "c_id";
    public static final String COLUMN_USERS_AVATAR_URL = "c_avatar_url";
    public static final String COLUMN_USERS_FOLLOWER = "c_follower";
    public static final String COLUMN_USERS_FOLLOWING = "c_following";
    public static final String COLUMN_USERS_CONTRIBUTOR_TO = "c_contrib_to";

    public static final String TABLE_USER_DATA = "t_userdata";
    public static final String COLUMN_USER_KEYID = "_id";
    public static final String COLUMN_USER_LOGIN = "c_user_login";
    public static final String COLUMN_USER_ID = "c_user_id";
    public static final String COLUMN_USER_AVATAR_URL = "c_avatar_url";
    public static final String COLUMN_USER_PUBLIC_REPOS_COUNT = "c_public_repos";
    public static final String COLUMN_USER_FOLLOWERS_COUNT = "c_followers";
    public static final String COLUMN_USER_FOLLOWING_COUNT = "c_following";
    public static final String COLUMN_USER_TOTAL_PRIVATE_REPOS_COUNT = "c_total_private_repos";
    public static final String COLUMN_USER_OWNED_PRIVATE_REPOS_COUNT = "c_owned_private_repos";


    /** An instance variable for SQLiteDatabase */
    private SQLiteDatabase mDB;

    /** Constructor */
    public DBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        this.mDB = getWritableDatabase();
    }

    /** This is a callback method, invoked when the method
     * getReadableDatabase() / getWritableDatabase() is called
     * provided the database does not exists
     * */
    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "create table "+ TABLE_USER_DATA + " ( "
                + COLUMN_USER_KEYID + " integer primary key autoincrement , "
                + COLUMN_USER_LOGIN + " text , "
                + COLUMN_USER_ID + " integer , "
                + COLUMN_USER_AVATAR_URL + " text , "
                + COLUMN_USER_PUBLIC_REPOS_COUNT + " integer , "
                + COLUMN_USER_FOLLOWERS_COUNT + " integer , "
                + COLUMN_USER_FOLLOWING_COUNT + " integer , "
                + COLUMN_USER_TOTAL_PRIVATE_REPOS_COUNT + " integer , "
                + COLUMN_USER_OWNED_PRIVATE_REPOS_COUNT + "  integer  ) " ;
        db.execSQL(sql);

        sql = "create table "+ TABLE_USERS + " ( "
                + COLUMN_USERS_KEYID + " integer primary key autoincrement , "
                + COLUMN_USERS_NAME + " text , "
                + COLUMN_USERS_ID + " integer UNIQUE ON CONFLICT IGNORE, "
                + COLUMN_USERS_AVATAR_URL + " text , "
                + COLUMN_USERS_FOLLOWER + " integer DEFAULT 0,"
                + COLUMN_USERS_FOLLOWING + " integer DEFAULT 0,"
                + COLUMN_USERS_CONTRIBUTOR_TO + " integer DEFAULT 0)";
        db.execSQL(sql);

        sql = "create table "+ TABLE_REPOS + " ( "
                + COLUMN_REPOS_KEYID + " integer primary key autoincrement , "
                + COLUMN_REPOS_ID + "  integer UNIQUE ON CONFLICT IGNORE, "
                + COLUMN_REPOS_NAME + "  text , "
                + COLUMN_REPOS_OWNED + "  integer DEFAULT 0, "
                + COLUMN_REPOS_STARRED + "  integer DEFAULT 0, "
                + COLUMN_REPOS_OWNER + " integer DEFAULT 0, "
                + COLUMN_REPOS_COMMITS_COUNT + " integer DEFAULT 0, "
                + COLUMN_REPOS_BRANCHES_COUNT + " integer DEFAULT 0, "
                + COLUMN_REPOS_RELEASES_COUNT + " integer DEFAULT 0, "
                + COLUMN_REPOS_STAR_COUNT + " integer DEFAULT 0, "
                + COLUMN_REPOS_FORK_COUNT + " integer DEFAULT 0, "
                + COLUMN_REPOS_CONTRIBUTOR_COUNT + " integer DEFAULT 0, "
                + COLUMN_REPOS_SIZE + " integer DEFAULT 0, "
                + COLUMN_REPOS_LANGUAGES + " text DEFAULT 'Not specified') ";

        db.execSQL(sql);

        /*sql = "insert into " + TABLE_REPOS + " ( " + COLUMN_REPO_NAME + " ) "
                + " values ( 'My Repo' )";*/
        //db.execSQL(sql);

    }

    public Cursor getUserData(){
        return mDB.query(TABLE_USER_DATA, new String[] {
                        COLUMN_USER_LOGIN,
                        COLUMN_USER_ID,
                        COLUMN_USER_AVATAR_URL,
                        COLUMN_USER_PUBLIC_REPOS_COUNT,
                        COLUMN_USER_FOLLOWERS_COUNT,
                        COLUMN_USER_FOLLOWING_COUNT,
                        COLUMN_USER_TOTAL_PRIVATE_REPOS_COUNT,
                        COLUMN_USER_OWNED_PRIVATE_REPOS_COUNT } ,
                null, null, null, null, null);
    }

    public void setUserData(String userName, int userId, String userAvatarUrl, int publicReposCount,
                            int followersCount, int followingCount, int totalPrivateReposCount, int ownedPrivateReposCount){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_LOGIN, userName);
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_USER_AVATAR_URL, userAvatarUrl);
        values.put(COLUMN_USER_PUBLIC_REPOS_COUNT, publicReposCount);
        values.put(COLUMN_USER_FOLLOWERS_COUNT, followersCount);
        values.put(COLUMN_USER_FOLLOWING_COUNT, followingCount);
        values.put(COLUMN_USER_TOTAL_PRIVATE_REPOS_COUNT, totalPrivateReposCount);
        values.put(COLUMN_USER_OWNED_PRIVATE_REPOS_COUNT, ownedPrivateReposCount);
        if(!userExists(userId)){
            db.insert(TABLE_USER_DATA, null, values);
        }else{
            db.update(TABLE_USER_DATA, values, COLUMN_USER_ID + " = ?",
                    new String[] { String.valueOf(userId) });
        }
    }

    public boolean userExists(int userId) {
        boolean exists = false;

        SQLiteDatabase db = this.getWritableDatabase();
        String checkQuery = "SELECT * FROM " + TABLE_USER_DATA + " WHERE "
                + COLUMN_USER_ID + "=" + userId;

        Cursor cursor = db.rawQuery(checkQuery, null);
        if (cursor.moveToFirst()) exists = true;
        cursor.close();
        return exists;
    }

    public Cursor getUsers(String selection, String [] args){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query(TABLE_USERS,
                null,
                selection, args, null, null, null);
    }

    public void insertUser(String userName, int userId, String userAvatarUrl){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERS_NAME, userName);
        values.put(COLUMN_USERS_ID, userId);
        values.put(COLUMN_USERS_AVATAR_URL, userAvatarUrl);
        db.insert(TABLE_USERS, null, values);
    }

    public void updateUser(int userId, String column, String value){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(column, value);
        db.update(TABLE_USERS, updateValues,
                COLUMN_USERS_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    public void updateUser(int userId, String column, int value){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(column, value);
        db.update(TABLE_USERS, updateValues,
                COLUMN_USERS_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    public void insertFollower(String userName, int userId, String userAvatarUrl){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERS_NAME, userName);
        values.put(COLUMN_USERS_ID, userId);
        values.put(COLUMN_USERS_AVATAR_URL, userAvatarUrl);
        //values.put(COLUMN_USERS_FOLLOWER, 1);
        db.insert(TABLE_USERS, null, values);

        updateUser(userId, COLUMN_USERS_FOLLOWER, 1);
        /*ContentValues updateValues = new ContentValues();
        updateValues.put(COLUMN_USERS_FOLLOWER, 1);
        db.update(TABLE_USERS, updateValues,
                COLUMN_USERS_ID + "=?",
                new String[]{String.valueOf(userId)});*/
    }

    public void insertFollowing(String userName, int userId, String userAvatarUrl){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERS_NAME, userName);
        values.put(COLUMN_USERS_ID, userId);
        values.put(COLUMN_USERS_AVATAR_URL, userAvatarUrl);
        //values.put(COLUMN_USERS_FOLLOWING, 1);
        db.insert(TABLE_USERS, null, values);

        updateUser(userId, COLUMN_USERS_FOLLOWING, 1);
        /*ContentValues updateValues = new ContentValues();
        updateValues.put(COLUMN_USERS_FOLLOWING, 1);
        db.update(TABLE_USERS, updateValues,
                COLUMN_USERS_ID + "=?",
                new String[]{String.valueOf(userId)});*/
    }

    public void insertContributor(String userName, int userId, String userAvatarUrl, int contributorTo){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERS_NAME, userName);
        values.put(COLUMN_USERS_ID, userId);
        values.put(COLUMN_USERS_AVATAR_URL, userAvatarUrl);
        //values.put(COLUMN_USERS_FOLLOWING, 1);
        db.insert(TABLE_USERS, null, values);

        updateUser(userId, COLUMN_USERS_CONTRIBUTOR_TO, contributorTo);
        /*ContentValues updateValues = new ContentValues();
        updateValues.put(COLUMN_USERS_FOLLOWING, 1);
        db.update(TABLE_USERS, updateValues,
                COLUMN_USERS_ID + "=?",
                new String[]{String.valueOf(userId)});*/
    }

    public Cursor getRepositories(String selection, String [] args){
        SQLiteDatabase db = this.getWritableDatabase();
        return mDB.query(TABLE_REPOS, null ,
                selection, args, null, null, null);
    }

    public void insertOwnedRepository(String repoName, int repoId, int repoOwner, int repoStar, int repoFork, long repoSize){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REPOS_NAME, repoName);
        values.put(COLUMN_REPOS_ID, repoId);
        values.put(COLUMN_REPOS_OWNER, repoOwner);
        values.put(COLUMN_REPOS_STAR_COUNT, repoStar);
        values.put(COLUMN_REPOS_FORK_COUNT, repoFork);
        values.put(COLUMN_REPOS_SIZE, repoSize);
        /*values.put(COLUMN_REPOS_COMMITS_COUNT, );
        values.put(COLUMN_REPOS_BRANCHES_COUNT, );
        values.put(COLUMN_REPOS_RELEASES_COUNT, );*/
        /*values.put(COLUMN_REPOS_CONTRIBUTOR_COUNT, );
        values.put(COLUMN_REPOS_LANGUAGES, );*/
        db.insert(TABLE_REPOS, null, values);

        updateRepo(repoId, COLUMN_REPOS_OWNED, 1);
        /*ContentValues updateValues = new ContentValues();
        updateValues.put(COLUMN_REPOS_OWNED, 1);
        db.update(TABLE_REPOS, updateValues,
                COLUMN_REPOS_ID+"=?",
                new String[]{String.valueOf(repoId)});*/
    }

    public void insertStarredRepository(String repoName, int repoId, int repoOwner, int repoStar, int repoFork, long repoSize){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REPOS_NAME, repoName);
        values.put(COLUMN_REPOS_ID, repoId);
        values.put(COLUMN_REPOS_OWNER, repoOwner);
        values.put(COLUMN_REPOS_STAR_COUNT, repoStar);
        values.put(COLUMN_REPOS_FORK_COUNT, repoFork);
        values.put(COLUMN_REPOS_SIZE, repoSize);
        db.insert(TABLE_REPOS, null, values);

        updateRepo(repoId, COLUMN_REPOS_STARRED, 1);
        /*ContentValues updateValues = new ContentValues();
        updateValues.put(COLUMN_REPOS_STARRED, 1);
        db.update(TABLE_REPOS, updateValues,
                COLUMN_REPOS_ID + "=?",
                new String[]{String.valueOf(repoId)});*/
    }

    public void updateRepo(int repoId, String column, String value){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(column, value);
        db.update(TABLE_REPOS, updateValues,
                COLUMN_REPOS_ID+"=?",
                new String[]{String.valueOf(repoId)});
    }

    public void updateRepo(int repoId, String column, int value){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(column, value);
        db.update(TABLE_REPOS, updateValues,
                COLUMN_REPOS_ID+"=?",
                new String[]{String.valueOf(repoId)});
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
    }


}
