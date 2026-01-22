package com.maja.med_app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maja.med_app.model.Doctor;
import com.maja.med_app.repository.DoctorRepository;

import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.util.ValidationErrorUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController //class in internet
@RequestMapping("/doctors") // localhost:8080/doctors
@RequiredArgsConstructor  //create constructor for finals (line 21)
public class DoctorController {

    private final DoctorRepository doctorRepository;
    /*without Lombok
    public DoctorController (DoctorRepository doctorRepository){
        this.doctorRepository = doctorRepository;    
    }
    */

    //save doctor
    @PostMapping
    public Doctor addDoctor(@Valid @RequestBody Doctor doctor, BindingResult result) {  

        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);

        if(!errors.isEmpty()){
            throw new AppValidationException(errors);
        }
        return doctorRepository.save(doctor);
    }

    // get doctors
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteDoctor(@PathVariable Long id) {
        doctorRepository.deleteById(id);
    }
    
}