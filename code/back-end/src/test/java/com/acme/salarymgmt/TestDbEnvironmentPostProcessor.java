package com.acme.salarymgmt;

import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Creates the salary_mgmt_test MySQL database during Spring Boot startup
 * if using test application.yml (which references salary_mgmt_test).
 */
public class TestDbEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, org.springframework.boot.SpringApplication application) {
        String dbUrl = environment.getProperty("spring.datasource.url");
        if (dbUrl == null || !dbUrl.contains("salary_mgmt_test")) {
            return; // Not in test mode
        }

        String dbName = dbUrl.replaceAll("jdbc:mysql://[^/]+/", "").split("[?&]")[0];
        try (Connection c = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true",
                "root", "root99*");
             Statement s = c.createStatement()) {
            s.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (Exception ignored) {
            // Database may already exist or server not ready — tests will handle it
        }
    }
}
