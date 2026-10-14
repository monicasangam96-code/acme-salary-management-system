package com.acme.salarymgmt.service;

import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final EmployeeRepository employeeRepository;

    public ReportService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public void exportEmployeesCsv(OutputStream outputStream, String search, String country,
                                    String dept, String status, boolean showAll) throws IOException {
        StringBuilder csv = new StringBuilder();
        LocalDate now = LocalDate.now();

        // Header row
        csv.append("emp_no,first_name,last_name,email,phone,address_line1,city,state_province,zip_code,country,dept,job_title,employment_status,start_date,end_date,current_salary,currency,effective_date\n");

        // Resolve status to enum or null
        Employee.EmploymentStatus empStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                empStatus = Employee.EmploymentStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // ignore invalid status filter
            }
        }

        // Fetch all employees (paginate in batches for large datasets)
        int page = 0;
        int pageSize = 500;
        boolean hasMore = true;

        while (hasMore) {
            var pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);
            java.util.List<Employee> employees = employeeRepository.searchWithFilters(country, dept, empStatus, search, pageable).getContent();
            if (employees.isEmpty()) break;

            for (Employee emp : employees) {
                csv.append(escape(emp.getEmpNo())).append(",")
                   .append(escape(emp.getFirstName())).append(",")
                   .append(escape(emp.getLastName())).append(",")
                   .append(escape(emp.getEmail())).append(",")
                   .append(escape(emp.getPhone() != null ? emp.getPhone() : "")).append(",")
                   .append(escape(emp.getAddressLine1().replace(",", ";"))).append(",")
                   .append(escape(emp.getCity())).append(",")
                   .append(escape(emp.getStateProvince() != null ? emp.getStateProvince() : "")).append(",")
                   .append(escape(emp.getZipCode())).append(",")
                   .append(escape(emp.getCountry())).append(",")
                   .append(escape(emp.getDept())).append(",")
                   .append(escape(emp.getJobTitle())).append(",")
                   .append(escape(emp.getEmploymentStatus().toString())).append(",")
                   .append(now).append(",")
                   .append("").append(",")
                   .append("USD")
                   .append("\n");
            }

            if (employees.size() < pageSize) hasMore = false;
            page++;
        }

        outputStream.write(csv.toString().getBytes("UTF-8"));
    }

    private String escape(String value) {
        if (value == null || value.isBlank()) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
