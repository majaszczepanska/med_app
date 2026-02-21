package com.maja.med_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // table in database
@Data // getters and setters and toString - Lombok 
@NoArgsConstructor //create an empty doctor, Doctor doctor = new Doctor()
@AllArgsConstructor //create a new not empty doctor, Doctor doctor = new Doctor(null, "name", "lname"..)
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto generated id
    @Schema(accessMode=Schema.AccessMode.READ_ONLY) //do not show this in post
    private Long id;
    
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    //without Lombok (@Data)  - public String getFirstName(){return firstName;} public void setFirstName(String firstName){ this.firstName=firstName;})

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

    private String specialization;

    @Column(columnDefinition = "boolean default false")
    private boolean deleted = false;

}