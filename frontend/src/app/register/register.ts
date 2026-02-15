import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {
  registerData = {
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    pesel: ''
  };

  //constructor(private http: HttpClient, private router: Router) {}
  constructor(private authService: AuthService, private router: Router) {}

  register() {
    this.authService.register(this.registerData).subscribe({
      next: (response) => {
        alert("Account successfuly created ✅ \nSign in");
        this.router.navigate(['/login']);
      },
      error: (err) => {
        alert("Error while registering ❌: " + (err.error || "Check your data"));
        console.error(err);
      }
    })

  }
}
