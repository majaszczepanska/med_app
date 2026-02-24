package com.maja.med_app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.maja.med_app.model.AppUser;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.model.Specialization;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;
import com.maja.med_app.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                //CREATE ADMIN if there are none in db
                AppUser admin = new AppUser();
                admin.setEmail("admin@medapp.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                userRepository.save(admin);
            }

            //CREATE DOCTORS if there are none in db
            if (doctorRepository.count() == 0) {
                createDoctor("Gregory", "House", Specialization.GENERAL_PRACTITIONER, "house@medapp.com", userRepository, doctorRepository, passwordEncoder);
                createDoctor("Allison", "Cameron", Specialization.CARDIOLOGIST, "cameron@medapp.com", userRepository, doctorRepository, passwordEncoder);
                createDoctor("Robert", "Chase", Specialization.SURGEON, "chase@medapp.com", userRepository, doctorRepository, passwordEncoder);
                createDoctor("Lisa", "Cuddy", Specialization.NEUROLOGIST, "cuddy@medapp.com", userRepository, doctorRepository, passwordEncoder);
            }

            //CREATE PATIENTS if there are none in db
            if (patientRepository.count() == 0) {
                createPatient("John", "Smith", "80010112340", "Diabetes", null, "john.smith@test.com", userRepository, patientRepository, passwordEncoder);
                createPatient("Anna", "Johnson", "90020223457", "None", null, "anna.johnson@test.com", userRepository, patientRepository, passwordEncoder);
                createPatient("Peter", "Williams", "70030334567", "Hypertension", null, "peter.williams@test.com", userRepository, patientRepository, passwordEncoder);
                createPatient("Catherine", "Brown", "78041107891", "Penicillin allergy", null, "catherine.brown@test.com", userRepository, patientRepository, passwordEncoder);
                createPatient("Michael", "Davis", "60040445676", "Migraines", null, "michael.davis@test.com", userRepository, patientRepository, passwordEncoder); 
            }
        };
    }
    
    //CREATE DOCTOR and his user account
    private Doctor createDoctor(String firstName, String lastName, Specialization spec, String email, 
                                UserRepository userRepository, DoctorRepository doctorRepository, PasswordEncoder passwordEncoder) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("doctor123"));
        user.setRole("DOCTOR");
        user.setEnabled(true);
        userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setSpecialization(spec);
        doctor.setUser(user);
        return doctorRepository.save(doctor);
    }

    //CREATE PATIENT and his user account
    private void createPatient(String firstName, String lastName, String pesel, String disease, Doctor mainDoctor, 
                               String email, UserRepository userRepository, PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("patient123"));
        user.setRole("PATIENT");
        user.setEnabled(true);
        userRepository.save(user);

        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setPesel(pesel);
        patient.setDisease(disease);
        patient.setMainDoctor(mainDoctor);
        patient.setUser(user);
        patientRepository.save(patient);
    }
}