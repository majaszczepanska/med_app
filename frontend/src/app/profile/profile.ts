import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ErrorService } from '../services/error.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, RouterModule, FormsModule, MatIconModule],
  standalone: true,
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  userRole = sessionStorage.getItem('userRole');
  myEmail = sessionStorage.getItem('email');
  myDoctorInfo: any = null;

  updateData = {
    firstName: '',
    lastName: '',
    pesel: '',
    phoneNumber: '',
    address: '',
    disease: '',
    mainDoctorId: null
  };
  
  passwordData = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  showOldPass: boolean = false;
  showNewPass: boolean = false;
  showConfPass: boolean = false;

  doctorsList: any[] = [];
  @Output() onError = new EventEmitter<any>();

  constructor(private authService: AuthService, private router: Router, private cdr: ChangeDetectorRef, private http: HttpClient, private errorService: ErrorService) {}

  ngOnInit() {
    this.http.get('http://192.168.131.213:8080/doctors').subscribe({
      next: (docs: any) => {
        this.doctorsList = docs;
        if (this.userRole === 'DOCTOR') {
          const myDocId = Number(sessionStorage.getItem('doctorId'));
          this.myDoctorInfo = this.doctorsList.find(d => d.id === myDocId);
        }
        if (this.userRole === 'PATIENT') {
          this.loadPatientProfile();
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Could not load doctors: " + err)
    });
    
  }

  loadPatientProfile() {
      this.authService.getProfile().subscribe({
        next: (patient: any) => {
          this.updateData.firstName = patient.firstName || '';
          this.updateData.lastName = patient.lastName || '';
          this.updateData.pesel = patient.pesel || '';
          this.updateData.phoneNumber = patient.phoneNumber || '';
          this.updateData.address = patient.address || '';
          this.updateData.disease = patient.disease || '';
          this.updateData.mainDoctorId = patient.mainDoctor ? patient.mainDoctor.id : null;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("Error while uploading profile data");
        }
      })
  }

  update() {
    this.authService.updateProfile(this.updateData).subscribe({
      next: () => {
        alert("Account successfuly updated ✅");
      },
      error: (err) => {
        this.onError.emit(err);
      }
    })
  }

  changePassword() {
    if (this.passwordData.newPassword !== this.passwordData.confirmPassword) {
      alert("❌ ERROR: Passwords do not match!");
      return;
    }

    const payload = {
      oldPassword: this.passwordData.oldPassword,
      newPassword: this.passwordData.newPassword
    };

    this.authService.changePassword(payload).subscribe({
      next: () => {
        alert("Password changed successfully! ✅ \nPlease log in again.");
        sessionStorage.clear();
        window.location.href = '/login'; 
      },
      error: (err) => {
        this.errorService.handleErrors(err);
      }
    });
  }
}

