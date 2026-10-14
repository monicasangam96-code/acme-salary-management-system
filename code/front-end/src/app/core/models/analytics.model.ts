export interface AnalyticsSummary {
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
