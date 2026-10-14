import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';
import { SalaryHistoryComponent } from '../salary-history/salary-history.component';
import { SalaryEntryFormComponent } from '../salary-entry-form/salary-entry-form.component';

@Component({
  selector: 'app-salary-detail',
  template: `
    <div class="salary-detail">
      @if (employee) {
        <div class="employee-header">
          <h2>{{ employee.firstName }} {{ employee.lastName }}</h2>
          <p>{{ employee.empNo }} | {{ employee.dept }} | {{ employee.jobTitle }}</p>
          <p>{{ employee.country }} | {{ employee.getEmploymentStatusLabel() }}</p>
        </div>

        <!-- Tabs -->
        <div class="tabs">
          <button [class.active]="tab === 'salary'" (click)="tab = 'salary'">Salary History</button>
          <button [class.active]="tab === 'payroll'" (click)="tab = 'payroll'">Payroll Records</button>
        </div>

        <!-- Salary History Tab -->
        @if (tab === 'salary') {
          <app-salary-entry-form [employeeId]="employeeId" (saved)="onSalarySaved()"></app-salary-entry-form>
          <app-salary-history [employeeId]="employeeId"></app-salary-history>
        }

        <!-- Payroll Records Tab -->
        @if (tab === 'payroll') {
          <p class="section-title">Payroll Records</p>
          <table class="data-table">
            <thead><tr><th>Period</th><th>Gross Pay</th><th>Net Pay</th><th>Status</th></tr></thead>
            <tbody>
              @for (record of payroll; track record.id) {
                <tr>
                  <td>{{ record.periodStart }} to {{ record.periodEnd }}</td>
                  <td>${{ record.grossPay | number:'1.2-2' }}</td>
                  <td>${{ record.netPay | number:'1.2-2' }}</td>
                  <td><span class="badge">{{ record.payrollStatus }}</span></td>
                </tr>
              } @empty {
                <tr><td colspan="4" class="empty">No payroll records found</td></tr>
              }
            </tbody>
          </table>
        }
      } @else {
        <p>Loading employee...</p>
      }
    </div>
  `,
  styles: [`
    :host { display: block; padding: 20px; }
    .employee-header { margin-bottom: 16px; }
    h2 { margin-bottom: 4px; color: #212529; }
    p { margin: 2px 0; color: #6c757d; font-size: 14px; }
    .tabs { display: flex; gap: 0; border-bottom: 2px solid #dee2e6; margin-bottom: 16px; }
    .tabs button { padding: 8px 20px; background: none; border: none; cursor: pointer; font-size: 14px; border-bottom: 2px solid transparent; }
    .tabs button.active { border-bottom-color: #0d6efd; color: #0d6efd; font-weight: 500; }
    .section-title { font-size: 16px; font-weight: 500; margin: 16px 0 8px; color: #495057; }
    .data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
    .data-table th { padding: 12px 16px; text-align: left; background: #f8f9fa; font-weight: 600; color: #495057; }
    .data-table td { padding: 12px 16px; border-bottom: 1px solid #e9ecef; }
    .badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; background: #e7f5ff; color: #0d6efd; }
    .empty { text-align: center; color: #adb5bd; padding: 20px; }
  `]
})
export class SalaryDetailComponent implements OnInit {
  employeeId: number = 0;
  tab = 'salary';
  employee: any;
  payroll: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService
  ) {}

  ngOnInit(): void {
    this.employeeId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadEmployee();
  }

  loadEmployee(): void {
    this.employeeService.getById(this.employeeId).subscribe(emp => {
      this.employee = emp;
    });
  }

  onSalarySaved(): void {
    // Refresh salary history and KPI cards
  }
}
