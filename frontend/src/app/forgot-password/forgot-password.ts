import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ErrorService } from '../services/error.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {
  email = '';
  successMessage = '';
  errorMessage = '';
  isLoading = false;
  
  constructor(private authService: AuthService, private errorService: ErrorService, private cdr: ChangeDetectorRef) {}

  submit() {
    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.forgotPassword({ email: this.email }).subscribe({
      next: (response: any) => {
        this.successMessage = response.message || 'Reset link sent (if email exists).';
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.errorMessage = this.errorService.handleErrors(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}