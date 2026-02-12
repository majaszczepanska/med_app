package com.maja.med_app.controller;

import java.util.List;
import java.util.Map;


import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maja.med_app.model.Patient;
import com.maja.med_app.service.PatientService;
import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.util.ValidationErrorUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public Patient addPatient(@Valid @RequestBody Patient patient, BindingResult result){
        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);
        if (!errors.isEmpty()){
            throw new AppValidationException(errors);
        }
        return patientService.createPatient(patient);
    }

    @GetMapping
    public List<Patient> gatAllPatients(){
        return patientService.getAllPatients();
    }

    @PutMapping("/{id}")
    public Patient updatePatient(@PathVariable Long id, @Valid @RequestBody Patient updatedPatient, BindingResult result){
        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);
        if (!errors.isEmpty()){
            throw new AppValidationException(errors);
        }
        return patientService.updatePatient(id, updatedPatient);
    }

    @DeleteMapping("/{id}")
    public void deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
    }
}

