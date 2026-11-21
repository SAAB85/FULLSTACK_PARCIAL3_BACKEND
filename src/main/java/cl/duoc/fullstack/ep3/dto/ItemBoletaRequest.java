package cl.duoc.fullstack.ep3.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemBoletaRequest(
        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El producto debe tener un ID válido")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad) {
}
