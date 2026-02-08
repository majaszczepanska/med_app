import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { PatientService } from './patient.service';
import { DoctorService } from './doctor.service';
import { AppointmentService } from './appointment.service';
import { FormsModule } from '@angular/forms';
//import { RouterOutlet } from '@angular/router';
import {FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions, EventClickArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, FullCalendarModule, ],
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
  currentAppointmentId: number | null = null;
  currentDoctorId: number | null = null;

  showCalendar: boolean = true;

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

  minDate: string = '';


  //CALENDAR
  calendarOptions: CalendarOptions = {
    initialView: 'timeGridWeek',
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    weekends: false,
    slotMinTime: '08:00:00',
    slotMaxTime: '16:00:00',
    height: 'auto',
    headerToolbar: {
      left: 'prev, next today',
      center: 'title',
      right: 'dayGridMonth, timeGridWeek, timeGridDay'
    },
    events: [],
    dateClick: (arg) => this.handleDateClick(arg),
    eventClick: (arg) => this.handleEventClick(arg)
  };


  constructor (
    private patientService: PatientService,
    private doctorService: DoctorService,
    private appointmentService: AppointmentService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit() {
    this.updateMinDate();
    this.refreshAll();
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

  updateMinDate() {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    this.minDate = now.toISOString().slice(0, 16);
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
        this.appointments = data.sort((a:any, b:any) => (a.visitTime).localeCompare(b.visitTime));
        this.cdr.detectChanges();
        this.updateCalendarEvent();
      },
      error: (err: any) => console.error(err)
    })
  }

  updateCalendarEvent() {
    this.calendarOptions.events = this.appointments.map(a => {
      const startDate = new Date(a.visitDate);
      const endDate = new Date(startDate.getTime() + 15 * 60000);
      return {
        id: a.id.toString(),
        title: `${a.patient?.lastName} (${a.doctor?.lastName})`,
        start: a.visitTime,
        end: endDate.toISOString(),
        backgroundColor: this.isPastDate(a.visitTime) ? '#6c757d' : '#6f42c1', 
        borderColor: 'transparent'
      }

    })                               
  }

  handleDateClick(arg: any){
    if (this.isPastDate(arg.dateStr) && !this.isToday(arg.dateStr)) {
      alert("Cannot book inside the past");
      return;
    }

    this.newAppointment = {
      visitTime: arg.dateStr.slice(0, 16),
      patientId: null,
      doctorId: null
    };

    this.isEditing = false;
    this.activeTab = 'appointments';
    document.querySelector('.form-panel')?.scrollIntoView({behavior: 'smooth'});
  }
  handleEventClick(arg: EventClickArg) {
    const appointmentId = Number(arg.event.id);
    const appointment = this.appointments.find(a => a.id === appointmentId);

    if(appointment) {
      this.editAppointment(appointment);
      this.activeTab = 'appointments';
      document.querySelector('.form-panel')?.scrollIntoView({behavior: 'smooth'});
    }
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
    if (err.error && err.error.message) {
      alert("❌ ERROR: " + err.error.message);
      return;
    }
    if (err.error && typeof err.error === 'object') {
      let errorMessage = "❌ VALIDATION ERRORS:\n";
      let hasValidationErrors = false;

        for (const key in err.error) {
          if (err.error.hasOwnProperty(key)) {
            hasValidationErrors = true;
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
        if (hasValidationErrors) {
            alert(errorMessage);
            return;
        }
    } 
    if (typeof err.error === 'string') {
      alert("❌ ERROR: " + err.error);
      return;
    } 

    alert("❌ UNKNOWN ERROR (" + err.status + "): " + (err.statusText || "Server error"));
  }

  removePatient(id: number) {
    if(confirm("Are you sure you want to delete this patient?")) {
      this.patientService.deletePatient(id).subscribe({
        next: () => this.refreshPatients(),
        error: (err: any) => {
          this.handleErrors(err);
        }
      });
    }
  }

  saveDoctor() {
    if (this.isEditing && this.currentDoctorId){
       this.doctorService.updateDoctor(this.currentDoctorId, this.newDoctor).subscribe({
      next: () => {
        alert("Doctor updated successfully ✅");
        this.refreshDoctors();
        this.resetForm();
      }
      ,
      error: (err: any) => {
        this.handleErrors(err);
      }
    });
    } else {
      this.doctorService.createDoctor(this.newDoctor).subscribe({
        next: () => {
          alert("Doctor added successfully ✅");
          this.refreshDoctors();
          this.resetForm();
        }
        ,
        error: (err: any) => {
          this.handleErrors(err);
        }
      });
    }
  }

  
  editDoctor(doctor:any) {
    this.isEditing = true;
    this.currentDoctorId = doctor.id;
    this.newDoctor = {
      firstName: doctor.firstName,
      lastName: doctor.lastName,
      specialization: doctor.specialization
    }
  }
  

  removeDoctor(id: number) {
    if(confirm("Are you sure you want to delete this doctor?")) {
      this.doctorService.deleteDoctor(id).subscribe({
        next: () => {
          alert("Doctor deleted ✅");
          this.refreshDoctors();
        },
        error: (err: any) => {
          this.handleErrors(err);
        }
      });
    }
  }

  saveAppointment() {
    const appointmentToSend = this.prepareAppointmentData();
    if (!appointmentToSend) return;

    if(this.isEditing && this.currentAppointmentId) {
      this.appointmentService.updateAppointment(this.currentAppointmentId, appointmentToSend).subscribe({
        next: () => {
          alert("Appointment updated successfully ✅");
          this.refreshAppointments();
          this.resetForm();
        },
        error: (err: any) => {
         this.handleErrors(err);
        }
      });
      return;
    }
    this.appointmentService.createAppointment(appointmentToSend).subscribe({
      next: () => {
        alert("Appointment added successfully ✅");
        this.refreshAppointments();
        this.resetForm();
      },
      error: (err: any) => {
        this.handleErrors(err);
      }
    });
  }

  
  prepareAppointmentData() {
    const appointmentData = { ...this.newAppointment };

    if (appointmentData.patientId) {
      appointmentData.patient = { 
        id: Number(appointmentData.patientId) 
      };
      delete appointmentData.patientId; 
    } else {
       appointmentData.patient = null;
    }

    if (appointmentData.doctorId) {
       appointmentData.doctor = { id: Number(appointmentData.doctorId) };
       delete appointmentData.doctorId;
    } else {
       appointmentData.doctor = null;
    }
    if (appointmentData.visitTime) {
       appointmentData.visitTime = appointmentData.visitTime.replace('T', ' ');
    }

    return appointmentData;
  }

  formatDateDisplay(dateStr: string): string {
    if(!dateStr) {
      return '';
    }
    const dateT = dateStr.replace(' ', 'T');
    const date = new Date(dateT);
    if (isNaN(date.getTime())) {
      return dateStr; 
    }
    return date.toLocaleDateString('pl-PL', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

  }

  isPastDate(dateStr: string): boolean {
    if(!dateStr) {
      return false;
    }
    const dateT = dateStr.replace(' ', 'T');
    const date = new Date(dateT);
    const now = new Date();
    return date < now;
  }

  isToday(dateStr: string): boolean {
    if (!dateStr) {
      return false;
    }
    const visitDate = new Date(dateStr.replace(' ', 'T'));
    const today = new Date();
    return visitDate.toDateString() === today.toDateString();
  }

  editAppointment(appointment: any) {
    this.isEditing = true;
    this.currentAppointmentId = appointment.id;
    this.activeTab = 'appointments';
  
    let formatedDate = appointment.visitTime;
    if (formatedDate && formatedDate.includes(' ')) {
      formatedDate = formatedDate.replace(' ', 'T');
    }
    this.newAppointment = {
      visitTime: formatedDate,
      patientId: appointment.patient?.id || null,
      doctorId: appointment.doctor?.id || null
    };
  }
  
  removeAppointment(id: number) {
    if(confirm("Are you sure you want to delete this appointment?")) {
      this.appointmentService.deleteAppointment(id).subscribe({
        next: () => {
          alert("Appointment cancelled successfully ✅");
          this.refreshAppointments(); 
        },
        error: (err: any) => {
          console.error(err);
          if (err.error && err.error.message) {
             alert("❌ ERROR: \n " + err.error.message); 
          } else if (err.error && typeof err.error === 'string') {
             alert("❌ ERROR: \n" + err.error);
          } else {
             alert("❌ Cannot cancel this appointment (it might be too late).");
          }
        }
     });
    }
  }
  


  cancelEdit() {
    this.resetForm();
  }

  resetForm() {
    this.newPatient = {firstName: '', lastName: '', pesel: '', disease: '', mainDoctor: ''};
    this.newDoctor = {firstName: '', lastName: '', specialization: ''};
    this.newAppointment = {visitTime: '', patientId: null, doctorId: null};
    this.isEditing = false;
    this.currentPatientId = null;
    this.currentAppointmentId = null;
    this.currentDoctorId = null;
  }



}
