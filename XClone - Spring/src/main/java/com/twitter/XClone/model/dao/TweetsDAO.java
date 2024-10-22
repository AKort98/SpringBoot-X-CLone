package com.twitter.XClone.model.dao;

import com.twitter.XClone.model.LocalUser;
import com.twitter.XClone.model.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TweetsDAO extends ListCrudRepository<Tweet, Long> {

    @Query("SELECT t FROM Tweet t WHERE t.user IN :users")
    List<Tweet> findByUsers(@Param("users") List<LocalUser> users, Sort sort);

    @Query("SELECT t FROM Tweet t WHERE t.user IN :users")
    List<Tweet> findByUser(@Param("users") List<LocalUser> users, Pageable pageable);

    List<Tweet> findByUser_Username(String username, Pageable pageable);

    long countByUser(LocalUser user);








}
