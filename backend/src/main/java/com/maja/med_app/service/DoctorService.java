package com.maja.med_app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.maja.med_app.controller.AppointmentController;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.repository.AppointmentRepository;
import com.maja.med_app.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    //POST
    public Doctor createDoctor(Doctor doctor){
        return doctorRepository.save(doctor);
    }
    //GET
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAllByDeletedFalse();
    }
    //PUT
    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        Doctor existingDoctor = doctorRepository.findById(id).orElse(null);
        if (existingDoctor == null)  {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found");
        } else {
            existingDoctor.setFirstName(updatedDoctor.getFirstName());
            existingDoctor.setLastName(updatedDoctor.getLastName());
            existingDoctor.setSpecialization(updatedDoctor.getSpecialization());
        }
        return doctorRepository.save(existingDoctor);
    }
    //DELETE
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id).orElse(null);
        if (doctor == null)  {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found");
        } else {
            boolean hasFutureVisits = appointmentRepository.existsByDoctorIdAndVisitTimeAndDeletedFalse(id, LocalDateTime.now());
            if (hasFutureVisits) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete doctor with scheduled appointments");
            }
            doctor.setDeleted(true);
            doctorRepository.save(doctor);
        }
    }
}
