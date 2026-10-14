import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

const API_URL = 'http://localhost:8080/api/v1';

export interface AuthResponse {
  sessionToken: string;
  userId: number;
  userName: string;
  role: 'HR_MANAGER' | 'HR_ANALYST';
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser$: BehaviorSubject<AuthResponse | null> = new BehaviorSubject<AuthResponse | null>(null);

  constructor(private http: HttpClient) {
    this.checkSession();
  }

  get currentUser() { return this.currentUser$.asObservable(); }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, { email, password })
      .pipe(
        tap(res => this.currentUser$.next(res)),
        catchError(err => { throw err; })
      );
  }

  logout(): void {
    this.http.post(`${API_URL}/auth/logout`, {}).subscribe(() => {
      this.currentUser$.next(null);
    });
  }

  checkSession(): void {
    this.http.get<{ id: number; name: string; role: string }>(`${API_URL}/auth/me`)
      .subscribe({
        next: res => this.currentUser$.next({ sessionToken: '', userId: res.id, userName: res.name, role: res.role as 'HR_MANAGER' | 'HR_ANALYST' }),
        error: () => this.currentUser$.next(null)
      });
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
