package com.maja.med_app.controller;

import org.springframework.security.core.Authentication;

import java.util.Map;

import org.hibernate.validator.constraints.pl.PESEL;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.model.AppUser;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.PatientRepository;
import com.maja.med_app.repository.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public record UserDto(Long id, Long patientId, String email, String role) {}
    public record RegisterDto(
        @NotBlank(message = "Required")
        @Email(message = "Invalid email format")
        String email, 

        @NotBlank(message = "Required")
        @Size(min = 6, message= "Min. 6 characters")
        String password, 

        @NotBlank(message = "Required")
        @Size(min = 3, message= "Min. 3 characters")
        @Pattern(regexp = "^[A-Z][a-z]+(-[A-Z][a-z]+)?$", message = "Capital letter & letters only")
        String firstName, 

        @NotBlank(message = "Required")
        @Size(min = 3, message= "Min. 3 characters")
        @Pattern(regexp = "^[A-Z][a-z]+(-[A-Z][a-z]+)?$", message = "Capital letter & letters only")
        String lastName, 

        @NotBlank(message = "Required")
        @PESEL(message = "Invalid PESEL format")
        String pesel
    ) {}

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
        userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setPesel(request.pesel());

        patientRepository.save(patient);
        return ResponseEntity.ok("User and Patient registered successfuly");
    }


    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        //user.setPassword(null);
        Long patientId = null;
        if ("PATIENT".equals(user.getRole())) {
            patientId = patientRepository.findByUser(user)
                    .map(Patient::getId) 
                    .orElse(null);
        }
        UserDto response = new UserDto(
            user.getId(),
            patientId,
            user.getEmail(),
            user.getRole()
        );

        return ResponseEntity.ok(response);
    }

}
