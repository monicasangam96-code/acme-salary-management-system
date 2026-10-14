import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/auth/auth.service';

@Component({
  standalone: true,
  selector: 'app-root',
  templateUrl: './app.component.html',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  styles: [`
    :host { display: block; height: 100vh; }
    .app-layout { display: flex; flex-direction: column; height: 100%; }
    .top-nav { background: #212529; color: white; padding: 0 24px; height: 56px; display: flex; align-items: center; justify-content: space-between; }
    .nav-brand { font-size: 18px; font-weight: 600; }
    .user-badge { background: #343a40; padding: 4px 12px; border-radius: 4px; font-size: 13px; color: #adb5bd; }
    .side-rail { width: 180px; background: #f8f9fa; border-right: 1px solid #dee2e6; padding: 16px 0; flex-shrink: 0; }
    .side-rail a { display: block; padding: 10px 24px; color: #495057; text-decoration: none; font-size: 14px; }
    .side-rail a:hover { background: #e9ecef; }
    .side-rail a.active { background: #e7f5ff; color: #0d6efd; border-right: 3px solid #0d6efd; font-weight: 500; }
    .logout-link { color: #dc3545 !important; margin-top: 16px; cursor: pointer; }
    .main-content { flex: 1; overflow: auto; background: #f8f9fa; }
  `]
})
export class AppComponent {
  constructor(public authService: AuthService) {}

  onLogout(): void {
    this.authService.logout();
    window.location.href = '/login';
  }
}
