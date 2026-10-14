import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

// Use relative path to go through Nginx on port 4200 — ensures cookies work
// across all requests (localhost:4200 ↔ localhost:8080 share the same origin)
const API_PATH = '/api/v1';

export interface AuthResponse {
  sessionToken: string;
  userId: number;
  userName: string;
  role: 'HR_MANAGER' | 'HR_ANALYST';
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser$: BehaviorSubject<AuthResponse | null> = new BehaviorSubject<AuthResponse | null>(null);
  private router = inject(Router);

  constructor(private http: HttpClient) {
    this.checkSession();
  }

  get currentUser() { return this.currentUser$.asObservable(); }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_PATH}/auth/login`, { email, password })
      .pipe(
        tap(res => this.currentUser$.next(res)),
        catchError(err => { throw err; })
      );
  }

  logout(): void {
    this.http.post(`${API_PATH}/auth/logout`, {}).subscribe(() => {
      this.currentUser$.next(null);
    });
  }

  checkSession(): void {
    this.http.get<{ id: number; name: string; role: string }>(`${API_PATH}/auth/me`)
      .subscribe({
        next: res => {
          const authRes: AuthResponse = {
            sessionToken: '',
            userId: res.id,
            userName: res.name,
            role: res.role as 'HR_MANAGER' | 'HR_ANALYST'
          };
          this.currentUser$.next(authRes);
        },
        error: () => this.currentUser$.next(null)
      });
  }

  getReturnUrl(): string | null {
    return new URLSearchParams(window.location.search).get('returnUrl');
  }

  isAuthenticated(): boolean {
    return this.currentUser$.value !== null;
  }

  isHRManager(): boolean {
    return this.currentUser$.value?.role === 'HR_MANAGER';
  }

  isAdmin(): boolean {
    return this.isHRManager();
  }
}
