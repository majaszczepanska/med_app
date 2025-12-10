package com.maja.med_app.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null || appointment.getDoctor().getId() == 0 ){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Needed doctors id");
        }
        Long doctorId = appointment.getDoctor().getId();
        Doctor doctorFromDb = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No doctor with this id"));
        if(appointmentRepository.existsByDoctorIdAndVisitTime(doctorId, appointment.getVisitTime())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Doctor is occupied");
        }

        if (appointment.getPatient() == null || appointment.getPatient().getId() == null || appointment.getPatient().getId() == 0){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Needed patient's id");
        }
        Long patientId = appointment.getPatient().getId();
        Patient patientFromDb = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No patient with this id"));

        appointment.setDoctor(doctorFromDb);
        appointment.setPatient(patientFromDb);

        LocalDateTime visitTime = appointment.getVisitTime();
        if (visitTime == null){
            appointment.setVisitTime(LocalDateTime.now());
        }
        //(visitTime.getMinute() != 0 && visitTime.getMinute() != 15 && visitTime.getMinute() != 30 && visitTime.getMinute() != 45)
        if(visitTime.getMinute()% 15 != 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointments only 15 minutes after previous");
        }

        return appointmentRepository.save(appointment);
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }


    @GetMapping("/doctor/{doctorId}/avaliable")
    public List<String> getAvaliableSlots( 
        @PathVariable Long doctorId,
        @RequestParam String date) {
        
        LocalDate searchDate = LocalDate.parse(date);

        List<Appointment> takenAppointments = appointmentRepository.findAllByDoctorIdAndVisitTimeBetween(doctorId, searchDate.atStartOfDay(), searchDate.atTime(23, 59,59));
        List<String> availableSlots = new ArrayList<>();
        LocalDateTime startWork = searchDate.atTime(8, 0);
        LocalDateTime endWork = searchDate.atTime(16, 0);
        
        while (startWork.isBefore(endWork)){
            final LocalDateTime currentSlot = startWork;
            boolean isBusy = takenAppointments.stream()
                .anyMatch(appointment -> appointment.getVisitTime().isEqual(currentSlot));

            if (!isBusy){
                availableSlots.add(currentSlot.toLocalTime().toString());
            }
            startWork = startWork.plusMinutes(15);
        }

        return availableSlots;
    }   


    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentRepository.deleteById(id);
    }
}
