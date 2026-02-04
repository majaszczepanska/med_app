package com.maja.med_app.controller;

import java.lang.foreign.Linker.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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
//@CrossOrigin (origins = "http://localhost:4200")
@CrossOrigin(origins = "*")
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
                errors.put("visitTime",  currentMessage + separator + "Appointments only 15 minutes after previous \n       (8:00 8:15 8:30 etc.)");
            }
        } else {
            errors.put("visitTime", "Required (date & time)");
        }
        
        //Validate doctor
        Long doctorId = null;
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null || appointment.getDoctor().getId() == 0 ){
            errors.put("doctor", "Required");
        } else {
            //Fetch full entities from db
            doctorId = appointment.getDoctor().getId();
            Optional<Doctor> doctorFromDb = doctorRepository.findById(doctorId);
            if (doctorFromDb.isEmpty()){
                errors.put("doctor", "No doctor with this id");
            } else {
                appointment.setDoctor(doctorFromDb.get());
            }
        }

        //Validate patient
        Long patientId = null;
         if (appointment.getPatient() == null || appointment.getPatient().getId() == null || appointment.getPatient().getId() == 0){
            errors.put("patient", "Required");
        } else {
            //Fetch full entities from db
            patientId = appointment.getPatient().getId();
            Optional<Patient> patientFromDb = patientRepository.findById(patientId);
            if (patientFromDb.isEmpty()){
                errors.put("patient", "No patient with this id");
            } else {
                appointment.setPatient(patientFromDb.get());
            }
        }

        //all errors collected, throw exception
        if (!errors.isEmpty()){
            throw new AppValidationException(errors);
        }

        //Check if doctor is avaliable at that time
        if(appointmentRepository.existsByDoctorIdAndVisitTime(doctorId, appointment.getVisitTime())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Doctor is occupied");
        }

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
    public void deleteAppointment(@NonNull @PathVariable Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        
        if (appointment == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        } else {
            LocalDateTime oneDayFromNow = LocalDateTime.now().plusDays(1);
            if(appointment.getVisitTime().isBefore(oneDayFromNow)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete appointment less than 1 day before visit");
            }
        }

        appointmentRepository.deleteById(id);
    }
}
