import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

const API_URL = 'http://localhost:8080/api/v1';

@Injectable({ providedIn: 'root' })
export class ReportService {
  constructor(private http: HttpClient) {}

  downloadCSV(exportType: string, search?: string, country?: string, dept?: string): void {
    let params = new HttpParams()
      .set('export', exportType);
    if (search) params = params.set('search', search);
    if (country) params = params.set('country', country);
    if (dept) params = params.set('dept', dept);

    this.http.get(`${API_URL}/reports/csv`, { params, responseType: 'blob' })
      .subscribe({
        next: (blob: Blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `acme_${exportType}_export.csv`;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        },
        error: (err) => { throw err; }
      });
  }
}
