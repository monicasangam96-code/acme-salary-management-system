import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AnalyticsService, DepartmentBreakdown, CountryBreakdown, JobLevelBreakdown } from "../../../core/services/analytics.service";
import { ReportService } from "../../../core/services/report.service";

@Component({
  standalone: true,
  selector: 'app-report-builder',
  templateUrl: './report-builder.component.html',
  imports: [CommonModule, FormsModule],
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
  jobLevelData: JobLevelBreakdown[] = [];

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
    } else if (this.selectedTemplate === 'level-distribution') {
      this.analyticsService.getByJobLevel().subscribe(d => this.jobLevelData = d);
    }
  }

  onExport(): void {
    this.reportService.downloadCSV('employees');
  }
}
