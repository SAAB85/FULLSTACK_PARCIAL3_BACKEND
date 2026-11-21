package cl.duoc.fullstack.ep3.dto;

import cl.duoc.fullstack.ep3.model.Categoria;

public record CategoriaResponse(
        Long id,
        String nombre,
        String descripcion) {

    public static CategoriaResponse desde(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion());
    }
}
