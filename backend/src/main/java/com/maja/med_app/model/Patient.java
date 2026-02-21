package com.maja.med_app.model;

import org.hibernate.validator.constraints.pl.PESEL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @NotBlank(message = "Required")
    @Size(min = 3, message= "Min. 3 characters")
    @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+(-[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+)?$", message = "Capital letter & letters only")
    @Schema(example = "string")
    private String firstName;

    @NotBlank(message = "Required")
    @Size(min = 3, message= "Min. 3 characters")
    @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+(-[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+)?$", message = "Capital letter & letters only")
    @Schema(example = "string")
    private String lastName;

    @Column(unique = true)
    //@Pattern(regexp = "^\\d{2}(?:0[1-9]|1[0-2]|2[1-9]|3[0-2]|4[1-9]|5[0-2]|6[1-9]|7[0-2]|8[1-9]|9[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{5}$", message = "PESEL is invalid")   
    @NotBlank(message = "Required")
    @PESEL(message = "Invalid PESEL format")
    @Schema(example = "12345678901")
    private String pesel;

    @Pattern(regexp = "^[0-9]{9}$", message = "Phone number must consist of exactly 9 digits")
    private String phoneNumber;
    private String address;

    private String disease;
    
    @JsonIgnoreProperties("patients")
    @ManyToOne
    @JoinColumn(name= "main_doctor_id")
    //@jakarta.validation.constraints.NotNull(message = "Required")
    @Schema(implementation = Object.class, example = "{\"id\": 0}")
    private Doctor mainDoctor;

    @Column(columnDefinition = "boolean default false")
    private boolean deleted = false;

}
