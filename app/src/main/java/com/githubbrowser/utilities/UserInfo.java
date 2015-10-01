package com.githubbrowser.utilities;

public class UserInfo {
    private String mUserName;
    private int mUserId;
    private String mUserAvatarURL;


    public UserInfo(String userName, int userId, String userAvatarUrl){
        mUserName = userName;
        mUserId = userId;
        mUserAvatarURL = userAvatarUrl;
    }

    public String getUserName() {
        return mUserName;
    }

    public int getUserId() {
        return mUserId;
    }

    public String getUserAvatarURL() {
        return mUserAvatarURL;
    }
}
