import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { PatientService } from './patient.service';
import { DoctorService } from './doctor.service';
import { AppointmentService } from './appointment.service';
import { FormsModule } from '@angular/forms';
//import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})

export class AppComponent implements OnInit {

  activeTab: string = 'patients';

  patients: any[] = [];
  doctors: any[] = [];
  appointments: any[] = [];

  isEditing: boolean = false;
  currentPatientId: number | null = null;

  newPatient: any = {
    firstName: '',
    lastName: '',
    pesel: '',
    disease: '',
    mainDoctor: ''
  };

  newDoctor: any = {
    firstName: '',
    lastName: '',
    specialization: ''
  };

  newAppointment: any = {
    visitTime: null,
    patientId: null,
    doctorId: ''
  };



  constructor (
    private patientService: PatientService,
    private doctorService: DoctorService,
    private appointmentService: AppointmentService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit() {
    setInterval(() => {
      if (this.activeTab === 'patients') {
        this.refreshPatients();
      }
      if (this.activeTab === 'doctors') {
        this.refreshDoctors();
      }
      if (this.activeTab === 'appointments') {
        this.refreshAppointments();
      }
    }, 2000);
  }

  setActiveTab(tabName: string) {
    this.activeTab = tabName;
    this.isEditing = false;
    this.resetForm();
    this.refreshAll();
  }

  refreshAll() {
    this.refreshPatients();
    this.refreshDoctors();
    this.refreshAppointments();
  }

  refreshPatients() {
    this.patientService.getPatients().subscribe({
      next: (data:any) => {
        this.patients = data.sort((a:any, b:any) => a.id - b.id);
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error(err)
    });
  }

  refreshDoctors() {
    this.doctorService.getDoctors().subscribe({
      next: (data: any) => {
        this.doctors = data.sort((a:any, b:any) => a.id - b.id);
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error(err)
    });
  }

  refreshAppointments() {
    this.appointmentService.getAppointments().subscribe({
      next: (data: any) => {
        this.appointments = data.sort((a:any, b:any) => a.id - b.id);
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error(err)
    })
  }

  onSubmit() {
    if (this.activeTab === 'patients') {
      if(this.isEditing) {
        this.doUpdatePatient();
      } else {
        this.doCreatePatient();
      }
    } 
    else if (this.activeTab === 'doctors') {
      this.saveDoctor();
    }
    else if (this.activeTab === 'appointments') {
      this.saveAppointment();
    }
  }

  doCreatePatient() {
    const patientToSend = this.preparePatientData();
    if(!patientToSend) return;
    this.patientService.createPatient(patientToSend).subscribe({
      next: () => {
        alert("Patient added successfully");
        this.refreshPatients();
        this.resetForm();
      },
      error: (err: any) => {
        //alert("Error adding patient");
        this.handleErrors(err);
      }
    });
  }

  doUpdatePatient() {
    if(!this.currentPatientId) return;
    const patientToSend = this.preparePatientData();
    if(!patientToSend) return;
    this.patientService.updatePatient(this.currentPatientId, patientToSend).subscribe({
      next: () => {
        alert("Patient updated successfully");
        this.refreshPatients();
        this.resetForm();
      },
      error: (err: any) => {
        //alert("Error updating patient");
        this.handleErrors(err);
      }
    });
  }

  editPatient(patient:any) {
    this.isEditing = true;
    this.currentPatientId = patient.id;
    this.newPatient = {
      firstName: patient.firstName,
      lastName: patient.lastName,
      pesel: patient.pesel,
      disease: patient.disease,
      mainDoctor: patient.mainDoctor ? patient.mainDoctor.id : ''
    }
  }

  preparePatientData() {
    const patientData = { ...this.newPatient };
    if(patientData.mainDoctor && patientData.mainDoctor !== '') {
      const doctorId = Number(patientData.mainDoctor);
      if(!isNaN(doctorId) && doctorId > 0) {
        patientData.mainDoctor = {
          "id": doctorId
        }
      }
    } else {
      patientData.mainDoctor = null;
    }
    return patientData;
  }
  
  handleErrors(err: any) {
    console.error(err);
    let errorMessage = "";
    if (err.error && typeof err.error === 'object' && !err.error.message) {
      errorMessage = "❌ ERRORS:\n";
        for (const key in err.error) {
          if (err.error.hasOwnProperty(key)) {
            errorMessage += `\n• ${key.toUpperCase()}:\n`;
            const fullErrorText = err.error[key];
            let lines = fullErrorText.split(',');
            lines = lines
              .map((line: string) => line.trim())
              .sort((a: string, b: string) => b.localeCompare(a));
            lines.forEach((line: string) => {
              errorMessage += `    • ${line}\n`;
            });
          }
        }
    } 
    else if (typeof err.error === 'string') {
      errorMessage = "ERRORS IN FORM:\n";
      const lines = err.error.split(',');
      lines.forEach((line: string) => {
        errorMessage += `• ${line.trim()}\n`;
      });
    } 
    else {
      errorMessage += "An unknown error: " + (err.message || err);
    }
    
    alert(errorMessage);
  }

  removePatient(id: number) {
    if(confirm("Are you sure you want to delete this patient?")) {
      this.patientService.deletePatient(id).subscribe({
        next: () => this.refreshPatients(),
        error: (err: any) => console.error(err)
      });
    }
  }

  saveDoctor() {
    this.doctorService.createDoctor(this.newDoctor).subscribe({
      next: () => {
        alert("Doctor added successfully");
        this.refreshDoctors();
        this.resetForm();
      }
      ,
      error: (err: any) => {
        this.handleErrors(err);
        console.error(err);
      }
    });
  }

  removeDoctor(id: number) {
    if(confirm("Are you sure you want to delete this doctor?")) {
      this.doctorService.deleteDoctor(id).subscribe({
        next: () => this.refreshDoctors(),
        error: (err: any) => console.error(err)
      });
    }
  }

  saveAppointment() {
    // 1. Używamy funkcji pomocniczej do "spakowania" ID w obiekty
    const appointmentToSend = this.prepareAppointmentData();
    
    // Jeśli walidacja nie przeszła, przerywamy
    if (!appointmentToSend) return;

    this.appointmentService.createAppointment(appointmentToSend).subscribe({
      next: () => {
        alert("Appointment added successfully ✅");
        this.refreshAppointments();
        this.resetForm();
      },
      error: (err: any) => {
        this.handleErrors(err);
        console.error(err);
      }
    });
  }

  // --- NOWA FUNKCJA POMOCNICZA ---
  prepareAppointmentData() {
    const data = { ...this.newAppointment };
    
    // Konwersja patientId -> obiekt Patient { id: ... }
    if (data.patientId) {
       data.patient = { id: Number(data.patientId) };
       delete data.patientId; // Usuwamy stare pole, żeby nie mylić Javy
    } else {
       alert("Patient ID is required!");
       return null;
    }

    // Konwersja doctorId -> obiekt Doctor { id: ... }
    if (data.doctorId) {
       data.doctor = { id: Number(data.doctorId) };
       delete data.doctorId;
    } else {
       alert("Doctor ID is required!");
       return null;
    }

    // Opcjonalnie: Jeśli Java wymaga sekund, a input daje "YYYY-MM-DDTHH:MM"
    if (data.visitTime && data.visitTime.length === 16) {
        data.visitTime = data.visitTime + ":00";
    }

    return data;
  }

  /*
  removeAppointment(id: number) {
    if(confirm("Are you sure you want to delete this appointment?")) {
      this.appointmentService.deleteAppointment(id).subscribe({
        next: () => this.refreshAppointments(),
        error: (err: any) => console.error(err)
      });
    }
  }
    */


  cancelEdit() {
    this.resetForm();
  }

  resetForm() {
    this.newPatient = {firstName: '', lastName: '', pesel: '', disease: '', mainDoctor: ''};
    this.newDoctor = {firstName: '', lastName: '', specialization: ''};
    this.newAppointment = {visitTime: null, patientId: null, doctorId: ''};
    this.isEditing = false;
    this.currentPatientId = null;
  }



}
