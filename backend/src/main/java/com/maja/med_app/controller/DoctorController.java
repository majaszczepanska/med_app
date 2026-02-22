package com.maja.med_app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.maja.med_app.model.Specialization;
import com.maja.med_app.repository.UserRepository;
import com.maja.med_app.service.DoctorService;
import com.maja.med_app.service.EmailService;
import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.util.ValidationErrorUtils;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController //class in internet
@RequestMapping("/doctors") // localhost:8080/doctors
@RequiredArgsConstructor  //create constructor for finals (line 21)
//@CrossOrigin(origins = "http://localhost:4200")
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public record CreateDoctorDto(
        @NotBlank(message = "Required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Required")
        @Size(min = 6, message= "Min. 6 characters")
        String password,

        @NotBlank(message = "Required")
        @Size(min = 3, message= "Min. 3 characters")
        @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+(-[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+)?$", message = "Capital letter & letters only")
        String firstName,

        @NotBlank(message = "Required")
        @Size(min = 3, message= "Min. 3 characters")
        @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+(-[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+)?$", message = "Capital letter & letters only")
        String lastName,

        @NotNull(message = "Required")
        Specialization specialization

    ) {}

    //save doctor
    @PostMapping
    @Transactional
    public ResponseEntity<?> addDoctor(@NonNull @Valid @RequestBody CreateDoctorDto request, BindingResult result) {  
        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);

        if (userRepository.findByEmail(request.email()).isPresent()) {
            errors.put("email", "Email already taken");
        }
        if(!errors.isEmpty()){
            throw new AppValidationException(errors);
        }

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password())); 
        user.setRole("DOCTOR"); 
        userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user); 
        doctor.setFirstName(request.firstName());
        doctor.setLastName(request.lastName());
        doctor.setSpecialization(request.specialization());

        doctorService.createDoctor(doctor);
        //return doctorService.createDoctor(doctor);
        emailService.sendDoctorAccountEmail(request.email(), request.firstName(), request.password());
        return ResponseEntity.ok(doctor);
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

    @GetMapping("/specializations")
    public ResponseEntity<Specialization[]> getSpecializations() {
        return ResponseEntity.ok(Specialization.values());
    }
    
}