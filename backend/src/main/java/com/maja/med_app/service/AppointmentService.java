package com.maja.med_app.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.model.Appointment;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.AppointmentRepository;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    //POST
    public Appointment createAppointment(Appointment appointment){
        validateAppointment(appointment);
        //Check if doctor is avaliable at that time
        if(appointmentRepository.existsByDoctorIdAndVisitTimeAndDeletedFalse(appointment.getDoctor().getId(), appointment.getVisitTime())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Doctor is occupied");
        }
        if(appointmentRepository.existsByPatientIdAndVisitTimeAndDeletedFalse(appointment.getPatient().getId(), appointment.getVisitTime())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Patient has an appointment");
        }
        return appointmentRepository.save(appointment);
    }

    //GET
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAllByDeletedFalse();
    }

    //PUT
    public Appointment updateAppointment(Long id, Appointment updatedAppointment){
        Appointment existingAppointment = appointmentRepository.findById(id).orElse(null);

        if (existingAppointment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        } else {
            //ADMIN OPTION
            //checkEditTime(updatedAppointment.getVisitTime());
            if(updatedAppointment.getVisitTime() != null){
                existingAppointment.setVisitTime(updatedAppointment.getVisitTime());
            }
            if(updatedAppointment.getDoctor() != null && updatedAppointment.getDoctor().getId() != null) {
                Doctor newDoctor = doctorRepository.findById(updatedAppointment.getDoctor().getId()).orElse(null);
                if (newDoctor == null){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor with this ID not found");
                } else {
                    existingAppointment.setDoctor(newDoctor);
                }
            }
            if(updatedAppointment.getPatient() != null && updatedAppointment.getPatient().getId() != null) {
                Patient newPatient = patientRepository.findById(updatedAppointment.getPatient().getId()).orElse(null);
                if (newPatient == null){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient with this ID not found");
                } else {
                    existingAppointment.setPatient(newPatient);
                }
            }
            
            existingAppointment.setDescription(updatedAppointment.getDescription());
            if (appointmentRepository.existsByDoctorIdAndVisitTimeAndDeletedFalseAndIdNot(existingAppointment.getDoctor().getId(), existingAppointment.getVisitTime(), id)){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor is occupied at this time");
            }
            if (appointmentRepository.existsByPatientIdAndVisitTimeAndDeletedFalseAndIdNot(existingAppointment.getPatient().getId(), existingAppointment.getVisitTime(), id)){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This patient already has an appointment at this time");
            }
        }
        return appointmentRepository.save(existingAppointment);
    }


    //SOFT DELETE
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        
        if (appointment == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }
        //ADMIN OPTION
        //checkEditTime(appointment.getVisitTime());
        
        appointment.setDeleted(true);
        appointmentRepository.save(appointment);
        //appointmentRepository.deleteById(id);
    }


    //GET AVAILABLE SLOTS
    public List<String> getAvailableSlots(Long doctorId,String date) {
        LocalDate searchDate = LocalDate.parse(date);
        List<Appointment> takenAppointments = appointmentRepository.findAllByDoctorIdAndVisitTimeBetweenAndDeletedFalse(doctorId, searchDate.atStartOfDay(), searchDate.atTime(23, 59,59));
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

    public List<Appointment> getAppointmentsByPatient(Long patientId){
        return appointmentRepository.findAllByPatientIdAndDeletedFalse(patientId);
    }


    private void checkEditTime(LocalDateTime visitTime) {
        LocalDateTime oneHourFromNow = LocalDateTime.now().plusHours(1);
        if(visitTime.isBefore(oneHourFromNow)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit appointment less than 1 hour before visit");
        }
    }


    private void validateAppointment(Appointment appointment){
        Map<String, String> errors = new HashMap<>();

        //Validate visit time
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
            if(visitTime.getDayOfWeek() == DayOfWeek.SATURDAY || visitTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
                String currentMessage = errors.getOrDefault("visitTime", "");
                String separator = currentMessage.isEmpty() ? "" : ", \n";
                errors.put("visitTime", currentMessage + separator + "Clinic is closed on weekends");
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
    }

}
