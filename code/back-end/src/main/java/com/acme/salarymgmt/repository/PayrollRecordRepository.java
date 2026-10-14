package com.acme.salarymgmt.repository;

import com.acme.salarymgmt.model.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {

    @Query("SELECT p FROM PayrollRecord p WHERE p.employeeId = :employeeId ORDER BY p.periodStart DESC")
    List<PayrollRecord> findByEmployeeIdOrderByPeriodStartDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT p FROM PayrollRecord p WHERE p.payDate BETWEEN :start AND :end ORDER BY p.payDate")
    List<PayrollRecord> findByPayDateBetween(@Param("start") java.time.LocalDate start,
                                               @Param("end") java.time.LocalDate end);

    long countByPayrollStatus(PayrollRecord.PayrollStatus status);
}
