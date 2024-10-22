package com.twitter.XClone.api.model;

import com.twitter.XClone.model.Comment;

import java.util.List;

public class PaginatedComments {
    List<CommentWithDetails> comments;
    int totalPageNumber;
    int nextPage;

    public List<CommentWithDetails> getComments() {
        return comments;
    }

    public void setComments(List<CommentWithDetails> comments) {
        this.comments = comments;
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
