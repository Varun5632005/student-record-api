#!/bin/sh
set -e

# Port configuration: Render injects PORT (e.g. 10000). Default to 8080 if not set.
APP_PORT="${PORT:-8080}"

# Database URL detection: Render defines DATABASE_URL when PostgreSQL is attached.
# Users may also set SPRING_DATASOURCE_URL.
RAW_URL="${SPRING_DATASOURCE_URL:-${DATABASE_URL:-}}"

JAVA_OPTS="-Dserver.port=${APP_PORT}"

if [ -n "$RAW_URL" ]; then
    echo "Configuring database connection from provided URL..."
    
    # Check if Postgres
    if echo "$RAW_URL" | grep -qE "^(postgres|postgresql|jdbc:postgresql)://"; then
        echo "PostgreSQL database detected."
        JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.driver-class-name=org.postgresql.Driver"
        
        # Check if URL contains credentials in user:pass@host format (Render style)
        if echo "$RAW_URL" | grep -q "@"; then
            USER=$(echo "$RAW_URL" | sed -E 's#^(postgres|postgresql|jdbc:postgresql)://([^:]+):([^@]+)@.*#\2#')
            PASS=$(echo "$RAW_URL" | sed -E 's#^(postgres|postgresql|jdbc:postgresql)://([^:]+):([^@]+)@.*#\3#')
            HOST_DB=$(echo "$RAW_URL" | sed -E 's#^(postgres|postgresql|jdbc:postgresql)://[^@]+@(.*)#\2#')
            
            JDBC_URL="jdbc:postgresql://${HOST_DB}"
            
            JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.url=${JDBC_URL}"
            JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.username=${USER}"
            JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.password=${PASS}"
        else
            # Ensure jdbc: prefix exists
            if ! echo "$RAW_URL" | grep -q "^jdbc:"; then
                RAW_URL="jdbc:${RAW_URL}"
            fi
            JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.url=${RAW_URL}"
        fi
    else
        JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.url=${RAW_URL}"
    fi
else
    echo "No external database URL found. Defaulting to H2 in-memory database."
    JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.driver-class-name=org.h2.Driver"
    JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.url=jdbc:h2:mem:studentdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.username=sa"
    JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.password="
fi

echo "Starting Spring Boot application on port ${APP_PORT}..."
exec java ${JAVA_OPTS} -jar app.jar
