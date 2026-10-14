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
  createdBy?: number;
}
