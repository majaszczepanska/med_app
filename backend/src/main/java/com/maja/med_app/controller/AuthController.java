package com.maja.med_app.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.maja.med_app.model.AppUser;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.PatientRepository;
import com.maja.med_app.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public record UserDto(Long id, Long patientId, String email, String role) {}
    public record RegisterDto(String email, String password, String firstName, String lastName, String pesel) {}

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@RequestBody RegisterDto request){
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already taken");
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
