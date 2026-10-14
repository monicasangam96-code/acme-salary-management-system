import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService } from '../../core/services/analytics.service';
import { KpiCardComponent } from '../../../shared/kpi-card/kpi-card.component';
import { CountryBreakdownComponent } from '../country-breakdown/country-breakdown.component';
import { DepartmentBreakdownComponent } from '../department-breakdown/department-breakdown.component';

@Component({
  selector: 'app-dashboard-overview',
  template: `
    <div class="dashboard">
      <h2>Compensation Dashboard</h2>

      <!-- KPI Cards -->
      <div class="kpi-grid">
        <app-kpi-card [title]="'Total Annual Payroll'" [value]="summary.totalPayroll" color="#0d6efd"></app-kpi-card>
        <app-kpi-card [title]="'Average Salary'" [value]="summary.averageSalary" color="#198754"></app-kpi-card>
        <app-kpi-card [title]="'Median Salary'" [value]="summary.medianSalary" color="#fd7e14"></app-kpi-card>
        <app-kpi-card [title]="'Employees'" [value]="summary.employeeCount" color="#6f42c1"></app-kpi-card>
      </div>

      <!-- Charts -->
      <div class="charts-grid">
        <app-country-breakdown></app-country-breakdown>
        <app-department-breakdown></app-department-breakdown>
      </div>
    </div>
  `,
  styles: [`
    .dashboard { padding: 20px; }
    .kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .charts-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
    h2 { margin-bottom: 16px; }
  `]
})
export class DashboardOverviewComponent implements OnInit {
  summary = { totalPayroll: 0, averageSalary: 0, medianSalary: 0, employeeCount: 0 };

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.analyticsService.getSummary().subscribe(s => this.summary = s);
  }
}
