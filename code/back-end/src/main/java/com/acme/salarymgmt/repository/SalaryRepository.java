package com.acme.salarymgmt.repository;

import com.acme.salarymgmt.dto.response.AnalyticsSummaryResponse;
import com.acme.salarymgmt.dto.response.CountryBreakdownResponse;
import com.acme.salarymgmt.dto.response.DepartmentBreakdownResponse;
import com.acme.salarymgmt.dto.response.JobLevelBreakdownResponse;
import com.acme.salarymgmt.model.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    @Query("SELECT s FROM Salary s WHERE s.employeeId = :employeeId ORDER BY s.effectiveDate DESC")
    List<Salary> findByEmployeeIdOrderByEffectiveDateDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT s FROM Salary s WHERE s.employeeId = :employeeId AND s.status = 'CURRENT'")
    Optional<Salary> findCurrentByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT s FROM Salary s WHERE s.employeeId = :employeeId AND s.status = 'CURRENT'")
    List<Salary> findAllCurrentByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT new com.acme.salarymgmt.dto.response.AnalyticsSummaryResponse(" +
           "COALESCE(SUM(s.salaryAmount), 0)," +
           "COALESCE(AVG(s.salaryAmount), 0)," +
           "(SELECT AVG(sub.salaryAmount) FROM Salary sub WHERE sub.status = 'CURRENT')," +
           "MIN(s.salaryAmount), MAX(s.salaryAmount), COUNT(s)) " +
           "FROM Salary s JOIN s.employee e " +
           "WHERE s.status = 'CURRENT' AND e.isActive = true " +
           "AND (:currencyCode IS NULL OR s.currencyCode = :currencyCode) " +
           "AND (:country IS NULL OR e.country = :country) " +
           "AND (:dept IS NULL OR e.dept = :dept)")
    List<AnalyticsSummaryResponse> computeSummary(
            @Param("currencyCode") String currencyCode,
            @Param("country") String country,
            @Param("dept") String dept);

    @Query("SELECT new com.acme.salarymgmt.dto.response.CountryBreakdownResponse(" +
           "e.country, COUNT(s), SUM(s.salaryAmount)) " +
           "FROM Salary s JOIN s.employee e " +
           "WHERE s.status = 'CURRENT' AND (:currencyCode IS NULL OR s.currencyCode = :currencyCode) " +
           "GROUP BY e.country ORDER BY SUM(s.salaryAmount) DESC")
    List<CountryBreakdownResponse> getAnalyticsByCountry(
            @Param("currencyCode") String currencyCode);

    @Query("SELECT new com.acme.salarymgmt.dto.response.DepartmentBreakdownResponse(" +
           "e.dept, COUNT(s), SUM(s.salaryAmount)) " +
           "FROM Salary s JOIN s.employee e " +
           "WHERE s.status = 'CURRENT' AND (:country IS NULL OR e.country = :country) " +
           "AND (:currencyCode IS NULL OR s.currencyCode = :currencyCode) " +
           "GROUP BY e.dept ORDER BY SUM(s.salaryAmount) DESC")
    List<DepartmentBreakdownResponse> getAnalyticsByDepartment(
            @Param("country") String country,
            @Param("currencyCode") String currencyCode);

    @Query("SELECT new com.acme.salarymgmt.dto.response.JobLevelBreakdownResponse(" +
           "e.jobTitle, COUNT(s), AVG(s.salaryAmount), " +
           "MIN(s.salaryAmount), MAX(s.salaryAmount)) " +
           "FROM Salary s JOIN s.employee e " +
           "WHERE s.status = 'CURRENT' AND (:country IS NULL OR e.country = :country) " +
           "AND (:dept IS NULL OR e.dept = :dept) " +
           "AND (:currencyCode IS NULL OR s.currencyCode = :currencyCode) " +
           "GROUP BY e.jobTitle ORDER BY AVG(s.salaryAmount) DESC")
    List<JobLevelBreakdownResponse> getAnalyticsByJobLevel(
            @Param("country") String country,
            @Param("dept") String dept,
            @Param("currencyCode") String currencyCode);
}
