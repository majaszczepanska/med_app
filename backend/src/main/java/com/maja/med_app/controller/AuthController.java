package com.maja.med_app.controller;

import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

import org.hibernate.validator.constraints.pl.PESEL;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.model.AppUser;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;
import com.maja.med_app.repository.UserRepository;
import com.maja.med_app.service.EmailService;
import com.maja.med_app.util.ValidationErrorUtils;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;


//CONTROLLER LAYER
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;

    public record UserDto(Long id, Long patientId, Long doctorId, String email, String role) {}
    public record RegisterDto(
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

        @NotBlank(message = "Required")
        @PESEL(message = "Invalid PESEL format")
        String pesel
    ) {}

    public record ChangePasswordDto(
        @NotBlank(message = "Required")
        String oldPassword,

        @NotBlank(message = "Required")
        @Size(min = 6, message = "Min. 6 characters")
        String newPassword
    ) {}


    //REGISTER NEW USER AND PATIENT
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDto request, BindingResult result){
        Map<String, String> errors = com.maja.med_app.util.ValidationErrorUtils.mapErrors(result);
        if (userRepository.findByEmail(request.email()).isPresent()) {
            errors.put("email", "Email already taken");
        }
        if(!errors.isEmpty()){
            throw new AppValidationException(errors);
        }

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("PATIENT");

        //generate verification token and set enabled to false until user clicks the link in email
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setEnabled(false);
        userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setPesel(request.pesel());

        patientRepository.save(patient);

        //send verification email with token
        emailService.sendRegistrationEmail(patient.getUser().getEmail(), patient.getFirstName(), token);
        return ResponseEntity.ok("User and Patient registered successfuly");
    }

    //GET CURRENT USER DATA - for patient and doctor to view their profile data 
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Long patientId = null;
        Long doctorId = null;
        if ("PATIENT".equals(user.getRole())) {
            patientId = patientRepository.findByUser(user)
                    .map(Patient::getId) 
                    .orElse(null);
        } else if ("DOCTOR".equals(user.getRole())){
            doctorId = doctorRepository.findByUser(user)
                    .map(Doctor::getId)
                    .orElse(null);
        }
        //return user data along with patientId or doctorId to frontend to determine which profile data to fetch
        UserDto response = new UserDto(
            user.getId(),
            patientId,
            doctorId,
            user.getEmail(),
            user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    //PUT - change password by himself
    @PutMapping("/change-password")
    @Transactional
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDto request, BindingResult result) {
        Map<String, String> errors = ValidationErrorUtils.mapErrors(result);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if(!user.isEnabled()) {
            errors.put("account", "Account not activated. Please check your email for verification link.");
        }

        //check if old password matches
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            errors.put("oldPassword", "Incorrect current password");
        }
        if (!errors.isEmpty()) {
            throw new AppValidationException(errors);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }


    //GET - verify account by token from email link (new added method to activate account after registration)
    @GetMapping("/verify")
    public ResponseEntity<Void> verifyAccount(@RequestParam String token) {
        AppUser user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "http://localhost:4200/login?verified=true")
                .build(); 
    }

}
