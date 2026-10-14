package com.acme.salarymgmt.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            // In session-based auth, the principal name holds the user ID as a Long string
            try {
                return Optional.of(Long.parseLong(auth.getName()));
            } catch (NumberFormatException e) {
                // Fallback to first created employee (the admin seed record)
                return Optional.of(1L);
            }
        }
        return Optional.of(1L);
    }
}
