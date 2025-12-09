package com.maja.med_app.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maja.med_app.model.Appointment;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.AppointmentRepository;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @PostMapping
    public Appointment addAppointment(@RequestBody Appointment appointment){
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null){
            throw new RuntimeException("Error: Needed doctors id");
        }
        Long doctorId = appointment.getDoctor().getId();
        Doctor doctorFromDb = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("No doctor with this id"));

        if (appointment.getPatient() == null || appointment.getPatient().getId() == null){
            throw new RuntimeException("Error: Needed patient's id");
        }
        Long patientId = appointment.getPatient().getId();
        Patient patientFromDb = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("No patient with this id"));

        appointment.setDoctor(doctorFromDb);
        appointment.setPatient(patientFromDb);
        return appointmentRepository.save(appointment);
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentRepository.deleteById(id);
    }
}
