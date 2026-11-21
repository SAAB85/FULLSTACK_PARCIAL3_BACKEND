package cl.duoc.fullstack.ep3.dto;

import cl.duoc.fullstack.ep3.model.Producto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Long categoriaId,
        String categoria,
        BigDecimal precio,
        Integer stock) {

    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getPrecio(),
                producto.getStock());
    }
}
