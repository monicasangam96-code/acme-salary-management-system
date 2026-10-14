import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { debounceTime, distinctUntilChanged, Subject, Subscription } from 'rxjs';
import { EmployeeService, PaginatedResponse } from "../../../core/services/employee.service";
import { Employee } from "../../../core/models/employee.model";
import { NotificationService } from "../../../core/services/notification.service";

@Component({
  standalone: true,
  selector: 'app-employee-list',
  templateUrl: './employee-list.component.html',
  imports: [CommonModule, FormsModule],
})
export class EmployeeListComponent implements OnInit, OnDestroy {
  employees: Employee[] = [];
  totalPages = 0;
  currentPage = 0;
  totalElements = 0;
  pageSize = 25;

  searchQuery = '';
  countryFilter = '';
  deptFilter = '';
  statusFilter = 'ACTIVE';

  private searchSubject = new Subject<string>();
  private searchSubscription: Subscription | null = null;

  countries = ['United States (US)', 'United Kingdom (UK)', 'Germany (DE)', 'Japan (JP)', 'France (FR)', 'India (IN)', 'Canada (CA)', 'Australia (AU)', 'Singapore (SG)'];
  departments = ['Engineering', 'Sales', 'Marketing', 'HR', 'Finance', 'Operations', 'Legal', 'Other'];

  currencySymbols: Record<string, string> = {
    USD: '$', EUR: '€', GBP: '£', JPY: '¥', INR: '₹',
    CAD: 'C$', AUD: 'A$', SGD: 'S$', KRW: '₩'
  };

  constructor(
    private employeeService: EmployeeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadEmployees();

    // Debounced search with debounceTime operator
    this.searchSubscription = this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => {
      this.currentPage = 0;
      this.loadEmployees();
    });
  }

  loadEmployees(): void {
    const page = this.currentPage || 0;
    this.employeeService.list(page, this.pageSize, this.searchQuery, this.countryFilter, this.deptFilter, this.statusFilter)
      .subscribe({
        next: (res: PaginatedResponse<Employee>) => {
          this.employees = res.content;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
        },
        error: (err) => {
          this.notificationService.error('Failed to load employees');
        }
      });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadEmployees();
  }

  onSearch(query: string): void {
    this.searchSubject.next(query);
  }

  onDeleteEmployee(id: number, empNo: string): void {
    if (confirm(`Are you sure you want to remove ${empNo}?`)) {
      this.employeeService.delete(id).subscribe({
        next: () => {
          this.loadEmployees();
          this.notificationService.success(`${empNo} removed`);
        },
        error: () => this.notificationService.error('Failed to delete employee')
      });
    }
  }

  getCurrencySymbol(currencyCode: string): string {
    return this.currencySymbols[currencyCode] || currencyCode;
  }

  formatSalary(amount: number | null, currency: string | null): string {
    if (!amount) return 'N/A';
    const symbol = this.getCurrencySymbol(currency || 'USD');
    return `${symbol}${amount.toLocaleString(undefined, { minimumFractionDigits: 2 })}`;
  }

  addEmployee(): void {
    // Navigate to salary manager to add a new employee
    window.location.href = '/salary/new';
  }

  ngOnDestroy(): void {
    if (this.searchSubscription) this.searchSubscription.unsubscribe();
  }
}
