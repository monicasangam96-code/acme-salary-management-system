package com.acme.salarymgmt.repository;

import com.acme.salarymgmt.model.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {

    List<AuditLogEntry> findByEmployeeIdOrderByChangedAtDesc(Long employeeId);

    List<AuditLogEntry> findByTableNameAndRecordIdOrderByChangedAtDesc(String tableName, Long recordId);
}
