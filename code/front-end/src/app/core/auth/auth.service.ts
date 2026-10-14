import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, Subject } from 'rxjs';
import { tap, catchError, first } from 'rxjs/operators';
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

  // Behaviorsubject starts as false — tells guard "not yet checked"
  private _sessionChecked$ = new BehaviorSubject<boolean>(false);
  // Tracks if we've ever had a valid user to prevent reset on failed checkSession
  private _hasValidUser = false;

  private router = inject(Router);

  constructor(private http: HttpClient) {
    this._restoreFromStorage();
    this.checkSession();
  }

  get currentUser(): Observable<AuthResponse | null> { return this.currentUser$.asObservable(); }

  /** Returns the current user value directly (not observable) */
  getCurrentValue(): AuthResponse | null {
    return this.currentUser$.value;
  }

  /** Returns whether session check has completed. BehaviorSubject always has a value. */
  isSessionCheckedNow(): boolean {
    return this._sessionChecked$.getValue();
  }

  get isSessionChecked(): Observable<boolean> { return this._sessionChecked$.asObservable(); }

  private _persistToStorage(): void {
    const user = this.currentUser$.value;
    if (user) {
      try { localStorage.setItem('acme_auth', JSON.stringify(user)); } catch {}
    }
  }

  private _restoreFromStorage(): void {
    try {
      const stored = localStorage.getItem('acme_auth');
      if (stored) {
        const user: AuthResponse = JSON.parse(stored);
        this.currentUser$.next(user);
        this._hasValidUser = true;
      }
    } catch {}
  }

  private _clearStorage(): void {
    try { localStorage.removeItem('acme_auth'); } catch {}
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_PATH}/auth/login`, { email, password })
      .pipe(
        tap(res => {
          this.currentUser$.next(res);
          this._hasValidUser = true;
          this._sessionChecked$.next(true);
          this._persistToStorage();
        }),
        catchError(err => { throw err; })
      );
  }

  logout(): void {
    this.http.post(`${API_PATH}/auth/logout`, {}).subscribe({
      next: () => this._clearState(),
      error: () => this._clearState()
    });
  }

  private _clearState(): void {
    this.currentUser$.next(null);
    this._hasValidUser = false;
    this._sessionChecked$.next(false);
    this._clearStorage();
  }

  checkSession(): void {
    // If we already have a valid user from login/storage, don't overwrite with failed session check
    if (this.currentUser$.value !== null && this._hasValidUser) {
      this._sessionChecked$.next(true);
      return;
    }

    this.http.get<{ id: number; name: string; role: string }>(`${API_PATH}/auth/me`)
      .subscribe({
        next: res => {
          const authRes: AuthResponse = {
            sessionToken: '',
            userId: res.id,
            userName: res.name,
            role: res.role as 'HR_MANAGER' | 'HR_ANALYST'
          };

          if (!this._hasValidUser) {
            this.currentUser$.next(authRes);
            this._hasValidUser = true;
            this._persistToStorage();
          }
          this._sessionChecked$.next(true);
        },
        error: () => {
          // Preserve existing user from storage — only set to null if no cached data exists
          const hasCachedUser = localStorage.getItem('acme_auth') !== null;
          if (!this._hasValidUser && !hasCachedUser) {
            this.currentUser$.next(null);
          } else if (hasCachedUser && !this.currentUser$.value) {
            try {
              const stored = JSON.parse(localStorage.getItem('acme_auth')!);
              this.currentUser$.next(stored);
              this._hasValidUser = true;
            } catch {}
          }
          this._sessionChecked$.next(true);
        }
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
