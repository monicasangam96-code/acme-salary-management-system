import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeeService } from '../../core/services/employee.service';

@Component({
  selector: 'app-salary-history',
  template: `
    <div class="salary-history">
      <h3>Salary History</h3>
      <table class="data-table">
        <thead><tr><th>Effective Date</th><th>Amount</th><th>Currency</th><th>Status</th><th>Previous</th></tr></thead>
        <tbody>
          @for (salary of salaries; track salary.id) {
            <tr>
              <td>{{ salary.effectiveDate }}</td>
              <td>{{ getCurrencySymbol(salary.currencyCode) }}{{ salary.salaryAmount | number:'1.2-2' }}</td>
              <td>{{ salary.currencyCode }}</td>
              <td><span class="badge" [class.current]="salary.status === 'CURRENT'">{{ salary.status }}</span></td>
              <td>{{ salary.previousSalaryAmount ? getCurrencySymbol(salary.currencyCode) + salary.previousSalaryAmount | number:'1.2-2' : '-' }}</td>
            </tr>
          } @empty {
            <tr><td colspan="5" class="empty">No salary history</td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    :host { display: block; margin-top: 16px; }
    h3 { font-size: 16px; margin-bottom: 8px; color: #495057; }
    .data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    .data-table th { padding: 10px 12px; text-align: left; background: #f8f9fa; font-weight: 600; color: #495057; border-bottom: 1px solid #dee2e6; }
    .data-table td { padding: 10px 12px; border-bottom: 1px solid #e9ecef; }
    .badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; background: #f1f3f5; color: #495057; }
    .badge.current { background: #d4edda; color: #155724; }
    .empty { text-align: center; color: #adb5bd; padding: 16px; }
  `]
})
export class SalaryHistoryComponent implements OnInit {
  @Input() employeeId!: number;
  salaries: any[] = [];

  constructor(private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.employeeService.getSalaryHistory(this.employeeId).subscribe(s => this.salaries = s);
  }

  getCurrencySymbol(code: string): string {
    const map: Record<string, string> = { USD: '$', EUR: '€', GBP: '£', JPY: '¥' };
    return map[code] || code;
  }
}
