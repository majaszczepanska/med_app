package com.maja.med_app.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
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
@Schema(example = "{\"firstName\": \"string\", \"lastName\":\"string\", \"disease\":\"string\", \"doctor\": {\"id\": 0}}")

public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode= Schema.AccessMode.READ_ONLY)
    private Long id;

    private String firstName;
    private String lastName;
    private String disease;
    
    @ManyToOne
    @JoinColumn(name= "main_doctor_id")
    private Doctor doctor;

}
