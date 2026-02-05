package com.maja.med_app.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maja.med_app.model.Appointment;
import com.maja.med_app.service.AppointmentService;

import jakarta.validation.Valid;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
//@CrossOrigin (origins = "http://localhost:4200")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public Appointment addAppointment(@Valid @RequestBody Appointment appointment){
        return appointmentService.createAppointment(appointment);
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }


    @PutMapping("/{id}")
    public Appointment updateAppointment(@PathVariable Long id, @Valid @RequestBody Appointment updatedAppointment){
        return appointmentService.updateAppointment(id, updatedAppointment);
    }


    @DeleteMapping("/{id}")
    public void deleteAppointment(@NonNull @PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }

    @GetMapping("/doctor/{doctorId}/available")
    public List<String> getAvailableSlots( @PathVariable Long doctorId, @RequestParam String date) {
       return appointmentService.getAvailableSlots(doctorId, date);
    }  
    
}
