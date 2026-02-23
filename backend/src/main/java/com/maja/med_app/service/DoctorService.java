package com.maja.med_app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.maja.med_app.model.AppointmentStatus;
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
    //method to create new doctor (used by admin when creating doctor accounts)
    public Doctor createDoctor(Doctor doctor){
        return doctorRepository.save(doctor);
    }

    //GET
    //method to get all doctors (used by patient to view list of doctors)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAllByDeletedFalse();
    }

    //PUT
    //method to update doctor details (used by admin to update doctor profile)
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
    //method to delete doctor (used by admin to delete doctor accounts, soft delete by setting flag)
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id).orElse(null);
        if (doctor == null)  {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found");
        } else {
            boolean hasFutureVisits = appointmentRepository.existsByDoctorIdAndVisitTimeAfterAndStatusNot(id, LocalDateTime.now(), AppointmentStatus.CANCELLED);
            if (hasFutureVisits) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete doctor with scheduled appointments");
            }
            doctor.setDeleted(true);
            doctorRepository.save(doctor);
        }
    }
}
