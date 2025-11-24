package com.isam.dto.inventario;

import io.grpc.Status;
import jakarta.validation.constraints.*;

public record ContabilizarStockManualRequestDto(
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,
    
    // Contabilización de estantería (opcional)
    @Digits(integer = 10, fraction = 3, message = "El stock físico de estantería debe tener máximo 10 dígitos enteros y 3 decimales")
    Double stockFisicoEstanteria,
    
    // Contabilización de almacén: solo modo preciso por lotes
    ContabilizacionPorLotesDto contabilizacionLotes
) {
    //NOTE : esta es otra forma de realizar validaciones personalizadas de forma un poco mas sencilla. Es muy util y nos ahorra tener que crear un Validator personalizado.
    public ContabilizarStockManualRequestDto {
        boolean tieneEstanteria = stockFisicoEstanteria != null;
        boolean tieneAlmacenLotes = contabilizacionLotes != null && !contabilizacionLotes.lotes().isEmpty();
        
        if (!tieneEstanteria && !tieneAlmacenLotes) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Debe proporcionar al menos una contabilización (estantería o almacén)")
                .asRuntimeException();
        }
        
        // Validar que los valores sean no negativos si están presentes
        if (stockFisicoEstanteria != null && stockFisicoEstanteria < 0) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El stock físico de estantería no puede ser negativo")
                .asRuntimeException();
        }
    }
}