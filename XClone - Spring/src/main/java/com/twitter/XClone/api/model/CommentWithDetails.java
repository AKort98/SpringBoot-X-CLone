package com.twitter.XClone.api.model;

import com.twitter.XClone.model.Comment;

public class CommentWithDetails {
    Comment comment;
    long replyCount;

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public long getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(long replyCount) {
        this.replyCount = replyCount;
    }
}
