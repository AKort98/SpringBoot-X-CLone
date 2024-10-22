package com.twitter.XClone.model.dao;

import com.twitter.XClone.model.Comment;
import com.twitter.XClone.model.Tweet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface CommentsDAO extends ListCrudRepository<Comment, Long> {

    @Query("select c from Comment c where c.tweet = ?1 and c.parent_comment is null")
    List<Comment> findByTweet(Tweet tweet, Pageable pageable);

    @Query("select count(c) from Comment c where c.parent_comment = ?1")
    long countByParent_comment(Comment parent_comment);

    @Query("select c from Comment c where c.parent_comment = ?1")
    List<Comment> findByParent_comment(Comment parent_comment, Pageable pageable);




    @Query("select count(c) from Comment c where c.tweet = ?1 and c.parent_comment is null" )
    long countByTweet(Tweet tweet);


    //@Query("select c from Comment c where c.parent_comment = ?1")
    //List<Comment> findByParent_comment(Comment parent_comment);

}
