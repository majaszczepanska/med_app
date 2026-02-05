package com.maja.med_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maja.med_app.model.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    // Spring Data JPA working
    List<Doctor> findAllByDeletedFalse();
}