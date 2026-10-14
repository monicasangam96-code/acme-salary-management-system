import { Injectable, inject } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';
import { Observable, tap, map, mergeMap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  private authService = inject(AuthService);
  private router = inject(Router);

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    const currentUser = this.authService.getCurrentValue();
    const sessionChecked = this.authService.isSessionCheckedNow();

    console.log('[AuthGuard] canActivate for', state.url, '- user:', currentUser?.userName || 'null', '- checked:', sessionChecked);

    // Already checked and no user — redirect to login with return URL
    if (sessionChecked && !currentUser) {
      console.log('[AuthGuard] BLOCKED: session checked but no user, redirecting');
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return new Observable(obs => obs.next(false));
    }

    // Already checked and user exists — allow
    if (sessionChecked && currentUser) {
      console.log('[AuthGuard] ALLOWED for', state.url, '- user:', currentUser.userName);
      return new Observable(obs => obs.next(true));
    }

    // Session not yet checked — wait for the HTTP check to complete
    console.log('[AuthGuard] session not checked, waiting...');
    return this.authService.isSessionChecked.pipe(
      tap(() => {
        // Trigger the session check if it hasn't been triggered yet
        const hasValidUser = this.authService.getCurrentValue();
        const alreadyChecked = this.authService.isSessionCheckedNow();
        if (!hasValidUser && !alreadyChecked) {
          this.authService.checkSession();
        }
      }),
      map(checked => {
        const user = this.authService.getCurrentValue();
        if (user) {
          console.log('[AuthGuard] ALLOWED for', state.url, '- user:', user.userName);
          return true;
        } else {
          console.log('[AuthGuard] BLOCKED after check, redirecting');
          this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
          return false;
        }
      })
    );
  }
}
