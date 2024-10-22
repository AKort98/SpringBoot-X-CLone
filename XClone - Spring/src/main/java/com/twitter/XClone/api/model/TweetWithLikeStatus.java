package com.twitter.XClone.api.model;

import com.twitter.XClone.model.Tweet;

public class TweetWithLikeStatus {
    Tweet tweet;
    boolean liked;

    public Tweet getTweet() {
        return tweet;
    }

    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
