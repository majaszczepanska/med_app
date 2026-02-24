import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ErrorService } from '../services/error.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatIconModule],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword {  
  token = '';
  newPassword = '';
  showPass = false;
  successMessage = '';
  errorMessage = '';
  isLoading = false;
  
  constructor(private route: ActivatedRoute, private authService: AuthService, private errorService: ErrorService, private router: Router) {  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      if(!this.token) {
        this.errorMessage = "❌ Invalid or missing token.";
      }
    });
  }

  submit() {
    if (!this.token) return;
    
    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.resetPassword({ token: this.token, newPassword: this.newPassword }).subscribe({
      next: (response: any) => {
        this.successMessage = 'Password reset successful! ✅ Redirecting to login...';
        this.isLoading = false;
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err: any) => {
        this.errorMessage = this.errorService.handleErrors(err);
        this.isLoading = false;
      }
    });
  }
}
