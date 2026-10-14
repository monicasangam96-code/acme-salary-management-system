import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: '', canActivate: [AuthGuard], redirectTo: '/dashboard' },
  { path: 'dashboard', canActivate: [AuthGuard], loadComponent: () => import('./features/compensation-analytics/dashboard-overview/dashboard-overview.component').then(m => m.DashboardOverviewComponent) },
  { path: 'directory', canActivate: [AuthGuard], loadComponent: () => import('./features/employee-directory/employee-list/employee-list.component').then(m => m.EmployeeListComponent) },
  { path: 'reports', canActivate: [AuthGuard], pathMatch: 'full', redirectTo: '/reports/custom' },
  { path: 'reports/custom', canActivate: [AuthGuard], loadComponent: () => import('./features/reports/report-builder/report-builder.component').then(m => m.ReportBuilderComponent) },
  { path: 'salary/:id', canActivate: [AuthGuard], loadComponent: () => import('./features/salary-manager/salary-detail/salary-detail.component').then(m => m.SalaryDetailComponent) },
  { path: '**', redirectTo: '/dashboard' }
];
