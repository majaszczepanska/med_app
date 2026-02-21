package com.maja.med_app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.hibernate.validator.constraints.pl.PESEL;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

import com.maja.med_app.model.AppUser;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;
import com.maja.med_app.repository.UserRepository;
import com.maja.med_app.service.PatientService;
import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.util.ValidationErrorUtils;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public record UpdateProfileDto(
        @NotBlank(message = "Required")
        @Size(min = 3, message= "Min. 3 characters")
        @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+(-[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+)?$", message = "Capital letter & letters only")
        String firstName,

        @NotBlank(message = "Required")
        @Size(min = 3, message= "Min. 3 characters")
        @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+(-[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+)?$", message = "Capital letter & letters only")
        String lastName, 

        @Column(unique = true)
        @PESEL(message = "Invalid PESEL format")
        String pesel, 

        @Pattern(regexp = "^[0-9]{9}$", message = "Phone number must consist of exactly 9 digits")
        String phoneNumber, 
        String address, 
        String disease,
        Long mainDoctorId
    ) {}

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

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<Patient> getCurrentPatientData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUser(user) 
            .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        return ResponseEntity.ok(patient);
    }

    @PutMapping("/me/profile") 
    public ResponseEntity<?> updatePatientProfile(@Valid @RequestBody UpdateProfileDto request, BindingResult result) {
        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);
        if (!errors.isEmpty()){
            throw new AppValidationException(errors); 
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Patient patient = patientRepository.findByUser(user) 
            .orElseThrow(() -> new RuntimeException("Patient profile not found"));


        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setPesel(request.pesel());

        patient.setPhoneNumber(request.phoneNumber());
        patient.setAddress(request.address());
        patient.setDisease(request.disease());

        if(request.mainDoctorId() != null) {
            @SuppressWarnings("null")
            Doctor doctor = doctorRepository.findById(request.mainDoctorId()).orElse(null);
            patient.setMainDoctor(doctor);
        }

        patientRepository.save(patient);

        return ResponseEntity.ok("Profile updated successfully");
    }

}

