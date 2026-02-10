import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Route, Router } from '@angular/router';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
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
    this.http.get('http://localhost:8080/doctors', {headers}).subscribe({
      next: () => {
        //alert("Login successful");
        sessionStorage.setItem('authData', 'Basic '+ credentials);
        this.loginSuccess.emit();
      },
      error: () => {
        this.errorMessage = "Invalid email or password";
      }
    });
  }

 

}
