import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {
  user = {
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    role: 'PATIENT' 
  };

  constructor(private http: HttpClient, private router: Router) {}

  register() {
    this.http.post('http://localhost:8080/auth/register', this.user, { responseType: 'text' }).subscribe({
      next: (response) => {
        alert("Account successfuly created ✅ Sign in");
        this.router.navigate(['/login']);
      },
      error: (err) => {
        alert("Error while registering ❌: " + (err.error || "Check your data"));
        console.error(err);
      }
    })

  }
}
