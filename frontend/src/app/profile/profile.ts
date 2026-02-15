import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, RouterModule, FormsModule],
  standalone: true,
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  updateData = {
    phoneNumber: '',
    address: '',
    disease: ''
  };

  constructor(private authService: AuthService, private router: Router) {}

  update() {
    this.authService.updateProfile(this.updateData).subscribe({
      next: (response) => {
        alert("Account successfuly updated ✅");
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        alert("Error while updating data ❌: " + (err.error || "Check your data"));
        console.error(err);
      }
    })

  }
}

