import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username = '';
  password = '';
  errorMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onSubmit(): void {
    console.log('=== onSubmit called ===');
    console.log('Username:', this.username);
    console.log('Password length:', this.password?.length);

    if (!this.username || !this.password) {
      console.log('Validation failed: missing credentials');
      this.errorMessage = 'Username and password are required';
      this.cdr.detectChanges();
      return;
    }

    console.log('Starting login request...');
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        console.log('✅ Login successful', response);
        this.isLoading = false;
        this.cdr.detectChanges();
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('❌ Login error occurred');
        console.error('Error object:', err);
        console.error('Error status:', err.status);
        console.error('Error error:', err.error);
        console.error('Error message:', err.message);

        this.isLoading = false;

        // Handle different error types
        if (err.error?.error) {
          // Backend returned error message
          console.log('Using backend error message:', err.error.error);
          this.errorMessage = err.error.error;
        } else if (err.status === 401 || err.status === 400) {
          console.log('401/400 error - invalid credentials');
          this.errorMessage = 'Invalid username or password';
        } else if (err.status === 0) {
          console.log('Status 0 - cannot connect to server');
          this.errorMessage = 'Cannot connect to server. Please check if the backend is running.';
        } else {
          console.log('Generic error');
          this.errorMessage = `Error: ${err.message || 'An error occurred. Please try again.'}`;
        }

        console.log('Final error message:', this.errorMessage);
        this.cdr.detectChanges();
      },
      complete: () => {
        console.log('Login request completed');
      }
    });
  }
}
