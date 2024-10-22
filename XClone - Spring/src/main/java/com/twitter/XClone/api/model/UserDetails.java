package com.twitter.XClone.api.model;

import com.twitter.XClone.model.Friendship;
import com.twitter.XClone.model.LocalUser;

public class UserDetails {
    LocalUser user;
    long tweetCount;
    boolean isFollowedByCurrentLoggedInUser;
    long followerCount;
    long followingCount;

    public long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    public boolean isFollowedByCurrentLoggedInUser() {
        return isFollowedByCurrentLoggedInUser;
    }

    public void setFollowedByCurrentLoggedInUser(boolean followedByCurrentLoggedInUser) {
        isFollowedByCurrentLoggedInUser = followedByCurrentLoggedInUser;
    }

    public LocalUser getUser() {
        return user;
    }

    public void setUser(LocalUser user) {
        this.user = user;
    }

    public long getTweetCount() {
        return tweetCount;
    }

    public void setTweetCount(long tweetCount) {
        this.tweetCount = tweetCount;
    }
}
