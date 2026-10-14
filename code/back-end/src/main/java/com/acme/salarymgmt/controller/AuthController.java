package com.acme.salarymgmt.controller;

import com.acme.salarymgmt.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> credentials,
                                               HttpServletRequest request) {
        // MVP: Simple demo auth (replace with real authentication in production)
        String email = credentials.get("email");
        String password = credentials.get("password");

        if ("admin@acme.com".equals(email) && "admin123".equals(password)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("authenticated", true);
            session.setAttribute("user", Map.of(
                    "id", 1,
                    "name", "1",
                    "userName", "System Admin",
                    "role", "HR_MANAGER"
            ));

            return ResponseEntity.ok(new AuthResponse(
                    session.getId(), 1L, "System Admin", "HR_MANAGER"));
        }

        if ("hranalyst@acme.com".equals(email) && "analyst123".equals(password)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("authenticated", true);
            session.setAttribute("user", Map.of(
                    "id", 2,
                    "name", "2",
                    "userName", "HR Analyst",
                    "role", "HR_ANALYST"
            ));

            return ResponseEntity.ok(new AuthResponse(
                    session.getId(), 2L, "HR Analyst", "HR_ANALYST"));
        }

        return ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).build();
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        return ResponseEntity.ok(Map.of(
                "id", user.get("id"),
                "name", user.get("userName"),
                "role", user.get("role")
        ));
    }
}
