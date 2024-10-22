package com.twitter.XClone.api.model;

import com.twitter.XClone.model.Tweet;

import java.util.List;

public class PaginatedTweets {
    List<TweetWithLikeStatus> tweets;
    int totalPageNumber;
    int nextPage;

    public List<TweetWithLikeStatus> getTweets() {
        return tweets;
    }

    public void setTweets(List<TweetWithLikeStatus> tweets) {
        this.tweets = tweets;
    }

    public int getTotalPageNumber() {
        return totalPageNumber;
    }

    public void setTotalPageNumber(int totalPageNumber) {
        this.totalPageNumber = totalPageNumber;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }
}
