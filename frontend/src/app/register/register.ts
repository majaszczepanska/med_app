import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ErrorService } from '../services/error.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatIconModule],
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

  showPass: boolean = false;
  constructor(private authService: AuthService, private router: Router, private errorService: ErrorService) {}

  register() {
    this.authService.register(this.registerData).subscribe({
      next: (response) => {
        alert("Account successfuly created ✅ \nSign in");
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.errorService.handleErrors(err);
      }
    })

  }
}
