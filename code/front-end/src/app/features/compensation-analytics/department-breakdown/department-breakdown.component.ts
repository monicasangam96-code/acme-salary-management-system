import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService, DepartmentBreakdown } from "../../../core/services/analytics.service";

@Component({
  standalone: true,
  selector: 'app-department-breakdown',
  templateUrl: './department-breakdown.component.html',
  imports: [CommonModule],
  styles: [`
    :host { display: block; }
    .chart-card { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    h3 { margin-bottom: 16px; font-size: 16px; color: #495057; }
    .bar-chart { display: flex; flex-direction: column; gap: 8px; }
    .bar-row { display: flex; align-items: center; gap: 8px; }
    .bar-label { width: 120px; font-size: 13px; color: #6c757d; text-align: right; flex-shrink: 0; }
    .bar-fill { height: 24px; border-radius: 4px; display: flex; align-items: center; padding: 0 8px; transition: width 0.3s ease; }
    .bar-value { font-size: 11px; color: white; font-weight: 600; white-space: nowrap; }
    .empty { text-align: center; color: #adb5bd; padding: 20px; }
  `]
})
export class DepartmentBreakdownComponent implements OnInit {
  data: DepartmentBreakdown[] = [];
  maxPayroll = 0;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.analyticsService.getByDepartment().subscribe(data => {
      this.data = data;
      this.maxPayroll = Math.max(...data.map(d => d.totalPayroll), 1);
    });
  }

  getBarPercent(value: number): number {
    return (value / this.maxPayroll) * 100;
  }
}
