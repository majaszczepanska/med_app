import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { PatientService } from './patient.service';
import { FormsModule } from '@angular/forms';
import { first } from 'rxjs';
//import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  patients: any[] = [];

  newPatient: any = {
    firstName: '',
    lastName: '',
    pesel: '',
    disease: '',
    mainDoctor: ''
  };
  constructor (private patientService: PatientService){}

  ngOnInit() {
    this.refreshList();
  }

  refreshList() {
    this.patientService.getPatients().subscribe({
      next: (data:any) => this.patients = data,
      error: (err: any) => console.error(err)
    });
  }
  

  addPatient() {

    const patientToSend = { ...this.newPatient };

    if(this.newPatient.mainDoctor){
      patientToSend.mainDoctor = {
        "id": Number(this.newPatient.mainDoctor)
      };
    }else {
      patientToSend.mainDoctor = null;
    }

    this.patientService.createPatient(patientToSend).subscribe({
      next: () => {
        alert("Patient added successfully");
        this.refreshList();
        this.newPatient = {firstName: '', lastName: '', pesel: '', disease: '', mainDoctor: ''};
      },
      error: (err: any) => {
        alert("Error adding patient");
        console.error(err);
      }
    });
  }

  removePatient(id: number) {
    if(confirm("Are you sure you want to delete this patient?")) {
      this.patientService.deletePatient(id).subscribe({
        next: () => this.refreshList(),
        error: (err: any) => console.error(err)
      });
    }
  }
}
