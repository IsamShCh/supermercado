package com.isam.dto.producto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EanPluValidator.class)
@Documented
public @interface EanOrPlu {
    String message() default "Debe proporcionar EAN o PLU, pero no ambos";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}