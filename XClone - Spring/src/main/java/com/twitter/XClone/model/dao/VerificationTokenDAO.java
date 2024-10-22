package com.twitter.XClone.model.dao;

import com.twitter.XClone.model.LocalUser;
import com.twitter.XClone.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface VerificationTokenDAO extends ListCrudRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    void deleteByUser(LocalUser user);


}
