# Fase Builder: Construir el proyecto completo
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY api ./api
COPY servicio-catalogo ./servicio-catalogo
COPY servicio-inventario ./servicio-inventario
COPY servicio-ventas ./servicio-ventas
COPY servicio-usuarios ./servicio-usuarios
COPY servicio-reportes ./servicio-reportes
COPY pruebas-e2e ./pruebas-e2e

# Construimos todo el proyecto saltandonos todos los test que tenemos
RUN mvn clean package -DskipTests

# Levantamos el módulo especificado
FROM eclipse-temurin:17-jre-alpine
ARG MODULE
ENV MODULE_NAME=${MODULE}
WORKDIR /app

# Copiamos el jar compilado del módulo
COPY --from=builder /app/${MODULE_NAME}/target/*.jar app.jar

# Exponemos los  puertos comunes para Spring Boot / gRPC 
EXPOSE 8080 8081 8082 8083 8085
EXPOSE 9090 9091 9092 9093 9095

ENTRYPOINT ["java", "-jar", "app.jar"]
