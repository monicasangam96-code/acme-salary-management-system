import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_PATH = '/api/v1';

export interface SalaryUpdateRequest {
  employeeId: number;
  salaryAmount: number;
  currencyCode: string;
  effectiveDate: string; // ISO-8601 date string (YYYY-MM-DD)
  notes?: string;
}

export interface SalarySummaryResponse {
  id: number;
  employeeId: number;
  salaryAmount: number;
  currencyCode: string;
  effectiveDate: string;
  notes?: string;
  updatedAt: string;
}

@Injectable({ providedIn: 'root' })
export class SalaryService {
  constructor(private http: HttpClient) {}

  /**
   * Create or update a salary record for an employee.
   * Backend maps this to SalaryUpdateRequest (record) and persists via SalaryService.updateSalary().
   */
  saveSalary(request: SalaryUpdateRequest): Observable<SalarySummaryResponse> {
    return this.http.post<SalarySummaryResponse>(`${API_PATH}/salaries`, request);
  }

  /**
   * Fetch salary history for an employee (list of historical salary records).
   */
  getSalaryHistory(employeeId: number): Observable<SalarySummaryResponse[]> {
    return this.http.get<SalarySummaryResponse[]>(`${API_PATH}/employees/${employeeId}/salary-history`);
  }
}
