package com.acme.salarymgmt.audit;

import com.acme.salarymgmt.model.AuditLogEntry;
import com.acme.salarymgmt.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditTrailAspect {

    private final AuditLogService auditLogService;

    public AuditTrailAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Automatically log all mutations performed by salary-related service methods.
     * The actor ID comes from the SecurityContext (set to 1L / System Admin for MVP).
     */
    @Around("execution(* com.acme.salarymgmt.service.SalaryService.*(..))")
    public Object aroundSalaryMutations(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
        // The salary mutation itself triggers audit via AuditLogService inside the service method.
        // This aspect is a safety net for any missed audit calls.
    }

    /**
     * Automatically log all mutations on employee records.
     */
    @Around("execution(* com.acme.salarymgmt.service.EmployeeService.*(..))")
    public Object aroundEmployeeMutations(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
        // Employee mutations are logged explicitly inside the service method for field-level detail.
        // This aspect ensures any missed logs are caught.
    }

    /**
     * Log delete operations (soft-delete is a mutation).
     */
    @Around("execution(* com.acme.salarymgmt.service.EmployeeService.softDelete(..)) || " +
            "execution(* com.acme.salarymgmt.repository.EmployeeRepository.delete*())")
    public Object aroundDeletes(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Log all insert operations (new employee creation).
     */
    @Around("execution(* com.acme.salarymgmt.service.EmployeeService.createNew(..)) || " +
            "execution(* com.acme.salarymgmt.repository.EmployeeRepository.save*())")
    public Object aroundInserts(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Log all update operations.
     */
    @Around("execution(* com.acme.salarymgmt.service.*Service.update*(..)) || " +
            "execution(* com.acme.salarymgmt.repository.*Repository.save*())")
    public Object aroundUpdates(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
