# Zhilian Assistive Data Platform (Skeleton)

This workspace contains a starter implementation for the "Zhilian Assistive Data Platform" project.

Folders:
- backend/: Spring Boot 3 + Spring Security + MyBatis-Plus (DM8-ready) + Springdoc
- frontend/: Vue 3 + Ant Design Vue (scaffold; requires Node.js to run)

Local prerequisites:
- Java 21+ (you have Java 25)
- Maven 3.9+
- Node.js 18+ (not detected in current environment)

Run backend (dev profile uses H2 for bootstrapping):
- cd backend
- mvn spring-boot:run

DM8:
- Provide DM JDBC driver jar to the runtime classpath and set `spring.profiles.active=dm`.
- Run `backend/db/schema_dm8.sql` then `backend/db/seed_permissions_dm8.sql`.
- Initial admin user is created by backend on first run.
