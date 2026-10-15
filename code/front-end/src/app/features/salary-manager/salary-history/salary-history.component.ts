import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeeService } from '../../../core/services/employee.service';

@Component({
  standalone: true,
  selector: 'app-salary-history',
  templateUrl: './salary-history.component.html',
  imports: [CommonModule],
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
export class SalaryHistoryComponent implements OnChanges {
  @Input() employeeId!: number;
  /** External data source — when provided, used instead of auto-fetching. */
  @Input() data: any[] | null = null;

  salaries: any[] = [];
  private _dataInputActive = false;

  constructor(private employeeService: EmployeeService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      const val = changes['data'].currentValue;
      this._dataInputActive = true;
      this.salaries = val || [];
    }
    // If no data input and employeeId changed, re-fetch
    if (!this._dataInputActive && changes['employeeId']) {
      this.fetch();
    }
  }

  ngOnInit(): void {
    // Auto-fetch only when there's no external data
    if (!this._dataInputActive) {
      this.fetch();
    }
  }

  fetch(): void {
    if (!this._dataInputActive) {
      this.employeeService.getSalaryHistory(this.employeeId).subscribe((s: any) => this.salaries = s);
    }
  }

  getCurrencySymbol(code: string): string {
    const map: Record<string, string> = { USD: '$', EUR: '€', GBP: '£', JPY: '¥' };
    return map[code] || code;
  }
}
