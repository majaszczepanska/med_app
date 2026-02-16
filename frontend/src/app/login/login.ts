import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})

export class LoginComponent {
  email = '';
  password = '';
  errorMessage = '';

  @Output() loginSuccess = new EventEmitter<void>();
  constructor(private http: HttpClient, private router: Router) {}

  login() {
    const credentials = btoa(this.email + ':' + this.password);
    const headers = new HttpHeaders({
      'Authorization': 'Basic ' + credentials
    });
    this.http.get('http://localhost:8080/auth/me', {headers}).subscribe({
      next: (userData: any) => {
        //alert("Login successful");
        sessionStorage.setItem('authData', 'Basic '+ credentials);
        sessionStorage.setItem('userRole', userData.role);
        sessionStorage.setItem('userId', userData.id);

        if (userData.patientId) {
          sessionStorage.setItem('patientId', userData.patientId)
        }
        this.loginSuccess.emit();
        //this.router.navigate(['/']);
        window.location.href = '/';
      },
      error: () => {
        this.errorMessage = "Invalid email or password";
      }
    });
  }

 

}
