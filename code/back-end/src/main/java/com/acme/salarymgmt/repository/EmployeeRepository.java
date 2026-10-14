package com.acme.salarymgmt.repository;

import com.acme.salarymgmt.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmpNo(String empNo);

    boolean existsByEmpNo(String empNo);

    boolean existsByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE e.isActive = true AND " +
           "(:search IS NULL OR LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE CONCAT('%', LOWER(:search), '%') OR LOWER(e.email) LIKE CONCAT('%', LOWER(:search), '%'))")
    Page<Employee> searchActive(
            @Param("search") String search, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.isActive = true AND " +
           "(:country IS NULL OR e.country = :country) AND " +
           "(:dept IS NULL OR e.dept = :dept) AND " +
           "(:status IS NULL OR e.employmentStatus = :status) AND " +
           "(:search IS NULL OR LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE CONCAT('%', LOWER(:search), '%') OR LOWER(e.email) LIKE CONCAT('%', LOWER(:search), '%'))")
    Page<Employee> searchWithFilters(
            @Param("country") String country,
            @Param("dept") String dept,
            @Param("status") Employee.EmploymentStatus status,
            @Param("search") String search,
            Pageable pageable);

    long countByCountryAndDeptAndIsActiveTrue(String country, String dept);

    @Query("SELECT e FROM Employee e WHERE e.id = :id AND e.isActive = true")
    Optional<Employee> findActiveById(@Param("id") Long id);
}
