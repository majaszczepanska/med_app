import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatIconModule } from '@angular/material/icon';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatIconModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})

export class LoginComponent {
  email = '';
  password = '';
  //errorMessage = '';

  loginSuccess = '';
  loginError = '';

  showPass: boolean = false;

  constructor(private authService: AuthService, private router: Router, private cdr: ChangeDetectorRef) {}

  login() {
    //this.errorMessage = '';
    this.loginSuccess = '';
    this.loginError = '';
    const credentials = btoa(this.email + ':' + this.password);
    
    this.authService.login(credentials).subscribe({
      next: (userData: any) => {
        sessionStorage.setItem('authData', 'Basic '+ credentials);
        sessionStorage.setItem('userRole', userData.role);
        sessionStorage.setItem('userId', userData.id);

        if (userData.patientId) {
          sessionStorage.setItem('patientId', userData.patientId)
        }
        if (userData.doctorId) {
          sessionStorage.setItem('doctorId', userData.doctorId)
        }
        sessionStorage.setItem('email', this.email)

        this.loginSuccess = "Logged in successfully! ✅ Redirecting...";
        this.cdr.detectChanges();
        setTimeout(() => {
          window.location.href = '/dashboard';
        }, 200);
      },
      error: () => {
        this.loginError = "❌ Invalid email or password";
        this.cdr.detectChanges();
      }
    });
  }

  clearMessages() {
    if (this.loginSuccess || this.loginError) {
      this.loginSuccess = '';
      this.loginError = '';
    }
  } 

}
