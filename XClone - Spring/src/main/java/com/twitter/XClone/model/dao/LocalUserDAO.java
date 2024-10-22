package com.twitter.XClone.model.dao;

import com.twitter.XClone.model.LocalUser;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface LocalUserDAO extends ListCrudRepository<LocalUser, Long> {

    Optional<LocalUser> findByUsernameIgnoreCase(String username);

    Optional<LocalUser> findByEmailIgnoreCase(String email);

    List<LocalUser> findByUsernameContainsIgnoreCase(String username);






}
