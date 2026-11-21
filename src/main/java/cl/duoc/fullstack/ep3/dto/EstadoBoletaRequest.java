package cl.duoc.fullstack.ep3.dto;

import cl.duoc.fullstack.ep3.model.EstadoBoleta;
import jakarta.validation.constraints.NotNull;

public record EstadoBoletaRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoBoleta estado) {
}
