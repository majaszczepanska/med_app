package com.maja.med_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maja.med_app.model.AppUser;
import com.maja.med_app.model.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>{
    List<Patient> findAllByDeletedFalse();
    Optional<Patient> findByUser(AppUser user);
    
}