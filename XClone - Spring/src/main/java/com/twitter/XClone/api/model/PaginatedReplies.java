package com.twitter.XClone.api.model;

import java.util.List;

public class PaginatedReplies {
    List<CommentWithDetails> replies;
    int nextPage;
    int totalPageCount;


    public List<CommentWithDetails> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentWithDetails> replies) {
        this.replies = replies;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public int getTotalPageCount() {
        return totalPageCount;
    }

    public void setTotalPageCount(int totalPageCount) {
        this.totalPageCount = totalPageCount;
    }
}