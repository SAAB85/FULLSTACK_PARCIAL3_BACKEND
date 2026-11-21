package cl.duoc.fullstack.ep3.dto;

import cl.duoc.fullstack.ep3.model.DetalleBoleta;

import java.math.BigDecimal;

public record DetalleBoletaResponse(
        Long productoId,
        String codigo,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {

    public static DetalleBoletaResponse desde(DetalleBoleta detalle) {
        return new DetalleBoletaResponse(
                detalle.getProducto().getId(),
                detalle.getProducto().getCodigo(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal());
    }
}
