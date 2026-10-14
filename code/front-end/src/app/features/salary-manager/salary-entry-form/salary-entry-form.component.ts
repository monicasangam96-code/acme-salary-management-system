import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-salary-entry-form',
  template: `
    <div class="salary-form">
      <h3>New Salary Entry</h3>
      <form (ngSubmit)="onSave()" #formDir="ngForm" [class.invalid]="!formDir.valid && submitted">
        <div class="form-row">
          <div class="form-group">
            <label for="amount">Salary Amount *</label>
            <input id="amount" type="number" step="0.01" min="0.01" [(ngModel)]="salaryAmount" name="amount" required>
            @if (submitted && !salaryAmount) { <span class="error-text">Required</span> }
          </div>
          <div class="form-group">
            <label for="currency">Currency *</label>
            <select id="currency" [(ngModel)]="currencyCode" name="currency" required>
              @for (c of currencies; track c.code) { <option [value]="c.code">{{ c.code }} ({{ c.symbol }})</option> }
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="effectiveDate">Effective Date *</label>
            <input id="effectiveDate" type="date" [(ngModel)]="effectiveDate" name="effectiveDate" required>
            @if (submitted && !effectiveDate) { <span class="error-text">Required</span> }
          </div>
        </div>
        <div class="form-group">
          <label for="notes">Notes</label>
          <textarea id="notes" [(ngModel)]="notes" name="notes" rows="3"></textarea>
        </div>
        <button type="submit" [disabled]="!formDir.valid" class="btn-save">Save Salary</button>
      </form>
    </div>
  `,
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
  currencies = [
    { code: 'USD', symbol: '$' },
    { code: 'EUR', symbol: '€' },
    { code: 'GBP', symbol: '£' },
    { code: 'JPY', symbol: '¥' },
    { code: 'INR', symbol: '₹' },
  ];

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    // Default effective date to next month
    const nextMonth = new Date();
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    this.effectiveDate = nextMonth.toISOString().split('T')[0];
  }

  onSave(): void {
    this.submitted = true;
    if (!this.salaryAmount || !this.effectiveDate) return;

    // API call placeholder — in production, use SalaryService
    console.log('Saving salary:', { employeeId: this.employeeId, salaryAmount: this.salaryAmount, currencyCode: this.currencyCode, effectiveDate: this.effectiveDate });

    this.notificationService.success(`Salary updated for employee ${this.employeeId}`);
    this.saved.emit();
  }
}
