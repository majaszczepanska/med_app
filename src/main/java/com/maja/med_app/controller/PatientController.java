package com.maja.med_app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;



import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @PostMapping
    public Patient addPatient(@Valid @RequestBody Patient patient, BindingResult result){
        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);

        if (patient.getMainDoctor() != null && patient.getMainDoctor().getId() != null && patient.getMainDoctor().getId() != 0){ 
            Long doctorId = patient.getMainDoctor().getId();
            Optional<Doctor> fullDoctor = doctorRepository.findById(doctorId);
            if (fullDoctor.isEmpty()){
                errors.put("mainDoctor", "No doctor with this id");
            } else {
                patient.setMainDoctor(fullDoctor.get());
            }
            
        } else {
            errors.put("mainDoctor", "Main doctor is mandatory");
        }
        if (!errors.isEmpty()){
            throw new AppValidationException(errors);
        }
        return patientRepository.save(patient);
        
    }

    @GetMapping
    public List<Patient> gatAllPatients(){
        return patientRepository.findAll();
    }

    @PutMapping("/{id}")
    public Patient updatePatient(@PathVariable Long id, @Valid @RequestBody Patient updatePatient, BindingResult result){

        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);
        
        Patient existingPatient = patientRepository.findById(id).orElse(null);

        if (existingPatient == null){
            errors.put("id", "No patient with this id");
            throw new AppValidationException(errors);
        }

        if (updatePatient.getMainDoctor() != null && updatePatient.getMainDoctor().getId() != null && updatePatient.getMainDoctor().getId() != 0){ 
            Long doctorId = updatePatient.getMainDoctor().getId();
            Optional<Doctor> fullDoctor = doctorRepository.findById(doctorId);
            if (fullDoctor.isEmpty()){
                errors.put("mainDoctor", "No doctor with this id");
            } else {
                updatePatient.setMainDoctor(fullDoctor.get());
            }
        }else {
            errors.put("mainDoctor", "Main doctor is mandatory");
        }
        if (!errors.isEmpty()){
            throw new AppValidationException(errors);
        }

        existingPatient.setFirstName(updatePatient.getFirstName());
        existingPatient.setLastName(updatePatient.getLastName());
        existingPatient.setPesel(updatePatient.getPesel());
        existingPatient.setMainDoctor(updatePatient.getMainDoctor());

        return patientRepository.save(existingPatient);

    }

    @DeleteMapping("/{id}")
    public void deletePatient(@PathVariable Long id) {
        patientRepository.deleteById(id);
    }
}