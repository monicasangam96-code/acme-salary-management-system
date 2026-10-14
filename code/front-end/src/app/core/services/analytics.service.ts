import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CountryBreakdown, DepartmentBreakdown, JobLevelBreakdown, AnalyticsSummary, CurrencyItem } from '../models/salary.model';

// Re-export these types so consumers can import them from this module
export type { CountryBreakdown, DepartmentBreakdown, JobLevelBreakdown, AnalyticsSummary, CurrencyItem };

const API_URL = 'http://localhost:8080/api/v1';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  constructor(private http: HttpClient) {}

  getSummary(country?: string, dept?: string, currency?: string): Observable<AnalyticsSummary> {
    let params = new HttpParams();
    if (country) params = params.set('country', country);
    if (dept) params = params.set('dept', dept);
    if (currency) params = params.set('currency', currency);
    return this.http.get<AnalyticsSummary>(`${API_URL}/analytics/summary`, { params });
  }

  getByCountry(currency?: string): Observable<CountryBreakdown[]> {
    let params = new HttpParams();
    if (currency) params = params.set('currency', currency);
    return this.http.get<CountryBreakdown[]>(`${API_URL}/analytics/by-country`, { params });
  }

  getByDepartment(country?: string, currency?: string): Observable<DepartmentBreakdown[]> {
    let params = new HttpParams();
    if (country) params = params.set('country', country);
    if (currency) params = params.set('currency', currency);
    return this.http.get<DepartmentBreakdown[]>(`${API_URL}/analytics/by-department`, { params });
  }

  getByJobLevel(country?: string, dept?: string, currency?: string): Observable<JobLevelBreakdown[]> {
    let params = new HttpParams();
    if (country) params = params.set('country', country);
    if (dept) params = params.set('dept', dept);
    if (currency) params = params.set('currency', currency);
    return this.http.get<JobLevelBreakdown[]>(`${API_URL}/analytics/by-job-level`, { params });
  }

  getCurrencies(): Observable<CurrencyItem[]> {
    return this.http.get<CurrencyItem[]>(`${API_URL}/analytics/by-currency`);
  }
}
