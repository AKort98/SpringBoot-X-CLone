package com.twitter.XClone.model.dao;

import com.twitter.XClone.model.LocalUser;
import com.twitter.XClone.model.Tweet;
import com.twitter.XClone.model.UserLikes;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface LikesDAO extends ListCrudRepository<UserLikes, Long> {
    List<UserLikes> findByUser(LocalUser user);



    Optional<UserLikes> findByUserAndTweet(LocalUser user, Tweet tweet);


    @Override
    void deleteById(Long aLong);
}
