import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AnalyticsService } from './analytics.service';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AnalyticsService]
    });
    service = TestBed.inject(AnalyticsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getSummary()', () => {
    it('should return analytics summary with default params', () => {
      const mockSummary = {
        totalEmployees: 100, averageSalary: 75000, medianSalary: 70000,
        maxSalary: 120000, minSalary: 45000, currencyCode: 'USD'
      };

      service.getSummary().subscribe(summary => {
        expect(summary.totalEmployees).toBe(100);
        expect(summary.averageSalary).toBe(75000);
      });

      const req = httpMock.expectOne('http://localhost:8080/api/v1/analytics/summary');
      expect(req.method).toBe('GET');
      req.flush(mockSummary);
    });

    it('should include filter params when provided', () => {
      const mockSummary = { totalEmployees: 10, averageSalary: 80000, medianSalary: 75000, maxSalary: 95000, minSalary: 60000, currencyCode: 'USD' };

      service.getSummary('US', 'Engineering').subscribe();
      const req = httpMock.expectOne((r) => r.url === 'http://localhost:8080/api/v1/analytics/summary');
      expect(req.params.get('country')).toBe('US');
      expect(req.params.get('dept')).toBe('Engineering');
      req.flush(mockSummary);
    });
  });

  describe('getByDepartment()', () => {
    it('should return department breakdown array', () => {
      const mockData = [
        { department: 'Engineering', totalPayroll: 500000, employeeCount: 30, averageSalary: 85000 },
        { department: 'Sales', totalPayroll: 300000, employeeCount: 20, averageSalary: 65000 }
      ];

      service.getByDepartment().subscribe(data => {
        expect(data.length).toBe(2);
        expect(data[0].department).toBe('Engineering');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/v1/analytics/by-department');
      expect(req.method).toBe('GET');
      req.flush(mockData);
    });
  });

  describe('getByCountry()', () => {
    it('should return country breakdown array', () => {
      const mockData = [
        { country: 'United States (US)', totalPayroll: 400000, employeeCount: 25, averageSalary: 95000, currencyCode: 'USD' },
        { country: 'United Kingdom (UK)', totalPayroll: 150000, employeeCount: 15, averageSalary: 62000, currencyCode: 'GBP' }
      ];

      service.getByCountry().subscribe(data => {
        expect(data.length).toBe(2);
        expect(data[0].country).toBe('United States (US)');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/v1/analytics/by-country');
      expect(req.method).toBe('GET');
      req.flush(mockData);
    });
  });

  describe('getByJobLevel()', () => {
    it('should return job level breakdown array', () => {
      const mockData = [
        { jobLevel: 'Junior', employeeCount: 10, averageSalary: 55000 },
        { jobLevel: 'Senior', employeeCount: 20, averageSalary: 95000 }
      ];

      service.getByJobLevel().subscribe(data => {
        expect(data.length).toBe(2);
        expect(data[0].jobLevel).toBe('Junior');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/v1/analytics/by-job-level');
      expect(req.method).toBe('GET');
      req.flush(mockData);
    });
  });

  describe('getCurrencies()', () => {
    it('should return currency list', () => {
      const mockCurrencies = [
        { code: 'USD', name: 'US Dollar', symbol: '$' },
        { code: 'EUR', name: 'Euro', symbol: '€' }
      ];

      service.getCurrencies().subscribe(currencies => {
        expect(currencies.length).toBe(2);
        expect(currencies[0].code).toBe('USD');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/v1/analytics/by-currency');
      expect(req.method).toBe('GET');
      req.flush(mockCurrencies);
    });
  });
});
