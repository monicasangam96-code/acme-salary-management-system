import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EmployeeService, PaginatedResponse } from './employee.service';
import { EmployeeDetail } from '../models/employee.model';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EmployeeService]
    });
    service = TestBed.inject(EmployeeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('list()', () => {
    it('should send GET request with default params', () => {
      const mockResponse: PaginatedResponse<EmployeeDetail> = {
        content: [], page: 0, size: 25, totalElements: 0, totalPages: 0
      };

      service.list().subscribe(res => {
        expect(res.content).toEqual([]);
      });

      const req = httpMock.expectOne((req) => req.url === 'http://localhost:8080/api/v1/employees');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('25');
      req.flush(mockResponse);
    });

    it('should include optional filters in params', () => {
      const mockResponse: PaginatedResponse<EmployeeDetail> = {
        content: [], page: 1, size: 10, totalElements: 5, totalPages: 1
      };

      service.list(1, 10, 'Smith', 'United States (US)', 'Engineering', 'ACTIVE')
        .subscribe();

      const req = httpMock.expectOne((req) => req.url === 'http://localhost:8080/api/v1/employees');
      expect(req.request.params.get('search')).toBe('Smith');
      expect(req.request.params.get('country')).toBe('United States (US)');
      expect(req.request.params.get('dept')).toBe('Engineering');
      expect(req.request.params.get('status')).toBe('ACTIVE');
      req.flush(mockResponse);
    });
  });

  describe('getById()', () => {
    it('should return employee detail for given id', () => {
      const mockEmployee: EmployeeDetail = {
        id: 1, empNo: 'EMP-001', firstName: 'John', lastName: 'Doe',
        email: 'john@acme.com', addressLine1: '123 Main St', city: 'NYC',
        zipCode: '10001', country: 'United States (US)', dept: 'Engineering',
        jobTitle: 'Engineer', employmentStatus: 'ACTIVE', startDate: '2024-01-01',
        active: true, createdBy: 1
      };

      service.getById(1).subscribe(emp => {
        expect(emp.empNo).toBe('EMP-001');
        expect(emp.firstName).toBe('John');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/v1/employees/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockEmployee);
    });
  });

  describe('create()', () => {
    it('should POST a new employee', () => {
      const newEmp = {
        empNo: 'EMP-002', firstName: 'Jane', lastName: 'Doe',
        email: 'jane@acme.com', addressLine1: '456 Oak Ave', city: 'LA',
        zipCode: '90001', country: 'United States (US)', dept: 'Sales',
        jobTitle: 'Sales Rep', employmentStatus: 'ACTIVE', startDate: '2024-06-01',
        active: true
      };

      service.create(newEmp as any).subscribe(emp => {
        expect(emp.empNo).toBe('EMP-002');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/v1/employees');
      expect(req.request.method).toBe('POST');
      req.flush({ ...newEmp, id: 2 });
    });
  });

  describe('delete()', () => {
    it('should send DELETE request and return void', () => {
      let deleted = false;
      service.delete(5).subscribe(() => { deleted = true; });

      const req = httpMock.expectOne('http://localhost:8080/api/v1/employees/5');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
      expect(deleted).toBe(true);
    });
  });
});
