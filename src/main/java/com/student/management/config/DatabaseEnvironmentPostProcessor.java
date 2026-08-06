package com.student.management.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String dbUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            dbUrl = environment.getProperty("DATABASE_URL");
        }

        Map<String, Object> map = new HashMap<>();

        if (dbUrl != null && !dbUrl.trim().isEmpty()) {
            dbUrl = dbUrl.trim();
            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                try {
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

                    map.put("spring.datasource.url", jdbcUrl.toString());
                    map.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
                    map.put("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQLDialect");

                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        map.put("spring.datasource.username", parts[0]);
                        map.put("spring.datasource.password", parts[1]);
                    }
                } catch (Exception e) {
                    System.err.println("DatabaseEnvironmentPostProcessor error parsing URL: " + e.getMessage());
                }
            } else if (dbUrl.startsWith("jdbc:postgresql://")) {
                map.put("spring.datasource.url", dbUrl);
                map.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
                map.put("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQLDialect");
            }
        } else {
            // Default to H2
            map.put("spring.datasource.url", "jdbc:h2:mem:studentdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            map.put("spring.datasource.driver-class-name", "org.h2.Driver");
            map.put("spring.datasource.username", "sa");
            map.put("spring.datasource.password", "");
            map.put("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
        }

        if (!map.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("customDatabaseConfig", map));
        }
    }
}
