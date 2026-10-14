package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.EmployeeSummaryResponse;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;

    public ReportService(EmployeeRepository employeeRepository, SalaryRepository salaryRepository) {
        this.employeeRepository = employeeRepository;
        this.salaryRepository = salaryRepository;
    }

    @Transactional(readOnly = true)
    public void exportEmployeesCsv(OutputStream outputStream, String search, String country,
                                    String dept, String status, boolean showAll) throws IOException {
        StringBuilder csv = new StringBuilder();
        LocalDate now = LocalDate.now();

        // Header row
        csv.append("emp_no,first_name,last_name,email,phone,address_line1,city,state_province,zip_code,country,dept,job_title,employment_status,start_date,end_date,current_salary,currency,effective_date\n");

        // Fetch all employees (paginate in batches for large datasets)
        int page = 0;
        int pageSize = 500;
        boolean hasMore = true;

        while (hasMore) {
            var pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);
            var results = employeeRepository.searchWithFilters(country, dept, status != null ? status : null, search, pageable);
            if (!results.hasContent()) break;

            for (var empSummary : results.getContent()) {
                csv.append(empSummary.getEmpNo()).append(",")
                   .append(escape(empSummary.getFirstName())).append(",")
                   .append(escape(empSummary.getLastName())).append(",")
                   .append(escape(empSummary.getEmail())).append(",")
                   .append(escape(empSummary.getPhone() != null ? empSummary.getPhone() : "")).append(",")
                   .append(escape(empSummary.getAddressLine1().replaceAll(",", ";"))).append(",")
                   .append(escape(empSummary.getCity())).append(",")
                   .append(escape(empSummary.getStateProvince() != null ? empSummary.getStateProvince() : "")).append(",")
                   .append(escape(empSummary.getZipCode())).append(",")
                   .append(escape(empSummary.getCountry())).append(",")
                   .append(escape(empSummary.getDept())).append(",")
                   .append(escape(empSummary.getJobTitle())).append(",")
                   .append(escape(empSummary.getEmploymentStatus())).append(",")
                   .append(now).append(",")
                   .append(empSummary.getCurrentSalaryAmount() != null ? empSummary.getCurrentSalaryAmount().toString() : "")
                   .append(",").append(empSummary.getCurrentCurrencyCode() != null ? empSummary.getCurrentCurrencyCode() : "USD")
                   .append("\n");
            }

            if (results.getTotalPages() <= page + 1) hasMore = false;
            page++;
        }

        outputStream.write(csv.toString().getBytes("UTF-8"));
    }

    private String escape(String value) {
        if (value == null || value.isBlank()) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
