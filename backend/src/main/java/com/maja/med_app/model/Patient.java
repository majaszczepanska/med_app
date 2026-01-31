package com.maja.med_app.model;

import org.hibernate.validator.constraints.pl.PESEL;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode= Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "First name is mandatory")
    @Size(min = 3, message= "First name must have at least 3 characters")
    @Pattern(regexp = "^[A-Z][a-z]+(-[A-Z][a-z]+)?$", message = "First name must start with a capital letter and contain only letters")
    @Schema(example = "string")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(min = 3, message= "Last name must have at least 3 characters")
    @Pattern(regexp = "^[A-Z][a-z]+(-[A-Z][a-z]+)?$", message = "Last name must start with a capital letter and contain only letters")
    @Schema(example = "string")
    private String lastName;

    @Column(unique = true)
    //@Pattern(regexp = "^\\d{2}(?:0[1-9]|1[0-2]|2[1-9]|3[0-2]|4[1-9]|5[0-2]|6[1-9]|7[0-2]|8[1-9]|9[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{5}$", message = "PESEL is invalid")   
    @PESEL(message = "PESEL is invalid")
    @Schema(example = "12345678901")
    private String pesel;

    private String disease;
    
    @ManyToOne
    @JoinColumn(name= "main_doctor_id")
    @Schema(implementation = Object.class, example = "{\"id\": 0}")
    private Doctor mainDoctor;

}
