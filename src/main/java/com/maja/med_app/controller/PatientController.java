package com.maja.med_app.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public Patient addPatient(@Valid @RequestBody Patient patient){
        if (patient.getMainDoctor() != null && patient.getMainDoctor().getId() != null && patient.getMainDoctor().getId() != 0){
            Long doctorId = patient.getMainDoctor().getId();
            Doctor fullDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("No doctor with this id"));
            patient.setMainDoctor(fullDoctor);
        }
        return patientRepository.save(patient);
    }

    @GetMapping
    public List<Patient> gatAllPatients(){
        return patientRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deletePatient(@PathVariable Long id) {
        patientRepository.deleteById(id);
    }
}
