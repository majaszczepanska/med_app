package com.maja.med_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maja.med_app.model.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email); 
    Optional<AppUser> findByVerificationToken(String verificationToken);
}
