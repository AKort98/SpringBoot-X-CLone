package com.twitter.XClone.model.dao;

import com.twitter.XClone.model.Friendship;
import com.twitter.XClone.model.LocalUser;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface FriendsDAO extends ListCrudRepository<Friendship, Long> {
    long countByUserAndStatusLike(LocalUser user, String status);

    List<Friendship> findByUser(LocalUser user);

    List<Friendship> findByFriend(LocalUser friend);

    Friendship findByUserAndFriendAndStatusLikeIgnoreCase(LocalUser user, LocalUser friend, String status);

    long deleteByUserAndFriend(LocalUser user, LocalUser friend);

    Optional<Friendship> findByUserAndFriend(LocalUser user, LocalUser friend);

    long countByFriendAndStatusLike(LocalUser friend, String status);

    List<Friendship> findByFriendAndStatusLike(LocalUser friend, String status);

    boolean existsByUserAndFriendAndStatusLike(LocalUser user, LocalUser friend, String status);

    List<Friendship> findByUserAndStatusLike(LocalUser user, String status);












}
