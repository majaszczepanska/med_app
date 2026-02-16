import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, RouterModule, FormsModule],
  standalone: true,
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  updateData = {
    firstName: '',
    lastName: '',
    pesel: '',
    phoneNumber: '',
    address: '',
    disease: ''
  };

  constructor(private authService: AuthService, private router: Router, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.authService.getProfile().subscribe({
      next: (patient: any) => {
        this.updateData.firstName = patient.firstName || '';
        this.updateData.lastName = patient.lastName || '';
        this.updateData.pesel = patient.pesel || '';
        this.updateData.phoneNumber = patient.phoneNumber || '';
        this.updateData.address = patient.address || '';
        this.updateData.disease = patient.disease || '';

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Error while uploading profile data");
      }
    })
  }
  update() {
    this.authService.updateProfile(this.updateData).subscribe({
      next: (response) => {
        alert("Account successfuly updated ✅");
        //this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        alert("Error while updating data ❌: " + (err.error || "Check your data"));
        console.error(err);
      }
    })

  }
}

