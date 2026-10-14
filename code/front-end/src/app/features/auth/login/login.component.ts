import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-login',
  template: `
    <div class="login-container">
      <div class="login-card">
        <h1>ACME Salary Management</h1>
        <p class="subtitle">Sign in to your account</p>
        <form (ngSubmit)="onLogin()">
          <div class="form-group">
            <label for="email">Email</label>
            <input id="email" type="email" [(ngModel)]="email" name="email" required>
          </div>
          <div class="form-group">
            <label for="password">Password</label>
            <input id="password" type="password" [(ngModel)]="password" name="password" required>
          </div>
          @if (error) { <p class="error">{{ error }}</p> }
          <button type="submit" [disabled]="!email || !password" class="btn-login">Sign In</button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; background: #f8f9fa; }
    .login-card { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); width: 360px; }
    h1 { font-size: 22px; margin-bottom: 4px; color: #212529; }
    .subtitle { color: #6c757d; margin-bottom: 24px; }
    .form-group { margin-bottom: 16px; }
    label { display: block; font-size: 13px; font-weight: 500; color: #495057; margin-bottom: 4px; }
    input { width: 100%; padding: 10px 12px; border: 1px solid #ced4da; border-radius: 4px; font-size: 14px; box-sizing: border-box; }
    .error { color: #dc3545; font-size: 13px; margin: 12px 0; }
    .btn-login { width: 100%; padding: 10px; background: #0d6efd; color: white; border: none; border-radius: 4px; font-size: 15px; cursor: pointer; }
    .btn-login[disabled] { opacity: 0.5; cursor: not-allowed; }
  `]
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  onLogin(): void {
    this.authService.login(this.email, this.password).subscribe({
      next: () => window.location.href = '/dashboard',
      error: () => this.error = 'Invalid email or password'
    });
  }
}
