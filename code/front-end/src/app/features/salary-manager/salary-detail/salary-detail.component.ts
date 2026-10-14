import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { EmployeeService } from "../../../core/services/employee.service";
import { SalaryHistoryComponent } from "../salary-history/salary-history.component";
import { SalaryEntryFormComponent } from "../salary-entry-form/salary-entry-form.component";

@Component({
  standalone: true,
  selector: 'app-salary-detail',
  templateUrl: './salary-detail.component.html',
  imports: [CommonModule, SalaryHistoryComponent, SalaryEntryFormComponent],
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
    this.employeeService.getById(this.employeeId).subscribe((emp: any) => {
      this.employee = emp;
    });
  }

  onSalarySaved(): void {
    // Refresh salary history and KPI cards
  }
}
