package com.maja.med_app.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    

    //without Lombok (@Data)  - public String getFirstName(){return firstName;} public void setFirstName(String firstName){ this.firstName=firstName;})
    private String firstName;
    private String lastName;
    private String specialization;

}