package com.isam.dto;

import com.isam.dto.inventario.CrearInventarioRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EanPluValidator implements ConstraintValidator<EanOrPlu, CrearInventarioRequestDto> {

    @Override
    public boolean isValid(CrearInventarioRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        boolean hasEan = isNotBlank(dto.ean());
        boolean hasPlu = isNotBlank(dto.plu());

        // Debe tener exactamente uno (XOR)
        if (hasEan && hasPlu) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "No se puede proporcionar EAN y PLU simultáneamente"
            ).addConstraintViolation();
            return false;
        }

        if (!hasEan && !hasPlu) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Debe proporcionar EAN o PLU"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

    // Utils
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}