package com.githubbrowser.utilities;

public class RepoInfo {

    private int mRepoId;
    private String mRepoName;
    private int mRepoOwned;
    private int mRepoStarred;
    private int mRepoOwner;
    private int mRepoCommitsCount;
    private int mRepoBranchesCount;
    private int mRepoReleasesCount;
    private int mRepoStarCount;
    private int mRepoForkCount;
    private int mRepoContributorCount;
    private long mRepoSize;
    private String mRepoLanguages;

    public RepoInfo(int id, String name, int owned, int starred, int owner, int commits,
                    int branches, int releases, int star, int fork, int contributor,
                    long size, String lang){
        mRepoId = id;
        mRepoName = name;
        mRepoOwned = owned;
        mRepoStarred = starred;
        mRepoOwner = owner;
        mRepoCommitsCount = commits;
        mRepoBranchesCount = branches;
        mRepoReleasesCount = releases;
        mRepoStarCount= star;
        mRepoForkCount = fork;
        mRepoContributorCount = contributor;
        mRepoSize = size;
        mRepoLanguages = lang;
    }


    public int getRepoId() {
        return mRepoId;
    }

    public String getRepoName() {
        return mRepoName;
    }

    public int getRepoOwned() {
        return mRepoOwned;
    }

    public int getRepoStarred() {
        return mRepoStarred;
    }

    public int getRepoOwner() {
        return mRepoOwner;
    }

    public int getRepoCommitsCount() {
        return mRepoCommitsCount;
    }

    public int getRepoBranchesCount() {
        return mRepoBranchesCount;
    }

    public int getRepoReleasesCount() {
        return mRepoReleasesCount;
    }

    public int getRepoStarCount() {
        return mRepoStarCount;
    }

    public int getRepoForkCount() {
        return mRepoForkCount;
    }

    public int getRepoContributorCount() {
        return mRepoContributorCount;
    }

    public String getRepoLanguages() {
        return mRepoLanguages;
    }

    public long getRepoSize() {
        return mRepoSize;
    }
}
