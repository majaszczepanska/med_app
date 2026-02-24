import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ErrorService } from '../services/error.service';
import { MatIconModule } from '@angular/material/icon';
import { NgForm } from '@angular/forms';

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

  registerSuccess = '';
  registerError = '';

  confirmPassword = '';
  showPass: boolean = false;
  showConfirmPass: boolean = false;
  isLoading: boolean = false;

  constructor(private authService: AuthService, private router: Router, private errorService: ErrorService, private cdr: ChangeDetectorRef) {}

  register(form: NgForm) {
    this.registerSuccess = '';
    this.registerError = '';
    this.isLoading = true;

    this.authService.register(this.registerData).subscribe({
      next: (response) => {
        //alert("Account successfuly created ✅ \nSign in");
        this.isLoading = false;
        this.registerSuccess = "Account created successfully! ✉️\nPlease check your inbox and click the verification link to activate your account.";
        form.resetForm();
        this.cdr.detectChanges();
        /*setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);*/
      },
      error: (err) => {
        this.isLoading = false;
        this.registerError = this.errorService.handleErrors(err);
        this.cdr.detectChanges();
      }
    })

  }

  clearMessages() {
    if (this.registerSuccess || this.registerError) {
      this.registerSuccess = '';
      this.registerError = '';
    }
  }
}
