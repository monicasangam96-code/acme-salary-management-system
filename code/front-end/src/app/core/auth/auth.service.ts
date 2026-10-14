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

  // Tracks whether checkSession() has completed its initial check — starts as false
  private sessionChecked$ = new Subject<boolean>();
  // Use BehaviorSubject with initial value so AuthGuard can always get a value
  private _sessionChecked$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  // Track if we've ever had a valid user to prevent reset by failed checkSession calls
  private _hasValidUser = false;

  private router = inject(Router);

  constructor(private http: HttpClient) {
    // Restore cached auth from sessionStorage (persists across page reloads)
    this._restoreFromStorage();
    this.checkSession();
  }

  get currentUser(): Observable<AuthResponse | null> { return this.currentUser$.asObservable(); }

  /** Returns the current user value directly (not observable) */
  getCurrentValue(): AuthResponse | null {
    return this.currentUser$.value;
  }

  /** Returns whether session check has completed */
  isSessionCheckedNow(): boolean {
    return this._sessionChecked$.value;
  }

  /** Returns whether the session check has completed. Used by AuthGuard. */
  get isSessionChecked(): Observable<boolean> {
    return this._sessionChecked$.asObservable();
  }

  /** Persist user to localStorage (survives page reloads more reliably) */
  private _persistToStorage(): void {
    const user = this.currentUser$.value;
    if (user) {
      try {
        localStorage.setItem('acme_auth', JSON.stringify(user));
      } catch {}
    }
  }

  /** Restore user from localStorage */
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

  /** Clear persisted auth */
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
      next: () => {
        this.currentUser$.next(null);
        this._hasValidUser = false;
        this._sessionChecked$.next(false);
        this._clearStorage();
      },
      error: () => {
        // Still clear local state even if server call fails
        this.currentUser$.next(null);
        this._hasValidUser = false;
        this._sessionChecked$.next(false);
        this._clearStorage();
      }
    });
  }

  checkSession(): void {
    // If we already have a valid user from login/storage, don't overwrite with failed session check
    if (this.currentUser$.value !== null && this._hasValidUser) {
      // Session is already established from login — ensure checked flag is set
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

          // Only set user if we haven't already had a valid user from login/storage
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
            // Restore from storage on error since we have cached data but no currentUser
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
