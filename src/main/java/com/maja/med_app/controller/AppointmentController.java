package com.maja.med_app.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.maja.med_app.exception.AppValidationException;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @PostMapping
    public Appointment addAppointment(@Valid @RequestBody Appointment appointment){
    
        Map<String, String> errors = new HashMap<>();

        //First validate visit time
        LocalDateTime visitTime = appointment.getVisitTime();

        if(visitTime != null){
            if(visitTime.isBefore(LocalDateTime.now())){
                errors.put("visitTime", "Visit time must be in the future");
            }
            if(visitTime.getHour() < 8 || visitTime.getHour() >= 16){
                String currentMessage = errors.getOrDefault("visitTime", "");
                String separator = currentMessage.isEmpty() ? "" : ", \n";
                errors.put("visitTime", currentMessage + separator + "Appointments only between 8:00 and 16:00");
            }   
            if(visitTime.getMinute()% 15 != 0){
                String currentMessage = errors.getOrDefault("visitTime", "");
                String separator = currentMessage.isEmpty() ? "" : ", \n";
                errors.put("visitTime",  currentMessage + separator + "Appointments only 15 minutes after previous (for example at 8:00, 8:15, 8:30 etc.)");
            }
        } else {
            errors.put("visitTime", "Visit time is mandatory");
        }
        
        //Validate if ids are present
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null || appointment.getDoctor().getId() == 0 ){
            errors.put("doctor", "Needed doctor id, id cannot be null or 0");
        }
         if (appointment.getPatient() == null || appointment.getPatient().getId() == null || appointment.getPatient().getId() == 0){
            errors.put("patient", "Needed patient id, id cannot be null or 0");
        }

        //all errors collected, throw exception
        if (!errors.isEmpty()){
            throw new AppValidationException(errors);
        }

        //Data from db

        Long doctorId = appointment.getDoctor().getId();
        Long patientId = appointment.getPatient().getId();

        //Fetch full entities from db
        Doctor doctorFromDb = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No doctor with this id"));
        Patient patientFromDb = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No patient with this id"));



        //Check if doctor is avaliable at that time
        if(appointmentRepository.existsByDoctorIdAndVisitTime(doctorId, appointment.getVisitTime())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Doctor is occupied");
        }

       
        //Set full entities to appointment
        appointment.setDoctor(doctorFromDb);
        appointment.setPatient(patientFromDb);
        


        return appointmentRepository.save(appointment);
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }


    @GetMapping("/doctor/{doctorId}/available")
    public List<String> getAvailableSlots( 
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
    public void deleteAppointment(@NonNull@PathVariable Long id) {
        appointmentRepository.deleteById(id);
    }
}
