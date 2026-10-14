package com.acme.salarymgmt.service;

import com.acme.salarymgmt.model.AuditLogEntry;
import com.acme.salarymgmt.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final Long defaultUserId = 1L; // System Admin

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logInsert(Long employeeId, String tableName, Long recordId, String fieldName,
                          Object newValue, Long actorId) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setEmployeeId(employeeId);
        entry.setTableName(tableName);
        entry.setRecordId(recordId);
        entry.setFieldName(fieldName);
        entry.setNewValue(newValue != null ? String.valueOf(newValue) : null);
        entry.setChangeType(AuditLogEntry.ChangeType.INSERT);
        entry.setChangedBy(actorId != null ? actorId : defaultUserId);
        auditLogRepository.save(entry);
    }

    @Transactional
    public void logUpdate(Long employeeId, String tableName, Long recordId, String fieldName,
                          Object oldValue, Object newValue, Long actorId) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setEmployeeId(employeeId);
        entry.setTableName(tableName);
        entry.setRecordId(recordId);
        entry.setFieldName(fieldName);
        entry.setOldValue(oldValue != null ? String.valueOf(oldValue) : null);
        entry.setNewValue(newValue != null ? String.valueOf(newValue) : null);
        entry.setChangeType(AuditLogEntry.ChangeType.UPDATE);
        entry.setChangedBy(actorId != null ? actorId : defaultUserId);
        auditLogRepository.save(entry);
    }

    @Transactional
    public void logDelete(Long employeeId, String tableName, Long recordId, Long actorId) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setEmployeeId(employeeId);
        entry.setTableName(tableName);
        entry.setRecordId(recordId);
        entry.setChangeType(AuditLogEntry.ChangeType.DELETE);
        entry.setChangedBy(actorId != null ? actorId : defaultUserId);
        auditLogRepository.save(entry);
    }

    public java.util.List<AuditLogEntry> findByEmployeeId(Long employeeId) {
        return auditLogRepository.findByEmployeeIdOrderByChangedAtDesc(employeeId);
    }
}
