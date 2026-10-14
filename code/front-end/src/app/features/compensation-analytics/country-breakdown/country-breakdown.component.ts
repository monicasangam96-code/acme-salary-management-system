import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService, CountryBreakdown } from '../../core/services/analytics.service';

@Component({
  selector: 'app-country-breakdown',
  template: `
    <div class="chart-card">
      <h3>Payroll by Country</h3>
      <div class="bar-chart">
        @for (item of data; track item.country) {
          <div class="bar-row">
            <span class="bar-label">{{ item.country }}</span>
            <div class="bar-fill" [style.width.%]="getBarPercent(item.totalPayroll)">
              <span class="bar-value">${{ (item.totalPayroll / 1000000).toFixed(1) }}M</span>
            </div>
          </div>
        } @empty {
          <p class="empty">Loading...</p>
        }
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
    .chart-card { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    h3 { margin-bottom: 16px; font-size: 16px; color: #495057; }
    .bar-chart { display: flex; flex-direction: column; gap: 8px; }
    .bar-row { display: flex; align-items: center; gap: 8px; }
    .bar-label { width: 120px; font-size: 13px; color: #6c757d; text-align: right; flex-shrink: 0; }
    .bar-fill {
      height: 24px;
      background: linear-gradient(90deg, #0d6efd, #3d8bfd);
      border-radius: 4px;
      display: flex; align-items: center; padding: 0 8px;
      transition: width 0.3s ease;
    }
    .bar-value { font-size: 11px; color: white; font-weight: 600; white-space: nowrap; }
    .empty { text-align: center; color: #adb5bd; padding: 20px; }
  `]
})
export class CountryBreakdownComponent implements OnInit {
  data: CountryBreakdown[] = [];
  maxPayroll = 0;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.analyticsService.getByCountry().subscribe(data => {
      this.data = data;
      this.maxPayroll = Math.max(...data.map(d => d.totalPayroll), 1);
    });
  }

  getBarPercent(value: number): number {
    return (value / this.maxPayroll) * 100;
  }
}
