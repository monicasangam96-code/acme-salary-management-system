import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Employee, EmployeeDetail } from '../models/employee.model';

const API_URL = 'http://localhost:8080/api/v1';

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  constructor(private http: HttpClient) {}

  list(page = 0, size = 25, search?: string, country?: string, dept?: string, status?: string): Observable<PaginatedResponse<EmployeeDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) params = params.set('search', search);
    if (country) params = params.set('country', country);
    if (dept) params = params.set('dept', dept);
    if (status) params = params.set('status', status);

    return this.http.get<PaginatedResponse<EmployeeDetail>>(`${API_URL}/employees`, { params });
  }

  getById(id: number): Observable<EmployeeDetail> {
    return this.http.get<EmployeeDetail>(`${API_URL}/employees/${id}`);
  }

  create(employee: Partial<Employee>): Observable<Employee> {
    return this.http.post<Employee>(`${API_URL}/employees`, employee);
  }

  update(id: number, employee: Partial<Employee>): Observable<EmployeeDetail> {
    return this.http.put<EmployeeDetail>(`${API_URL}/employees/${id}`, employee);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/employees/${id}`);
  }

  toggleStatus(id: number, status: string): Observable<void> {
    return this.http.patch<void>(`${API_URL}/employees/${id}/status?status=${status}`, {});
  }

  getSalaryHistory(employeeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/employees/${employeeId}/salary-history`);
  }
}
