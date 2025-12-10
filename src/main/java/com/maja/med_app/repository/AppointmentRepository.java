package com.maja.med_app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maja.med_app.model.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>  {
    //existBy (select count(*)>0), Doctor (in appointment look for doctor in doctor look for the id field), VisitTime (in appointment look for VisitTime field)
    boolean existsByDoctorIdAndVisitTime(Long doctorId, LocalDateTime visitTime);
    List<Appointment> findAllByDoctorIdAndVisitTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
}
