import { Injectable, inject } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';
import { Observable, tap, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  private authService = inject(AuthService);
  private router = inject(Router);

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    // Get the current values directly from BehaviorSubjects (synchronous)
    const currentUser = this.authService.getCurrentValue();
    const sessionChecked = this.authService.isSessionCheckedNow();

    console.log('[AuthGuard] canActivate for', state.url, '- user:', currentUser?.userName || 'null', '- checked:', sessionChecked);

    if (sessionChecked && !currentUser) {
      // Session check completed and no valid user — redirect to login
      console.log('[AuthGuard] BLOCKED: session checked but no user, redirecting to /dashboard');
      this.router.navigate(['/dashboard'], { queryParams: { returnUrl: state.url } });
      return of(false);
    }

    if (!sessionChecked) {
      // Session check not yet complete — trigger it and allow navigation
      console.log('[AuthGuard] triggering session check...');
      this.authService.checkSession();
      // Allow navigation; the session will be checked asynchronously
      return of(true);
    }

    // Session checked and user exists — allow
    console.log('[AuthGuard] ALLOWED for', state.url, '- user:', currentUser?.userName || 'null');
    return of(!!currentUser);
  }
}
