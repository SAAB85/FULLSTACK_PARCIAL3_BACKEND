package cl.duoc.fullstack.ep3.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 30, message = "El código admite hasta 30 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite hasta 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción admite hasta 500 caracteres")
        String descripcion,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 60, message = "La categoría admite hasta 60 caracteres")
        String categoria,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock) {
}
