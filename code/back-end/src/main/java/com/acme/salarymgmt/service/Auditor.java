package com.acme.salarymgmt.service;

/**
 * Interface for audit log service to enable mocking without inline mock maker on Java 26+.
 */
public interface Auditor {
    void logInsert(Long employeeId, String tableName, Long recordId, String fieldName,
                   Object newValue, Long actorId);
    void logUpdate(Long employeeId, String tableName, Long recordId, String fieldName,
                   Object oldValue, Object newValue, Long actorId);
    void logDelete(Long employeeId, String tableName, Long recordId, Long actorId);
}
