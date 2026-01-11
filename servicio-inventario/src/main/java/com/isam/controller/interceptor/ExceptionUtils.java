package com.isam.controller.interceptor;


import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessage;
import com.google.rpc.Code;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;

// Spring Data Access Exceptions
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

// JPA Persistence Exceptions
import jakarta.persistence.PersistenceException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;

// Hibernate Exceptions
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.LazyInitializationException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.QueryException;


// Transaction Exceptions
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;

// JDBC/SQL Exceptions
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.net.SocketException;

public class ExceptionUtils {

    /**
     * Maps exceptions to gRPC status codes without additional details
     */
    public static StatusRuntimeException trazarException(Throwable exception){
        return trazarException(exception, null);
    }

    /**
     * Maps exceptions to gRPC status codes with optional protobuf message details
     * This method handles all database, persistence, validation, and transaction exceptions
     */
    public static <T extends GeneratedMessage> StatusRuntimeException trazarException(Throwable excepcion, T mensajeGenerico){

        com.google.rpc.Status status;
        StatusRuntimeException statusRuntimeException;

        // If already a StatusRuntimeException, return as-is
        if(excepcion instanceof StatusRuntimeException){
            return (StatusRuntimeException) excepcion;
        }

        // Unwrap the exception to get the root cause
        Throwable cause = unwrapException(excepcion);

        // Map exception to appropriate gRPC status
        status = mapExceptionToStatus(cause, mensajeGenerico);
        statusRuntimeException = StatusProto.toStatusRuntimeException(status);

        return statusRuntimeException;
    }

