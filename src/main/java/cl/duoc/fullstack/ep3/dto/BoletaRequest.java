package cl.duoc.fullstack.ep3.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BoletaRequest(
        @NotEmpty(message = "La boleta debe tener al menos un producto")
        List<@Valid ItemBoletaRequest> items,

        @Size(max = 300, message = "La observación admite hasta 300 caracteres")
        String observacion) {
}
