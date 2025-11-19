package com.isam.dto.producto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCodigoSizeValidator implements ConstraintValidator<ValidCodigoSize, TraducirIdentificadorRequestDto> {

    @Override
    public boolean isValid(TraducirIdentificadorRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        String codigo = dto.codigo();
        TraducirIdentificadorRequestDto.TipoIdentificador tipo = dto.tipoIdentificador();

        if (codigo == null || tipo == null) {
            return true; // Dejar que otras validaciones manejen los nulos
        }

        int tamanoMaximo;
        switch (tipo) {
            case SKU:
                tamanoMaximo = 50;
                break;
            case EAN:
                tamanoMaximo = 13;
                break;
            case PLU:
                tamanoMaximo = 5;
                break;
            default:
                return true; // Tipo desconocido, permitir pasar
        }

        if (codigo.length() > tamanoMaximo) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "El código no puede exceder " + tamanoMaximo + " caracteres para " + tipo
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}