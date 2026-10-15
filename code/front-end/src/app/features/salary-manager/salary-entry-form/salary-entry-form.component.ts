import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../../core/services/notification.service';
import { SalaryService } from '../../../core/services/salary.service';

@Component({
  standalone: true,
  selector: 'app-salary-entry-form',
  templateUrl: './salary-entry-form.component.html',
  imports: [FormsModule],
  styles: [`
    :host { display: block; background: white; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-top: 16px; }
    h3 { font-size: 16px; margin-bottom: 12px; color: #495057; }
    .form-row { display: flex; gap: 16px; margin-bottom: 12px; }
    .form-group { flex: 1; }
    label { display: block; font-size: 13px; font-weight: 500; color: #495057; margin-bottom: 4px; }
    input, select, textarea { width: 100%; padding: 8px 12px; border: 1px solid #ced4da; border-radius: 4px; font-size: 14px; box-sizing: border-box; }
    .error-text { color: #dc3545; font-size: 12px; margin-top: 2px; display: block; }
    .btn-save { padding: 10px 20px; background: #0d6efd; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .btn-save[disabled] { opacity: 0.5; cursor: not-allowed; }
  `]
})
export class SalaryEntryFormComponent implements OnInit {
  @Input() employeeId!: number;
  @Output() saved = new EventEmitter<void>();

  salaryAmount: number | null = null;
  currencyCode = 'USD';
  effectiveDate = new Date().toISOString().split('T')[0];
  notes = '';
  submitted = false;
  saving = false;
  currencies = [
    { code: 'USD', symbol: '$' },
    { code: 'EUR', symbol: '€' },
    { code: 'GBP', symbol: '£' },
    { code: 'JPY', symbol: '¥' },
    { code: 'INR', symbol: '₹' },
  ];

  constructor(
    private notificationService: NotificationService,
    private salaryService: SalaryService
  ) {}

  ngOnInit(): void {
    // Default effective date to next month
    const nextMonth = new Date();
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    this.effectiveDate = nextMonth.toISOString().split('T')[0];
  }

  onSave(): void {
    this.submitted = true;
    if (!this.salaryAmount || !this.effectiveDate) return;

    this.saving = true;
    this.salaryService.saveSalary({
      employeeId: this.employeeId,
      salaryAmount: this.salaryAmount,
      currencyCode: this.currencyCode,
      effectiveDate: this.effectiveDate,
      notes: this.notes
    }).subscribe({
      next: () => {
        this.notificationService.success('Salary saved successfully');
        this.saved.emit();
      },
      error: (err) => {
        this.notificationService.error('Failed to save salary');
        console.error('Save salary failed:', err);
      },
      complete: () => { this.saving = false; }
    });
  }
}
