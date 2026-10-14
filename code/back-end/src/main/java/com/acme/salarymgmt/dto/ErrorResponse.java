package com.acme.salarymgmt.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {

    private String error;
    private String message;
    private List<FieldErrorDetail> details;
    private LocalDateTime timestamp;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public static class FieldErrorDetail {
        private String field;
        private String message;

        public FieldErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // --- Getters and Setters ---
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<FieldErrorDetail> getDetails() { return details; }
    public void setDetails(List<FieldErrorDetail> details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
