# SYSRED — Backend

API REST y lógica de negocio del sistema de gestión ISP SYSRED, construido con Spring Boot.

## Requisitos

- Java 8+
- MySQL / MariaDB
- Maven 3.6+

## Configuración

`src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wispcrm
    username: <usuario>
    password: <contraseña>
```

Variables sensibles (no commitear):

```yaml
# application-dev.yaml
ultramsg.token: <token>
aws.accessKey: <access_key>
aws.secretKey: <secret_key>
```

## Ejecutar

```bash
mvn clean install
java -jar target/wispcrm-0.0.1-SNAPSHOT.jar
```

API disponible en `http://localhost:8080`

## Estructura

```
src/main/java/org/wispcrm/
├── restcontroller/   # Endpoints REST
├── services/         # Lógica de negocio
├── daos/             # Repositorios JPA
├── modelo/           # Entidades y DTOs
├── mikrotik/         # Integración Mikrotik
├── utils/            # Tareas programadas y utilidades
└── SpringSecurityConfig.java
```

## Módulos principales

| Módulo | Descripción |
|--------|-------------|
| Clientes | CRUD y gestión de estado |
| Facturas | Generación, PDF (JasperReports), envío |
| Pagos | Registro y resumen mensual |
| Órdenes | Órdenes de servicio técnico |
| Planes | Planes de internet |
| Mikrotik | Control de perfiles de red |
| Notificaciones | Email (AWS SES) y WhatsApp (UltraMsg) |
