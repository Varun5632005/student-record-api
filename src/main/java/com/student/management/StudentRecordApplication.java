package com.student.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;

@SpringBootApplication
public class StudentRecordApplication {

    public static void main(String[] args) {
        configureDatabaseEnvironment();
        SpringApplication.run(StudentRecordApplication.class, args);
    }

    private static void configureDatabaseEnvironment() {
        String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            dbUrl = System.getenv("DATABASE_URL");
        }

        if (dbUrl != null && !dbUrl.trim().isEmpty()) {
            dbUrl = dbUrl.trim();
            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                try {
                    // Replace protocol scheme with http:// so java.net.URI can parse user:pass@host:port/db
                    String httpUrl = dbUrl.replaceFirst("^(postgres|postgresql)://", "http://");
                    URI uri = new URI(httpUrl);

                    String host = uri.getHost();
                    int port = uri.getPort();
                    String path = uri.getPath();
                    String userInfo = uri.getUserInfo();
                    String query = uri.getQuery();

                    StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://").append(host);
                    if (port != -1) {
                        jdbcUrl.append(":").append(port);
                    }
                    if (path != null) {
                        jdbcUrl.append(path);
                    }
                    if (query != null && !query.isEmpty()) {
                        jdbcUrl.append("?").append(query);
                    }

                    System.setProperty("spring.datasource.url", jdbcUrl.toString());
                    System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");

                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        System.setProperty("spring.datasource.username", parts[0]);
                        System.setProperty("spring.datasource.password", parts[1]);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse database URL: " + e.getMessage());
                }
            } else if (dbUrl.startsWith("jdbc:postgresql://")) {
                System.setProperty("spring.datasource.url", dbUrl);
                System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
            }
        }
    }
}
