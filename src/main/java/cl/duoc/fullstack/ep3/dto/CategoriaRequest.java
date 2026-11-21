package cl.duoc.fullstack.ep3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 60, message = "El nombre admite hasta 60 caracteres")
        String nombre,

        @Size(max = 300, message = "La descripción admite hasta 300 caracteres")
        String descripcion) {
}
