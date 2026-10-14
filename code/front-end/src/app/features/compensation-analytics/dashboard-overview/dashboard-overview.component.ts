import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService } from "../../../core/services/analytics.service";
import { KpiCardComponent } from "../../../shared/kpi-card/kpi-card.component";
import { CountryBreakdownComponent } from "../country-breakdown/country-breakdown.component";
import { DepartmentBreakdownComponent } from "../department-breakdown/department-breakdown.component";

@Component({
  standalone: true,
  selector: 'app-dashboard-overview',
  templateUrl: './dashboard-overview.component.html',
  imports: [CommonModule, KpiCardComponent, CountryBreakdownComponent, DepartmentBreakdownComponent],
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
