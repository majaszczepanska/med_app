package com.maja.med_app.controller;

import java.util.List;
import java.util.Map;

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

//CONTROLLER LAYER
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
//@CrossOrigin (origins = "http://localhost:4200")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    //POST
    @PostMapping
    public Appointment addAppointment(@Valid @RequestBody Appointment appointment){
        return appointmentService.createAppointment(appointment);
    }

    //GET
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    //PUT
    @PutMapping("/{id}")
    public Appointment updateAppointment(@PathVariable Long id, @Valid @RequestBody Appointment updatedAppointment){
        return appointmentService.updateAppointment(id, updatedAppointment);
    }

    //PUT - for doctor to mark appointment as completed and add description
    @PutMapping("/{id}/complete")
    public void completeAppointment(@PathVariable Long id,@RequestBody Map<String, String> request) {
        appointmentService.completeAppointment(id, request.get("description"));
    }

    //DELETE
    @DeleteMapping("/{id}")
    public void deleteAppointment(@NonNull @PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }

    //GET - for doctor to view available slots 
    @GetMapping("/doctor/{doctorId}/available")
    public List<String> getAvailableSlots( @PathVariable Long doctorId, @RequestParam String date) {
       return appointmentService.getAvailableSlots(doctorId, date);
    }  

    //GET - for doctor to view patient history (list of past appointments with descriptions)
    @GetMapping("/patient/{patientId}")
    public List<Appointment> getPatientHistory(@PathVariable Long patientId) {
        return appointmentService.getAppointmentsByPatient(patientId);
    }
    
}
