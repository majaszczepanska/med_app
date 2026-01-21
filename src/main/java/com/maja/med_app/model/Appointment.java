package com.maja.med_app.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
//@Schema(example = "{\"visitTime\": null, \"patient\": {\"id\": 0}, \"doctor\": {\"id\": 0}}")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Future(message = "Visit time must be in the future")
    @NotNull(message = "Visit time is mandatory")
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
}
