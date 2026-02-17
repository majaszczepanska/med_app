package com.maja.med_app.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(type = "string", example = "2026-01-01 24:00")
    private LocalDateTime visitTime;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    @Schema(implementation = Object.class, example = "{\"id\": 0}")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @Schema(implementation = Object.class, example = "{\"id\": 0}")
    private Doctor doctor;


    @Column(length = 500)
    private String description;

    /* 
    @Column(columnDefinition = "boolean default false")
    private boolean deleted = false;
    */

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'SCHEDULED'")
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
}
