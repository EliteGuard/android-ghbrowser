package com.githubbrowser.utilities;

public class UserDataInfo {

    private String mUserName;
    private int mUserId;
    private String mUserAvatarURL;
    private int mPublicReposCount;
    private int mFollowersCount;
    private int mFollowingCount;
    private int mTotalPrivateReposCount;
    private int mOwnedPrivateReposCount;

    public UserDataInfo(String userName, int userId, String userAvatarUrl, int publicReposCount,
                        int followersCount, int followingCount, int totalPrivateReposCount, int ownedPrivateReposCount){
        mUserName = userName;
        mUserId = userId;
        mUserAvatarURL = userAvatarUrl;
        mPublicReposCount = publicReposCount;
        mFollowersCount = followersCount;
        mFollowingCount = followingCount;
        mTotalPrivateReposCount = totalPrivateReposCount;
        mOwnedPrivateReposCount = ownedPrivateReposCount;
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

    public int getPublicReposCount() {
        return mPublicReposCount;
    }

    public int getFollowersCount() {
        return mFollowersCount;
    }

    public int getFollowingCount() {
        return mFollowingCount;
    }

    public int getTotalPrivateReposCount() {
        return mTotalPrivateReposCount;
    }

    public int getOwnedPrivateReposCount() {
        return mOwnedPrivateReposCount;
    }
}
