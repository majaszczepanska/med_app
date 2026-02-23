package com.maja.med_app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maja.med_app.model.Appointment;
import com.maja.med_app.model.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>  {

    List<Appointment> findAllByStatusNot(AppointmentStatus status);

    boolean existsByPatientIdAndVisitTimeAndStatusNot(Long patientId, LocalDateTime visitTime, AppointmentStatus status);

    boolean existsByDoctorIdAndVisitTimeAndStatusNot(Long doctorId, LocalDateTime visitTime, AppointmentStatus status);

    boolean existsByPatientIdAndVisitTimeAfterAndStatusNot(Long patientId, LocalDateTime visitTime, AppointmentStatus status);

    boolean existsByDoctorIdAndVisitTimeAfterAndStatusNot(Long doctorId, LocalDateTime visitTime, AppointmentStatus status);

    boolean existsByPatientIdAndVisitTimeAndStatusNotAndIdNot(Long patientId, LocalDateTime visitTime, AppointmentStatus status, Long appointmentId);

    boolean existsByDoctorIdAndVisitTimeAndStatusNotAndIdNot(Long doctorId, LocalDateTime visitTime, AppointmentStatus status, Long appointmentId);

    List<Appointment> findAllByDoctorIdAndVisitTimeBetweenAndStatusNot(Long doctorId, LocalDateTime start, LocalDateTime end, AppointmentStatus status);

    List<Appointment> findAllByPatientIdAndStatusNot(Long patientId, AppointmentStatus status);
}
