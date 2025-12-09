package com.maja.med_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.maja.med_app.model.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>  {
    //empty
}
