import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  // Empty path uses pathMatch: 'full' to ONLY match at exact root URL (/)
  // Without it, default 'prefix' mode would redirect everything to /dashboard
  { path: '', canActivate: [AuthGuard], redirectTo: '/dashboard', pathMatch: 'full' },

  // Login route - no guard needed (public)
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },

  // Dashboard route
  { path: 'dashboard', canActivate: [AuthGuard], loadComponent: () => import('./features/compensation-analytics/dashboard-overview/dashboard-overview.component').then(m => m.DashboardOverviewComponent) },

  // Directory route
  { path: 'directory', canActivate: [AuthGuard], loadComponent: () => import('./features/employee-directory/employee-list/employee-list.component').then(m => m.EmployeeListComponent) },

  // Reports routes
  { path: 'reports', canActivate: [AuthGuard], pathMatch: 'full', redirectTo: '/reports/custom' },
  { path: 'reports/custom', canActivate: [AuthGuard], loadComponent: () => import('./features/reports/report-builder/report-builder.component').then(m => m.ReportBuilderComponent) },

  // Salary detail route
  { path: 'salary/:id', canActivate: [AuthGuard], loadComponent: () => import('./features/salary-manager/salary-detail/salary-detail.component').then(m => m.SalaryDetailComponent) },

  // Fallback - should never match because we redirect to /dashboard explicitly
  { path: '**', redirectTo: '/dashboard' }
];