    /**
     * Unwraps nested exceptions to find the root cause
     */
    private static Throwable unwrapException(Throwable exception) {
        Throwable cause = exception;
        while(cause != null && cause.getCause() != null && cause != cause.getCause()){
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Maps specific exception types to gRPC status codes with appropriate messages
     */
    private static <T extends GeneratedMessage> com.google.rpc.Status mapExceptionToStatus(Throwable cause, T mensajeGenerico) {
        
        // ============================================================
        // 0) SPRING SECURITY EXCEPTIONS
        // ============================================================
        // Caso 1: Usuario anónimo intenta acceder a recurso protegido
        if (cause instanceof AuthenticationCredentialsNotFoundException) {
            return buildStatus(Code.UNAUTHENTICATED_VALUE, // Código gRPC 16
                "No autenticado. Debe iniciar sesión y enviar un token válido para realizar esta acción.",
                null, mensajeGenerico);
        }

        // Caso 2: Usuario logueado pero sin permisos suficientes.
        if (cause instanceof org.springframework.security.access.AccessDeniedException) {
            return buildStatus(Code.PERMISSION_DENIED_VALUE, // Código gRPC 7
                "Acceso denegado. No tiene los permisos necesarios para realizar esta acción.",
                null, mensajeGenerico);
        }

        // ============================================================
        // 1) SPRING DATA ACCESS EXCEPTIONS
        // ============================================================
        
        // Duplicate key violations (unique constraint)
        if (cause instanceof DuplicateKeyException) {
            return buildStatus(Code.ALREADY_EXISTS_VALUE,
                "Registro duplicado. La entidad con estos datos ya existe",
                cause, mensajeGenerico);
        }
        
        // Data integrity violations (FK, constraints)
        if (cause instanceof DataIntegrityViolationException) {
            return buildStatus(Code.FAILED_PRECONDITION_VALUE,
                "Violación de integridad de datos. No se cumplen las restricciones de la base de datos",
                cause, mensajeGenerico);
        }
        
        // Empty result when one was expected
        if (cause instanceof EmptyResultDataAccessException) {
            return buildStatus(Code.NOT_FOUND_VALUE,
                "Recurso no encontrado. La consulta no devolvió resultados",
                cause, mensajeGenerico);
        }
        
        // Incorrect result size
        if (cause instanceof IncorrectResultSizeDataAccessException) {
            return buildStatus(Code.FAILED_PRECONDITION_VALUE,
                "Resultado inesperado. Se esperaba un único resultado pero se obtuvieron múltiples",
                cause, mensajeGenerico);
        }
        
        // Data retrieval failure
        if (cause instanceof DataRetrievalFailureException) {
            return buildStatus(Code.INTERNAL_VALUE,
                "Error al recuperar datos. Fallo en la lectura desde la base de datos",
                cause, mensajeGenerico);
        }
        
        // Invalid API usage
        if (cause instanceof InvalidDataAccessApiUsageException) {
            return buildStatus(Code.INVALID_ARGUMENT_VALUE,
                "Uso incorrecto de la API de datos. Operación inválida",
                cause, mensajeGenerico);
        }
        
        // Optimistic locking failure (Spring)
        if (cause instanceof OptimisticLockingFailureException) {
            return buildStatus(Code.ABORTED_VALUE,
                "Conflicto de concurrencia. El recurso fue modificado por otra transacción",
                cause, mensajeGenerico);
        }
        
        // Generic Spring DataAccessException
        if (cause instanceof DataAccessException) {
            return buildStatus(Code.INTERNAL_VALUE,
                "Error de acceso a datos",
                cause, mensajeGenerico);
        }

        // ============================================================
        // 2) JPA / JAKARTA PERSISTENCE EXCEPTIONS
        // ============================================================
        
        // Entity already exists
        if (cause instanceof EntityExistsException) {
            return buildStatus(Code.ALREADY_EXISTS_VALUE,
                "La entidad que se intenta persistir ya existe",
                cause, mensajeGenerico);
        }
        
        // Entity not found
        if (cause instanceof EntityNotFoundException) {
            return buildStatus(Code.NOT_FOUND_VALUE,
                "Entidad no encontrada. No existe el recurso solicitado",
                cause, mensajeGenerico);
        }
        
        // No result from getSingleResult()
        if (cause instanceof NoResultException) {
            return buildStatus(Code.NOT_FOUND_VALUE,
                "Sin resultados. La consulta no devolvió ningún resultado",
                cause, mensajeGenerico);
        }
        
        // Multiple results from getSingleResult()
        if (cause instanceof NonUniqueResultException) {
            return buildStatus(Code.FAILED_PRECONDITION_VALUE,
                "Resultado no único. Se esperaba un único resultado pero se obtuvieron múltiples",
                cause, mensajeGenerico);
        }
        
        // Optimistic lock exception (JPA)
        if (cause instanceof OptimisticLockException) {
            return buildStatus(Code.ABORTED_VALUE,
                "Conflicto de versión optimista. El recurso fue modificado por otra transacción",
                cause, mensajeGenerico);
        }
        
        // Rollback exception
        if (cause instanceof RollbackException) {
            return buildStatus(Code.ABORTED_VALUE,
                "Transacción revertida. La operación no pudo completarse",
                cause, mensajeGenerico);
        }
        
        // Generic persistence exception
        if (cause instanceof PersistenceException) {
            return buildStatus(Code.INTERNAL_VALUE,
                "Error de persistencia",
                cause, mensajeGenerico);
        }

        // ============================================================
        // 3) HIBERNATE SPECIFIC EXCEPTIONS
        // ============================================================
        
        // Hibernate constraint violation (includes unique, FK, etc.)
        if (cause instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) cause;
            String constraintName = cve.getConstraintName();
            String message = "Violación de restricción de base de datos";
            if (constraintName != null) {
                message += " (" + constraintName + ")";
            }
            return buildStatus(Code.FAILED_PRECONDITION_VALUE, message, cause, mensajeGenerico);
        }
        
        // Lazy initialization exception
        if (cause instanceof LazyInitializationException) {
            return buildStatus(Code.FAILED_PRECONDITION_VALUE,
                "Error de inicialización lazy. Se intentó acceder a datos fuera de la sesión de persistencia",
                cause, mensajeGenerico);
        }
        
        // Stale object state (optimistic locking)
        if (cause instanceof StaleObjectStateException) {
            return buildStatus(Code.ABORTED_VALUE,
                "Estado obsoleto. El objeto fue modificado o eliminado por otra transacción",
                cause, mensajeGenerico);
        }
        
        // JDBC connection exception
        if (cause instanceof JDBCConnectionException) {
            return buildStatus(Code.UNAVAILABLE_VALUE,
                "Error de conexión a la base de datos. No se pudo establecer o mantener la conexión",
                cause, mensajeGenerico);
        }
        
        // Hibernate query exception
        if (cause instanceof QueryException) {
            return buildStatus(Code.INVALID_ARGUMENT_VALUE,
                "Error en la consulta. La query HQL/JPQL contiene errores",
                cause, mensajeGenerico);
        }

        // ============================================================
        // 4) BEAN VALIDATION EXCEPTIONS
        // ============================================================
        
        // Jakarta Bean Validation constraint violations (using fully qualified name to avoid import collision)
        if (cause instanceof jakarta.validation.ConstraintViolationException) {
            jakarta.validation.ConstraintViolationException cve =
                (jakarta.validation.ConstraintViolationException) cause;
            StringBuilder violationsMsg = new StringBuilder("Errores de validación:\n");
            cve.getConstraintViolations().forEach(violation ->
                violationsMsg.append("- ").append(violation.getPropertyPath())
                    .append(": ").append(violation.getMessage()).append("\n")
            );
            return buildStatus(Code.INVALID_ARGUMENT_VALUE,
                violationsMsg.toString().trim(),
                null, mensajeGenerico);
        }

        // ============================================================
        // 5) TRANSACTION EXCEPTIONS
        // ============================================================
        
        // Transaction system exception
        if (cause instanceof TransactionSystemException) {
            return buildStatus(Code.INTERNAL_VALUE,
                "Error del sistema transaccional. Fallo al gestionar la transacción",
                cause, mensajeGenerico);
        }
        
        // Generic transaction exception
        if (cause instanceof TransactionException) {
            return buildStatus(Code.ABORTED_VALUE,
                "Error transaccional",
                cause, mensajeGenerico);
        }

        // ============================================================
        // 6) SQL / JDBC EXCEPTIONS (PostgreSQL-specific)
        // ============================================================

        // SQL integrity constraint violation (generic, will be overridden by specific SQLState)
        if (cause instanceof SQLIntegrityConstraintViolationException) {
            return buildStatus(Code.FAILED_PRECONDITION_VALUE,
                "Violación de restricción SQL. No se cumplen las reglas de integridad",
                cause, mensajeGenerico);
        }

        // SQL timeout
        if (cause instanceof SQLTimeoutException) {
            return buildStatus(Code.DEADLINE_EXCEEDED_VALUE,
                "Timeout en base de datos. La operación superó el tiempo límite",
                cause, mensajeGenerico);
        }

        // PostgreSQL-specific SQLState mapping
        if (cause instanceof SQLException) {
            SQLException sqlEx = (SQLException) cause;
            String sqlState = sqlEx.getSQLState();

            if ("23505".equals(sqlState)) { // unique_violation
                return buildStatus(Code.ALREADY_EXISTS_VALUE,
                    "Registro duplicado. Ya existe un registro con estos datos únicos",
                    cause, mensajeGenerico);
            } else if ("23503".equals(sqlState)) { // foreign_key_violation
                return buildStatus(Code.FAILED_PRECONDITION_VALUE,
                    "Referencia inválida. El registro referenciado no existe",
                    cause, mensajeGenerico);
            } else if ("23502".equals(sqlState)) { // not_null_violation
                return buildStatus(Code.INVALID_ARGUMENT_VALUE,
                    "Campo requerido. No se puede dejar vacío un campo obligatorio",
                    cause, mensajeGenerico);
            } else if ("23514".equals(sqlState)) { // check_violation
                return buildStatus(Code.FAILED_PRECONDITION_VALUE,
                    "Violación de regla. Los datos no cumplen las reglas de validación",
                    cause, mensajeGenerico);
            } else if ("42P01".equals(sqlState)) { // undefined_table
                return buildStatus(Code.NOT_FOUND_VALUE,
                    "Tabla no encontrada. La tabla especificada no existe",
                    cause, mensajeGenerico);
            } else if ("42703".equals(sqlState)) { // undefined_column
                return buildStatus(Code.INVALID_ARGUMENT_VALUE,
                    "Columna no encontrada. La columna especificada no existe",
                    cause, mensajeGenerico);
            } else if ("08003".equals(sqlState)) { // connection_does_not_exist
                return buildStatus(Code.UNAVAILABLE_VALUE,
                    "Conexión perdida. La conexión a la base de datos se ha perdido",
                    cause, mensajeGenerico);
            } else if ("08006".equals(sqlState)) { // connection_failure
                return buildStatus(Code.UNAVAILABLE_VALUE,
                    "Error de conexión. No se pudo conectar a la base de datos",
                    cause, mensajeGenerico);
            } else if ("53300".equals(sqlState)) { // too_many_connections
                return buildStatus(Code.RESOURCE_EXHAUSTED_VALUE,
                    "Límite de conexiones excedido. Demasiadas conexiones activas",
                    cause, mensajeGenerico);
            } else if ("42P07".equals(sqlState)) { // duplicate_table
                return buildStatus(Code.ALREADY_EXISTS_VALUE,
                    "Tabla duplicada. La tabla ya existe",
                    cause, mensajeGenerico);
            } else if ("42701".equals(sqlState)) { // duplicate_column
                return buildStatus(Code.ALREADY_EXISTS_VALUE,
                    "Columna duplicada. La columna ya existe",
                    cause, mensajeGenerico);
            }

            // Fallback para SQLState no mapeados específicamente
            String message = "Error SQL";
            if (sqlState != null) {
                message += " (SQLState: " + sqlState + ")";
            }
            return buildStatus(Code.INTERNAL_VALUE, message, cause, mensajeGenerico);
        }

        // ============================================================
        // 7) NETWORK / CONNECTION EXCEPTIONS
        // ============================================================
        
        // Socket exceptions
        if (cause instanceof SocketException) {
            return buildStatus(Code.UNAVAILABLE_VALUE,
                "Error de conexión de red",
                cause, mensajeGenerico);
        }

        // ============================================================
        // 8) DEFAULT / UNKNOWN EXCEPTIONS
        // ============================================================
        
        // Fallback for any unhandled exception
        return buildStatus(Code.INTERNAL_VALUE,
            "Error interno del servidor",
            cause, mensajeGenerico);
    }

    /**
     * Helper method to build a gRPC Status with consistent structure
     */
    private static <T extends GeneratedMessage> com.google.rpc.Status buildStatus(
            int code,
            String baseMessage,
            Throwable cause,
            T mensajeGenerico) {
        
        StringBuilder fullMessage = new StringBuilder(baseMessage);
        
        // Append exception message if available
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isEmpty()) {
            fullMessage.append(":\n").append(cause.getMessage());
        }
        
        com.google.rpc.Status.Builder statusBuilder = com.google.rpc.Status.newBuilder()
                .setCode(code)
                .setMessage(fullMessage.toString());
        
        // Add protobuf details if provided
        if (mensajeGenerico != null) {
            statusBuilder.addDetails(Any.pack(mensajeGenerico));
        }
        
        return statusBuilder.build();
    }

    public static <Generico extends GeneratedMessage> void observarError(
            StreamObserver<Generico> responseObserver,
            Throwable throwable
    ){
        responseObserver.onError(trazarException(throwable));

    }

    public static <Generico extends GeneratedMessage> void observarError(
            StreamObserver<Generico> responseObserver,
            Throwable throwable,
            Generico mensajeDetalles
    ){
        responseObserver.onError(trazarException(throwable, mensajeDetalles));
    }



}