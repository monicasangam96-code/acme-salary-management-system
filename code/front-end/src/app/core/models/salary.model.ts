export interface AnalyticsSummary {
  totalPayroll: number;
  averageSalary: number;
  medianSalary: number;
  employeeCount: number;
}

export interface Salary {
  id: number;
  employeeId: number;
  salaryAmount: number;
  currencyCode: string;
  effectiveDate: string;
  status: 'CURRENT' | 'SUPERSEDED' | 'PENDING';
  previousSalaryAmount?: number;
  notes?: string;
  createdAt?: string;
}

export interface SalarySummary {
  totalPayroll: number;
  averageSalary: number;
  medianSalary: number;
  employeeCount: number;
}

export interface CountryBreakdown {
  country: string;
  count: number;
  totalPayroll: number;
}

export interface DepartmentBreakdown {
  department: string;
  count: number;
  totalPayroll: number;
}

export interface JobLevelBreakdown {
  jobLevel: string;
  count: number;
  avgSalary: number;
  minSalary: number;
  maxSalary: number;
}

export interface CurrencyItem {
  code: string;
  name: string;
  symbol: string;
}
