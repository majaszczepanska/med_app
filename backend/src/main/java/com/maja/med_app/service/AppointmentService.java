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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.model.AppUser;
import com.maja.med_app.model.Appointment;
import com.maja.med_app.model.AppointmentStatus;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.AppointmentRepository;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;
import com.maja.med_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    //POST
    //method to create new appointment 
    public Appointment createAppointment(Appointment appointment){
        enforceRoleBoundaries(appointment);

        validateAppointment(appointment);

        Map<String, String> errors = new HashMap<>();
        //check if doctor is avaliable at that time, patient doesn't have another appointment at that time
        if(appointmentRepository.existsByDoctorIdAndVisitTimeAndStatusNot(appointment.getDoctor().getId(), appointment.getVisitTime(), AppointmentStatus.CANCELLED)){
            errors.put("visitTime", "Doctor is occupied");
        }
        if(appointmentRepository.existsByPatientIdAndVisitTimeAndStatusNot(appointment.getPatient().getId(), appointment.getVisitTime(), AppointmentStatus.CANCELLED)){
            errors.put("visitTime", "Patient has an appointment");
        }
        if(!errors.isEmpty()) {
            throw new AppValidationException(errors);
        }

        //save appointment and send confirmation email to patient
        Appointment savedAppointment = appointmentRepository.save(appointment);
        if (savedAppointment.getPatient() != null && savedAppointment.getPatient().getUser() != null) {
            emailService.sendAppointmentConfirmation(
                savedAppointment.getPatient().getUser().getEmail(), 
                savedAppointment.getPatient().getFirstName(), 
                savedAppointment.getDoctor().getFirstName() + " " + savedAppointment.getDoctor().getLastName(), 
                savedAppointment.getVisitTime().toString()
            );
        }
        return savedAppointment;
    }

    //GET
    //method to get all appointments (admin all, doctor their own, patient their own with hidden details of other patients)
    public List<Appointment> getAllAppointments() {
        List<Appointment> allAppointments = appointmentRepository.findAllByStatusNot(AppointmentStatus.CANCELLED);

        //check user role and filter appointments according to RODO
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return allAppointments;
        }

        String email = auth.getName();
        AppUser user = userRepository.findByEmail(email).orElse(null);

        //PATIENT displays appointments (theirs and other patients' with hidden details)
        if (user != null && "PATIENT".equals(user.getRole())) {
            Patient myPatientProfile = patientRepository.findByUser(user).orElse(null);
            Long myId = (myPatientProfile != null) ? myPatientProfile.getId() : -1;

            List<Appointment> rodoList = new ArrayList<>();
            for (Appointment appointment : allAppointments) {
                if (!appointment.getPatient().getId().equals(myId)) {
                    Appointment hiddenAppointment = new Appointment();
                    hiddenAppointment.setId(appointment.getId());
                    hiddenAppointment.setVisitTime(appointment.getVisitTime());
                    hiddenAppointment.setDoctor(appointment.getDoctor());
                    hiddenAppointment.setStatus(appointment.getStatus());


                    Patient hiddenPatient = new Patient();
                    hiddenPatient.setFirstName("Reserved");   
                    hiddenPatient.setLastName(""); 
                    hiddenAppointment.setPatient(hiddenPatient);
                    hiddenAppointment.setDescription("Private Appointment");

                    rodoList.add(hiddenAppointment);
                }
                else {
                    rodoList.add(appointment);
                }
            }
            return rodoList;
        }

        //DOCTOR displays appointments (only their own)
        if (user != null && "DOCTOR".equals(user.getRole())) {
            Doctor myDoctorProfile = doctorRepository.findByUser(user).orElse(null);
            Long myId = (myDoctorProfile != null) ? myDoctorProfile.getId() : -1L;
            
            List<Appointment> doctorList = new ArrayList<>();
            for (Appointment app : allAppointments) {
                if (app.getDoctor() != null && app.getDoctor().getId().equals(myId)) {
                    doctorList.add(app);
                }
            }
            return doctorList;
        }
        
        //ADMIN displays appointments (all)
        return allAppointments;
    }

    //PUT
    //method to update appointment details
    public Appointment updateAppointment(Long id, Appointment updatedAppointment){
        Appointment existingAppointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        verifyOwnership(existingAppointment);
        enforceRoleBoundaries(updatedAppointment);

        //only update fields that are present in the request
        if(updatedAppointment.getVisitTime() != null){
            existingAppointment.setVisitTime(updatedAppointment.getVisitTime());
        }

        //check if doctor/patient with given id exists, if doctor/patient is being updated
        if(updatedAppointment.getDoctor() != null && updatedAppointment.getDoctor().getId() != null) {
            Doctor newDoctor = doctorRepository.findById(updatedAppointment.getDoctor().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor with this ID not found"));
            existingAppointment.setDoctor(newDoctor);
        }
        if(updatedAppointment.getPatient() != null && updatedAppointment.getPatient().getId() != null) {
            Patient newPatient = patientRepository.findById(updatedAppointment.getPatient().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient with this ID not found"));
            existingAppointment.setPatient(newPatient);
        }
        
        existingAppointment.setDescription(updatedAppointment.getDescription());

        //validate updated appointment details
        Map<String, String> errors = new HashMap<>();
        if (appointmentRepository.existsByDoctorIdAndVisitTimeAndStatusNotAndIdNot(existingAppointment.getDoctor().getId(), existingAppointment.getVisitTime(), AppointmentStatus.CANCELLED, id)){
            errors.put("visitTime", "Doctor is occupied at this time");
        }
        if (appointmentRepository.existsByPatientIdAndVisitTimeAndStatusNotAndIdNot(existingAppointment.getPatient().getId(), existingAppointment.getVisitTime(), AppointmentStatus.CANCELLED, id)){
            errors.put("visitTime", "This patient already has an appointment at this time");
        }
        return appointmentRepository.save(existingAppointment);
    }


    //SOFT DELETE
    //method to cancel appointment (set status to CANCELLED - do not delete to keep medical history)
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        
        verifyOwnership(appointment);
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    //COMPLETE (after visit)
    //method to mark appointment as completed and add diagnosis (used by doctor after visit)
    public void completeAppointment(Long id, String diagnosis) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        verifyOwnership(appointment);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            AppUser user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user == null || !"DOCTOR".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can complete appointments");
            }
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        if (diagnosis != null && !diagnosis.trim().isEmpty()) {
            appointment.setDescription(diagnosis);
        }
        
        appointmentRepository.save(appointment);
    }


    //GET AVAILABLE SLOTS
    //method to get available appointment slots for a doctor on a given date
    public List<String> getAvailableSlots(Long doctorId,String date) {
        LocalDate searchDate = LocalDate.parse(date);
        List<Appointment> takenAppointments = appointmentRepository.findAllByDoctorIdAndVisitTimeBetweenAndStatusNot(doctorId, searchDate.atStartOfDay(), searchDate.atTime(23, 59,59), AppointmentStatus.CANCELLED);
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

    //GET
    //method to get appointments of a patient 
    public List<Appointment> getAppointmentsByPatient(Long patientId){
        return appointmentRepository.findAllByPatientIdAndStatusNot(patientId, AppointmentStatus.CANCELLED);
    }


    //unused method to check if appointment is being edited less than 1 hour before visit (can be used to restrict functions for users)
    private void checkEditTime(LocalDateTime visitTime) {
        Map<String, String> errors = new HashMap<>();
        LocalDateTime oneHourFromNow = LocalDateTime.now().plusHours(1);
        if(visitTime.isBefore(oneHourFromNow)) {
            errors.put("visitTime", "Cannot edit appointment less than 1 hour before visit");
        }
    }


    //EXTRA VALIDATION METHODS
    //method to validate appointment details (visit time, doctor and patient existence)
    private void validateAppointment(Appointment appointment){
        Map<String, String> errors = new HashMap<>();

        //VALIDATE VISIT TIME
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
        
        //VALIDATE DOCTOR
        Long doctorId = null;
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null || appointment.getDoctor().getId() == 0 ){
            errors.put("doctor", "Required");
        } else {
            //fetch full entities from db
            doctorId = appointment.getDoctor().getId();
            Optional<Doctor> doctorFromDb = doctorRepository.findById(doctorId);
            if (doctorFromDb.isEmpty()){
                errors.put("doctor", "No doctor with this id");
            } else {
                appointment.setDoctor(doctorFromDb.get());
            }
        }

        //VALIDATE PATIENT
        Long patientId = null;
         if (appointment.getPatient() == null || appointment.getPatient().getId() == null || appointment.getPatient().getId() == 0){
            errors.put("patient", "Required");
        } else {
            //fetch full entities from db
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


    //CHECK OWNERSHIP
    //method to verify if the user trying to modify the appointment is either the patient or doctor assigned to this appointment (or admin) - RODO protection
    private void verifyOwnership(Appointment appointment) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return;
        
        AppUser user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null || "ADMIN".equals(user.getRole())) return; 

        if ("PATIENT".equals(user.getRole())) {
            Patient me = patientRepository.findByUser(user).orElse(null);
            if (me == null || !appointment.getPatient().getId().equals(me.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You can only modify your own appointments");
            }
        } else if ("DOCTOR".equals(user.getRole())) {
            Doctor myDoc = doctorRepository.findByUser(user).orElse(null);
            if (myDoc == null || !appointment.getDoctor().getId().equals(myDoc.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You can only modify your own appointments");
            }
        }
    }


    //ENFORCE ROLE BOUNDARIES
    //method to set doctor/patient of the appointment based on the logged in user role 
    private void enforceRoleBoundaries(Appointment appointment){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return;
        AppUser user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null || "ADMIN".equals(user.getRole())) return;

        if ("PATIENT".equals(user.getRole())) {
            Patient me = patientRepository.findByUser(user).orElse(null);
            if (me != null) appointment.setPatient(me); 
        }
        else if("DOCTOR".equals(user.getRole())) {
            Doctor me = doctorRepository.findByUser(user).orElse(null);
            if (me != null) appointment.setDoctor(me);
        }
        
    }
}
