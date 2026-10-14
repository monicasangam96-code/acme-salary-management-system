import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Attach session token if available (stored in localStorage or cookie by Angular SSR)
  const token = localStorage.getItem('session_token');
  if (token) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(cloned);
  }

  // For API requests, clone with withCredentials to ensure cookies are sent
  if (req.url.includes('/api/')) {
    const cloned = req.clone({
      withCredentials: true
    });
    return next(cloned);
  }

  return next(req);
};
