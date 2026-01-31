package com.maja.med_app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public Patient createPatient(@NonNull Patient patient) {
        validateDoctor(patient);
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
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
        patientRepository.deleteById(id);
    }

    private void validateDoctor(Patient patient) {
        if (patient.getMainDoctor() != null){
            Long doctorId = patient.getMainDoctor().getId();
            if (patient.getMainDoctor().getId() == null || patient.getMainDoctor().getId() == 0){ 
                Map<String, String> errors = new HashMap<>();
                errors.put("doctor", "Needed doctor id, id cannot be null or 0"); 
                throw new AppValidationException(errors);
            }
            Optional<Doctor> fullDoctor = doctorRepository.findById(doctorId);
            if (fullDoctor.isEmpty()){
                Map<String, String> errors = new HashMap<>();
                errors.put("mainDoctor", "No doctor with this id");
                throw new AppValidationException(errors);
            } else {
                patient.setMainDoctor(fullDoctor.get());
            }
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("mainDoctor", "Main doctor is mandatory");
            throw new AppValidationException(errors);
        }
    }
}
