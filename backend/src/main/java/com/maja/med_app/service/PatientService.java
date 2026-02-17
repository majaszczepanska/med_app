package com.maja.med_app.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.model.AppointmentStatus;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.AppointmentRepository;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public Patient createPatient(@NonNull Patient patient) {
        validateDoctor(patient);
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAllByDeletedFalse();
    }

    public Patient updatePatient(@NonNull Long id, @NonNull Patient updatedPatient) {
        Patient existingPatient = patientRepository.findById(id).orElse(null);

        if (existingPatient == null){
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "No patient with this id");
            throw new AppValidationException(errors);
        }

        if (updatedPatient.getMainDoctor() != null){
            validateDoctor(updatedPatient);
            existingPatient.setMainDoctor(updatedPatient.getMainDoctor());
        }
  
        existingPatient.setFirstName(updatedPatient.getFirstName());
        existingPatient.setLastName(updatedPatient.getLastName());
        existingPatient.setPesel(updatedPatient.getPesel());
        existingPatient.setMainDoctor(updatedPatient.getMainDoctor());

        return patientRepository.save(existingPatient);
    }

    public void deletePatient(@NonNull Long id){
        Patient patient = patientRepository.findById(id).orElse(null);
        if(patient == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
        }
        boolean hasFutureAppointments = appointmentRepository.existsByPatientIdAndVisitTimeAfterAndStatusNot(id, LocalDateTime.now(), AppointmentStatus.CANCELLED);
        if (hasFutureAppointments){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete patient with future appointments");
        }
        patient.setDeleted(true);
        patientRepository.save(patient);
    }

    private void validateDoctor(Patient patient) {
        if (patient.getMainDoctor() != null){
            Long doctorId = patient.getMainDoctor().getId();
            if (patient.getMainDoctor().getId() == null || patient.getMainDoctor().getId() == 0){ 
                Map<String, String> errors = new HashMap<>();
                errors.put("mainDoctor", "ID required (minimum 1)"); 
                throw new AppValidationException(errors);
            }
            @SuppressWarnings("null")
            Optional<Doctor> fullDoctor = doctorRepository.findById(doctorId);
            if (fullDoctor.isEmpty()){
                Map<String, String> errors = new HashMap<>();
                errors.put("mainDoctor", "ID not found in database");
                throw new AppValidationException(errors);
            } else {
                patient.setMainDoctor(fullDoctor.get());
            }
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("mainDoctor", "Required");
            throw new AppValidationException(errors);
        }
    }

    @SuppressWarnings("null")
    public Patient getPatientById(Long id) {
    return patientRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient with id " + id + " not found"));
}
}
