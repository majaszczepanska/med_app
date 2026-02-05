package com.maja.med_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maja.med_app.model.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>{
    List<Patient> findAllByDeletedFalse();
    
}