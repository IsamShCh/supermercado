package com.isam.dto.producto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCodigoSizeValidator.class)
@Documented
public @interface ValidCodigoSize {
    String message() default "El tamaño del código no es válido para el tipo de identificador";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}