package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.EmployeeDetailResponse;
import com.acme.salarymgmt.dto.EmployeeSummaryResponse;
import com.acme.salarymgmt.model.*;
import com.acme.salarymgmt.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;
    private final ValidationService validationService;
    private final AuditLogService auditLogService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           SalaryRepository salaryRepository,
                           ValidationService validationService,
                           AuditLogService auditLogService) {
        this.employeeRepository = employeeRepository;
        this.salaryRepository = salaryRepository;
        this.validationService = validationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeSummaryResponse> listActive(String search, String country, String dept,
                                                      Employee.EmploymentStatus status, Pageable pageable) {
        String searchLike = search != null ? "%" + search.toLowerCase() + "%" : null;
        var page = employeeRepository.searchWithFilters(country, dept, status, searchLike, pageable);

        return page.map(emp -> {
            BigDecimal salaryAmount = null;
            String currencyCode = null;
            var currentSalary = salaryRepository.findCurrentByEmployeeId(emp.getId());
            if (currentSalary.isPresent()) {
                salaryAmount = currentSalary.get().getSalaryAmount();
                currencyCode = currentSalary.get().getCurrencyCode();
            }
            return new EmployeeSummaryResponse(
                    emp.getId(), emp.getEmpNo(), emp.getFirstName(), emp.getLastName(),
                    emp.getCountry(), emp.getDept(), emp.getJobTitle(),
                    emp.getEmploymentStatus().toString(), salaryAmount, currencyCode);
        });
    }

    @Transactional(readOnly = true)
    public EmployeeDetailResponse getById(Long id) {
        var emp = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new com.acme.salarymgmt.exception.ResourceNotFoundException("Employee", "id", id));

        var response = new EmployeeDetailResponse();
        mapToDetailResponse(emp, response);
        return response;
    }

    @Transactional
    public EmployeeSummaryResponse create(CreateEmployeeRequest request) {
        validationService.validateRequired(request.getEmpNo(), "emp_no");
        validationService.validateRequired(request.getEmail(), "email");
        validationService.validateEmail(request.getEmail());
        validationService.validateDates(request.getStartDate(), request.getEndDate());

        if (employeeRepository.existsByEmpNo(request.getEmpNo())) {
            throw new com.acme.salarymgmt.exception.ValidationException(
                    "Duplicate employee number",
                    java.util.List.of("emp_no: " + request.getEmpNo() + " already exists"));
        }

        var emp = new Employee();
        emp.setEmpNo(request.getEmpNo());
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPhone(request.getPhone());
        emp.setAddressLine1(request.getAddressLine1());
        emp.setCity(request.getCity());
        emp.setStateProvince(request.getStateProvince());
        emp.setZipCode(request.getZipCode());
        emp.setCountry(request.getCountry());
        emp.setDept(request.getDept());
        emp.setJobTitle(request.getJobTitle());
        emp.setEmploymentStatus(Employee.EmploymentStatus.valueOf(request.getEmploymentStatus()));
        emp.setStartDate(request.getStartDate());
        emp.setEndDate(request.getEndDate());
        emp.setActive(true);

        emp = employeeRepository.save(emp);

        auditLogService.logInsert(
                emp.getId(), "employees", emp.getId(), "all_fields",
                "NEW: " + emp.getEmpNo(), 1L);

        return new EmployeeSummaryResponse(
                emp.getId(), emp.getEmpNo(), emp.getFirstName(), emp.getLastName(),
                emp.getCountry(), emp.getDept(), emp.getJobTitle(),
                emp.getEmploymentStatus().toString(), null, null);
    }

    @Transactional
    public EmployeeDetailResponse update(Long id, CreateEmployeeRequest request) {
        var emp = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new com.acme.salarymgmt.exception.ResourceNotFoundException("Employee", "id", id));

        String oldDept = emp.getDept();
        String oldStatus = emp.getEmploymentStatus().toString();

        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPhone(request.getPhone());
        emp.setAddressLine1(request.getAddressLine1());
        emp.setCity(request.getCity());
        emp.setStateProvince(request.getStateProvince());
        emp.setZipCode(request.getZipCode());
        emp.setCountry(request.getCountry());
        emp.setDept(request.getDept());
        emp.setJobTitle(request.getJobTitle());
        if (request.getEmploymentStatus() != null) {
            emp.setEmploymentStatus(Employee.EmploymentStatus.valueOf(request.getEmploymentStatus()));
        }
        emp.setStartDate(request.getStartDate());
        emp.setEndDate(request.getEndDate());

        employeeRepository.save(emp);

        // Log field changes
        if (!java.util.Objects.equals(oldDept, request.getDept())) {
            auditLogService.logUpdate(emp.getId(), "employees", id, "dept", oldDept, request.getDept(), 1L);
        }
        if (!java.util.Objects.equals(oldStatus, request.getEmploymentStatus())) {
            auditLogService.logUpdate(emp.getId(), "employees", id, "status", oldStatus, request.getEmploymentStatus(), 1L);
        }

        var response = new EmployeeDetailResponse();
        mapToDetailResponse(emp, response);
        return response;
    }

    @Transactional
    public void toggleStatus(Long id, String newStatus) {
        var emp = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new com.acme.salarymgmt.exception.ResourceNotFoundException("Employee", "id", id));

        var old = emp.getEmploymentStatus();
        emp.setEmploymentStatus(Employee.EmploymentStatus.valueOf(newStatus));
        if ("TERMINATED".equals(newStatus)) {
            emp.setEndDate(java.time.LocalDate.now());
        }
        employeeRepository.save(emp);
        auditLogService.logUpdate(emp.getId(), "employees", id, "status", old.toString(), newStatus, 1L);
    }

    @Transactional
    public void softDelete(Long id) {
        var emp = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new com.acme.salarymgmt.exception.ResourceNotFoundException("Employee", "id", id));

        emp.setActive(false);
        if (emp.getEmploymentStatus() == Employee.EmploymentStatus.ACTIVE) {
            emp.setEmploymentStatus(Employee.EmploymentStatus.TERMINATED);
        }
        employeeRepository.save(emp);
        auditLogService.logDelete(emp.getId(), "employees", id, 1L);
    }

    public java.util.List<Salary> getSalaryHistory(Long employeeId) {
        return salaryRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }

    private void mapToDetailResponse(Employee emp, EmployeeDetailResponse resp) {
        resp.setId(emp.getId());
        resp.setEmpNo(emp.getEmpNo());
        resp.setFirstName(emp.getFirstName());
        resp.setLastName(emp.getLastName());
        resp.setEmail(emp.getEmail());
        resp.setPhone(emp.getPhone());
        resp.setAddressLine1(emp.getAddressLine1());
        resp.setCity(emp.getCity());
        resp.setStateProvince(emp.getStateProvince());
        resp.setZipCode(emp.getZipCode());
        resp.setCountry(emp.getCountry());
        resp.setDept(emp.getDept());
        resp.setJobTitle(emp.getJobTitle());
        resp.setEmploymentStatus(emp.getEmploymentStatus().toString());
        resp.setStartDate(emp.getStartDate());
        resp.setEndDate(emp.getEndDate());
        resp.setActive(emp.getActive());
        resp.setCreatedAt(emp.getCreatedAt());
        resp.setCreatedBy(emp.getCreatedBy());
        resp.setUpdatedAt(emp.getUpdatedAt());

        // Current salary
        var current = salaryRepository.findCurrentByEmployeeId(emp.getId());
        if (current.isPresent()) {
            resp.setCurrentSalaryAmount(current.get().getSalaryAmount());
            resp.setCurrentCurrencyCode(current.get().getCurrencyCode());
            resp.setEffectiveDate(current.get().getEffectiveDate());
        }
    }

    public interface CreateEmployeeRequest {
        String getEmpNo();
        String getFirstName();
        String getLastName();
        String getEmail();
        String getPhone();
        String getAddressLine1();
        String getCity();
        String getStateProvince();
        String getZipCode();
        String getCountry();
        String getDept();
        String getJobTitle();
        String getEmploymentStatus();
        java.time.LocalDate getStartDate();
        java.time.LocalDate getEndDate();
    }
}
