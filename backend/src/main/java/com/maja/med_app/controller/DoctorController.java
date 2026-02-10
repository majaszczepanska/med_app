package com.maja.med_app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.maja.med_app.service.DoctorService;
import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.util.ValidationErrorUtils;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController //class in internet
@RequestMapping("/doctors") // localhost:8080/doctors
@RequiredArgsConstructor  //create constructor for finals (line 21)
//@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;
    /*without Lombok
    public DoctorController (DoctorRepository doctorRepository){
        this.doctorRepository = doctorRepository;    
    }
    */

    //save doctor
    @PostMapping
    public Doctor addDoctor(@NonNull @Valid @RequestBody Doctor doctor, BindingResult result) {  
        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);
        if(!errors.isEmpty()){
            throw new AppValidationException(errors);
        }
        return doctorService.createDoctor(doctor);
    }

    // get doctors
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    // update doctor 
    @PutMapping("/{id}")
    public Doctor updateDoctor(@PathVariable Long id, @Valid @RequestBody Doctor doctor, BindingResult result){
        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);
        if (!errors.isEmpty()){
            throw new AppValidationException(errors);
        }
        return doctorService.updateDoctor(id, doctor);
    }


    @DeleteMapping("/{id}")
    public void deleteDoctor(@NonNull @PathVariable Long id) {
        doctorService.deleteDoctor(id);
    }
    
}