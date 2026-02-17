package com.maja.med_app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maja.med_app.model.Appointment;
import com.maja.med_app.model.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>  {
    //List<Appointment> findAllByDeletedFalse();
    List<Appointment> findAllByStatusNot(AppointmentStatus status);
   
    //boolean existsByPatientIdAndVisitTimeAndDeletedFalse(Long patientId, LocalDateTime visitTime);
    boolean existsByPatientIdAndVisitTimeAndStatusNot(Long patientId, LocalDateTime visitTime, AppointmentStatus status);
    //boolean existsByDoctorIdAndVisitTimeAndDeletedFalse(Long doctorId, LocalDateTime visitTime);
    boolean existsByDoctorIdAndVisitTimeAndStatusNot(Long doctorId, LocalDateTime visitTime, AppointmentStatus status);

    //boolean existsByPatientIdAndVisitTimeAfterAndDeletedFalse(Long patientId, LocalDateTime visitTime);
    boolean existsByPatientIdAndVisitTimeAfterAndStatusNot(Long patientId, LocalDateTime visitTime, AppointmentStatus status);

    //boolean existsByDoctorIdAndVisitTimeAfterAndDeletedFalse(Long doctorId, LocalDateTime visitTime);
    boolean existsByDoctorIdAndVisitTimeAfterAndStatusNot(Long doctorId, LocalDateTime visitTime, AppointmentStatus status);

    //boolean existsByPatientIdAndVisitTimeAndDeletedFalseAndIdNot(Long patientId, LocalDateTime visitTime, Long appointmentId);
    boolean existsByPatientIdAndVisitTimeAndStatusNotAndIdNot(Long patientId, LocalDateTime visitTime, AppointmentStatus status, Long appointmentId);

    //boolean existsByDoctorIdAndVisitTimeAndDeletedFalseAndIdNot(Long doctorId, LocalDateTime visitTime, Long appointmentId);
    boolean existsByDoctorIdAndVisitTimeAndStatusNotAndIdNot(Long doctorId, LocalDateTime visitTime, AppointmentStatus status, Long appointmentId);
    
    //List<Appointment> findAllByDoctorIdAndVisitTimeBetweenAndDeletedFalse(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findAllByDoctorIdAndVisitTimeBetweenAndStatusNot(Long doctorId, LocalDateTime start, LocalDateTime end, AppointmentStatus status);

    List<Appointment> findAllByPatientIdAndStatusNot(Long patientId, AppointmentStatus status);
}
