import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { PatientService } from './patient.service';
import { DoctorService } from './doctor.service';
import { AppointmentService } from './appointment.service';
import { FormsModule } from '@angular/forms';
import {FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions, EventClickArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { HttpClient } from '@angular/common/http';
import enGb from '@fullcalendar/core/locales/en-gb';
import plLocale from '@fullcalendar/core/locales/pl';
import { RouterOutlet } from '@angular/router';
import { LoginComponent } from './login/login';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, FullCalendarModule, RouterOutlet, LoginComponent ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})

export class AppComponent implements OnInit {

  isLoggedIn: boolean = false;
  userRole: string = '';

  activeTab: string = 'dashboard';

  patients: any[] = [];
  doctors: any[] = [];
  appointments: any[] = [];

  isEditing: boolean = false;
  currentPatientId: number | null = null;
  currentAppointmentId: number | null = null;
  currentDoctorId: number | null = null;

  selectedDoctorId: number | null = null;

  searchText: string = '';

  showCalendar: boolean = true;

  patientHistory: any[] = [];
  selectedPatientForHistory: any = null;

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
    doctorId: '',
    description: ''
  };

  minDate: string = '';


  //CALENDAR
  calendarOptions: CalendarOptions = {
    initialView: 'timeGridWeek',
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    weekends: false,
    slotMinTime: '08:00:00',
    slotMaxTime: '16:00:00',

    expandRows: true,
    slotDuration: '00:15:00',
    slotLabelInterval: '01:00',
    allDaySlot: false,
    displayEventTime: false,

    locale: enGb,
    dayHeaderFormat: { weekday: 'short', day: '2-digit', month: 'numeric', omitCommas: true },

    height: 'auto',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    events: [],
    dateClick: (arg) => this.handleDateClick(arg),
    eventClick: (arg) => this.handleEventClick(arg)
  };


  constructor (
    private patientService: PatientService,
    private doctorService: DoctorService,
    private appointmentService: AppointmentService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient
  ){}


  //ON INIT (REFRESH TABS)
  ngOnInit() {
    if(sessionStorage.getItem('authData')) {
      this.isLoggedIn = true;
      this.userRole = sessionStorage.getItem('userRole') || '';
      this.initData();
    }
  }

  onLoginSuccess() {
    this.isLoggedIn = true;
    this.userRole = sessionStorage.getItem('userRole') || '';
    this.initData();
  }

  initData(){
    this.updateMinDate();
    this.refreshAll();
    setInterval(() => {
      if (this.isLoggedIn) {
        if (this.activeTab === 'patients') {
          this.refreshPatients();
        }
        if (this.activeTab === 'doctors') {
          this.refreshDoctors();
        }
        if (this.activeTab === 'appointments') {
          this.refreshAppointments();
        }
      } 
    }, 2000);
  }

  logout() {
    //sessionStorage.removeItem('authData');
    sessionStorage.clear()
    this.isLoggedIn = false;
    this.patients = [];
    this.doctors = [];
    this.appointments = [];
    this.selectedPatientForHistory = null; 
    this.activeTab = 'dashboard'; 
    window.location.reload();
  }


  //CALENDAR - disabled for past dates
  updateMinDate() {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    this.minDate = now.toISOString().slice(0, 16);
  }

  //ACTIVE TAB AND REFRESH
  setActiveTab(tabName: string) {
    this.activeTab = tabName;
    this.isEditing = false;
    this.searchText = '';

    if (tabName === 'history' && this.userRole === 'PATIENT') {
      this.loadMyHistory();
    } 
    else if (tabName === 'dashboard') {
      this.resetForm();
      this.refreshAll(); 
    }
  }

  //REFRESH
  refreshAll() {
    this.refreshPatients();
    this.refreshDoctors();
    this.refreshAppointments();
  }

  //REFRESH PATIENTS
  refreshPatients() {
    this.patientService.getPatients().subscribe({
      next: (data:any) => {
        this.patients = data.sort((a:any, b:any) => a.id - b.id);
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error(err)
    });
  }

  //REFRESH DOCTORS
  refreshDoctors() {
    this.doctorService.getDoctors().subscribe({
      next: (data: any) => {
        this.doctors = data.sort((a:any, b:any) => a.id - b.id);
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error(err)
    });
  }

  //REFRESH APPOINTMENTS
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

  //DASHBOARD
  get upcomingAppointments() {
    const now = new Date()
    return this.appointments.filter(a => !this.isPastDate(a.visitTime)).sort((a, b) => a.visitTime.localeCompare(b.visitTime)).slice(0,5);
  }

  //CALENDAR
  updateCalendarEvent() {
    let filetredAppointments = this.appointments;
    if (this.selectedDoctorId) {
      filetredAppointments = this.appointments.filter(a => a.doctor?.id === Number(this.selectedDoctorId));
    }

    this.calendarOptions.events = filetredAppointments.map(a => {
      const startDate = new Date(a.visitTime);
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

  onDoctorFilterChange(){
    this.updateCalendarEvent();
  }

  handleDateClick(arg: any){
    if (this.isPastDate(arg.dateStr) && !this.isToday(arg.dateStr)) {
      alert("Cannot book inside the past");
      return;
    }

    this.newAppointment = {
      visitTime: arg.dateStr.slice(0, 16),
      patientId: null,
      doctorId: null,
      description: ''
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


  //SUBMIT
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

  //ERRORS
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


  //PATIENTS 
  //FILTER
  get filteredPatients() {
  if (!this.searchText) {
    return this.patients; 
  }
  const lowerSearch = this.searchText.toLowerCase();
  return this.patients.filter(p => p.lastName.toLowerCase().includes(lowerSearch) || p.pesel.includes(lowerSearch));
}

  //CREATE
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

  //UPDATE
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

  //EDIT - for html form
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

  //PREPARE DATA (convert doctor id to object)
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
  
  //DELETE
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

  //HISTORY
  showPatientHistory(patient: any) {
    this.selectedPatientForHistory = patient;
    this.activeTab = 'history';
    this.patientHistory = [];

    this.appointmentService.getPatientHistory(patient.id).subscribe({
      next: (data) => {
        this.patientHistory = data.sort((a: any, b: any) => b.visitTime.localeCompare(a.visitTime));
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.handleErrors(err);
      }
    });
  }

  closeHistory() {
    this.selectedPatientForHistory = null;
    this.patientHistory = [];
    this.activeTab = 'patients';
  }

  //MY HISTORY
  loadMyHistory() {
  const myIdStr = sessionStorage.getItem('userId');

  if (myIdStr) {
    const myId = Number(myIdStr);

    this.appointmentService.getPatientDetails(myId).subscribe({
      next: (fullPatientData: any) => {
        this.showPatientHistory(fullPatientData);
      },
      error: (err: any) => {
        console.error("Error on loading patient data: ", err);
        const tstPatient = { id: myId, firstName: 'My', lastName: 'History' };
        this.showPatientHistory(tstPatient);
      }
    });
  }
}


  //DOCTOR
  //FILTER
  get filteredDoctors() {
  if (!this.searchText) {
    return this.doctors; 
  }
  const lowerSearch = this.searchText.toLowerCase();
  return this.doctors.filter(d => d.lastName.toLowerCase().includes(lowerSearch) || d.specialization.toLowerCase().includes(lowerSearch));
}

  //CREATE AND UPDATE
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

  //EDIT - for html form
  editDoctor(doctor:any) {
    this.isEditing = true;
    this.currentDoctorId = doctor.id;
    this.newDoctor = {
      firstName: doctor.firstName,
      lastName: doctor.lastName,
      specialization: doctor.specialization
    }
  }
  
  //DELETE DOCTOR
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


  //APOINTMENTS
  //FILTER
  get filetredAppointments() {
  if (!this.searchText) {
    return this.appointments; 
  }
  const lowerSearch = this.searchText.toLowerCase();
  return this.appointments.filter(a => a.patient.lastName.toLowerCase().includes(lowerSearch) || a.doctor.lastName.toLowerCase().includes(lowerSearch));
}

  //CREATE AND UPDATE 
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

  //PREPARE DATA (convert patient and doctor id to objects )
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

  //DATE in normal format for user
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

  //DATE - check if date is in past
  isPastDate(dateStr: string): boolean {
    if(!dateStr) {
      return false;
    }
    const dateT = dateStr.replace(' ', 'T');
    const date = new Date(dateT);
    const now = new Date();
    return date < now;
  }

  //DATE - check if date is today
  isToday(dateStr: string): boolean {
    if (!dateStr) {
      return false;
    }
    const visitDate = new Date(dateStr.replace(' ', 'T'));
    const today = new Date();
    return visitDate.toDateString() === today.toDateString();
  }

  //EDIT - for html to form
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
      doctorId: appointment.doctor?.id || null,
      description:appointment.description || '',
    };
  }
  
  //DELETE
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
  

  //CANCEL BUTTON
  cancelEdit() {
    this.resetForm();
  }

  //RESET FORM
  resetForm() {
    this.newPatient = {firstName: '', lastName: '', pesel: '', disease: '', mainDoctor: ''};
    this.newDoctor = {firstName: '', lastName: '', specialization: ''};
    this.newAppointment = {visitTime: '', patientId: null, doctorId: null, description: ''};
    this.isEditing = false;
    this.currentPatientId = null;
    this.currentAppointmentId = null;
    this.currentDoctorId = null;
  }



}
