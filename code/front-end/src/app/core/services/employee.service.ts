import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_PATH = '/api/v1';

export interface Employee {
  id: number;
  empNo: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  addressLine1: string;
  city: string;
  stateProvince?: string;
  zipCode: string;
  country: string;
  dept: string;
  jobTitle: string;
  employmentStatus: 'ACTIVE' | 'TERMINATED' | 'SUSPENDED';
  startDate: string;
  endDate?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
  currentSalaryAmount?: number;
  currentCurrencyCode?: string;
  effectiveDate?: string;
}

export interface EmployeeDetail extends Employee {
  previousSalary?: number;
  previousCurrency?: string;
}

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

  list(page = 0, size = 25, search = '', country = '', dept = '', status = ''): Observable<PaginatedResponse<EmployeeDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) params = params.set('search', search);
    if (country) params = params.set('country', country);
    if (dept) params = params.set('dept', dept);
    if (status) params = params.set('status', status);

    return this.http.get<PaginatedResponse<EmployeeDetail>>(`${API_PATH}/employees`, { params });
  }

  getById(id: number): Observable<EmployeeDetail> {
    return this.http.get<EmployeeDetail>(`${API_PATH}/employees/${id}`);
  }

  create(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(`${API_PATH}/employees`, employee);
  }

  update(id: number, employee: Employee): Observable<EmployeeDetail> {
    return this.http.put<EmployeeDetail>(`${API_PATH}/employees/${id}`, employee);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_PATH}/employees/${id}`);
  }

  toggleStatus(id: number, status: 'ACTIVE' | 'TERMINATED'): Observable<void> {
    return this.http.patch<void>(`${API_PATH}/employees/${id}/status?status=${status}`, {});
  }

  getSalaryHistory(employeeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${API_PATH}/employees/${employeeId}/salary-history`);
  }
}
