package com.acme.salarymgmt.dto;

public class AuthResponse {
    private String sessionToken;
    private Long userId;
    private String userName;
    private String role;

    public AuthResponse(String sessionToken, Long userId, String userName, String role) {
        this.sessionToken = sessionToken;
        this.userId = userId;
        this.userName = userName;
        this.role = role;
    }

    public String getSessionToken() { return sessionToken; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getRole() { return role; }
}
