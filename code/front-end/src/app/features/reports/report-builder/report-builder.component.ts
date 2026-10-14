import { Component, OnInit } from '@angular/core';
import { AnalyticsService, DepartmentBreakdown, CountryBreakdown } from '../../core/services/analytics.service';
import { ReportService } from '../../core/services/report.service';

@Component({
  selector: 'app-report-builder',
  template: `
    <div class="reports-page">
      <h2>Reports</h2>

      <!-- HR Question Templates -->
      <div class="template-section">
        <h3>HR Question Templates</h3>
        <select [(ngModel)]="selectedTemplate" (change)="onTemplateSelect()" class="template-select">
          <option value="">-- Select a question --</option>
          <option value="avg-by-country">What is the average salary in each country?</option>
          <option value="highest-departments">Which departments have the highest payroll?</option>
          <option value="level-distribution">How are salaries distributed by job level?</option>
        </select>
      </div>

      <!-- Results Table -->
      @if (selectedTemplate === 'avg-by-country') {
        <div class="results-table">
          <h3>Average Salary by Country</h3>
          <table class="data-table">
            <thead><tr><th>Country</th><th>Employee Count</th><th>Total Payroll</th></tr></thead>
            <tbody>
              @for (item of countryData; track item.country) {
                <tr><td>{{ item.country }}</td><td>{{ item.count }}</td><td>${{ (item.totalPayroll / 1000000).toFixed(2) }}M</td></tr>
              }
            </tbody>
          </table>
        </div>
      } @if (selectedTemplate === 'highest-departments') {
        <div class="results-table">
          <h3>Highest Payroll Departments</h3>
          <table class="data-table">
            <thead><tr><th>Department</th><th>Employee Count</th><th>Total Payroll</th></tr></thead>
            <tbody>
              @for (item of deptData; track item.department) {
                <tr><td>{{ item.department }}</td><td>{{ item.count }}</td><td>${{ (item.totalPayroll / 1000).toFixed(0) }}K</td></tr>
              }
            </tbody>
          </table>
        </div>
      }

      <!-- Export Button -->
      <div class="export-section">
        <button class="btn-export" (click)="onExport()">Download CSV Export</button>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; padding: 20px; }
    h2 { margin-bottom: 16px; }
    .template-section { margin-bottom: 24px; }
    .template-select { padding: 8px 12px; font-size: 14px; min-width: 300px; }
    .results-table { margin-bottom: 24px; background: white; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    h3 { margin-bottom: 12px; font-size: 16px; color: #495057; }
    .data-table { width: 100%; border-collapse: collapse; }
    .data-table th { padding: 8px 12px; text-align: left; border-bottom: 2px solid #dee2e6; font-weight: 600; color: #495057; }
    .data-table td { padding: 8px 12px; border-bottom: 1px solid #e9ecef; }
    .export-section { margin-top: 16px; }
    .btn-export { padding: 10px 20px; background: #198754; color: white; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; }
    .btn-export:hover { background: #157347; }
  `]
})
export class ReportBuilderComponent implements OnInit {
  selectedTemplate = '';
  countryData: CountryBreakdown[] = [];
  deptData: DepartmentBreakdown[] = [];

  constructor(
    private analyticsService: AnalyticsService,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {}

  onTemplateSelect(): void {
    if (this.selectedTemplate === 'avg-by-country') {
      this.analyticsService.getByCountry().subscribe(d => this.countryData = d);
    } else if (this.selectedTemplate === 'highest-departments') {
      this.analyticsService.getByDepartment().subscribe(d => this.deptData = d);
    }
  }

  onExport(): void {
    this.reportService.downloadCSV('employees');
  }
}
