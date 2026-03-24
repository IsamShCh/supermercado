# Supermercado

Sistema de gestión de supermercado basado en microservicios con comunicación gRPC y eventos Kafka.

## Requisitos

- Java 17
- Maven 3.9+
- Docker y Docker Compose

## Estructura del proyecto

```
supermercado/
├── api/                    # Contratos gRPC (.proto) y stubs generados
├── servicio-catalogo/      # Gestión de productos y categorías
├── servicio-inventario/    # Control de stock y lotes
├── servicio-ventas/        # Tickets y líneas de venta
├── servicio-usuarios/      # Usuarios, roles y permisos
├── servicio-reportes/      # Reportes (consume eventos Kafka)
├── simulador-supermercado/ # Simulador de operaciones
├── pruebas-e2e/            # Tests de integración end-to-end
├── scripts/                # Scripts de utilidad
└── documentos/             # Requisitos, diccionario, diagramas
```

## Levantar el proyecto

### Con Docker (todo incluido)

```bash
docker-compose up --build
```

Esto levanta los 5 servicios, sus bases de datos, Kafka, Kafka UI y Metabase.

### En local (solo servicios, las BDs y Kafka deben estar corriendo)

Primero levantar la infraestructura:

```bash
docker-compose up catalogo-db inventario-db ventas-db usuarios-db reportes-db zookeeper kafka
```

Luego compilar y arrancar los servicios:

```bash
mvn clean install -DskipTests
scripts\start_services.bat
```

## Puertos

| Servicio | HTTP | gRPC |
|---|---|---|
| servicio-catalogo | 8080 | 9090 |
| servicio-inventario | 8081 | 9091 |
| servicio-ventas | 8082 | 9092 |
| servicio-usuarios | 8083 | 9093 |
| servicio-reportes | 8085 | 9095 |

| Infraestructura | Puerto |
|---|---|
| Kafka (host) | 29092 |
| Kafka UI | 8090 |
| Metabase | 3000 |
| catalogo-db | 5432 |
| inventario-db | 5433 |
| ventas-db | 5434 |
| usuarios-db | 5435 |
| reportes-db | 5436 |

## Tests E2E

Los tests end-to-end están desactivados por defecto. Para ejecutarlos (con los servicios levantados):

```bash
mvn clean verify -P e2e
```

## Stack

- Spring Boot 3.5.7
- Spring gRPC 0.12.0
- gRPC 1.76.0 / Protobuf 4.32.1
- PostgreSQL 15
- Apache Kafka (Confluent 7.5.0)
- Metabase 0.47.2
