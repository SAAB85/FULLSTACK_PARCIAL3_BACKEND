package cl.duoc.fullstack.ep3.dto;

import cl.duoc.fullstack.ep3.model.Boleta;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BoletaResponse(
        Long id,
        String numero,
        Instant fecha,
        String estado,
        String cliente,
        String clienteNombre,
        String observacion,
        BigDecimal total,
        List<DetalleBoletaResponse> detalles) {

    public static BoletaResponse desde(Boleta boleta) {
        List<DetalleBoletaResponse> detalles = boleta.getDetalles().stream()
                .map(DetalleBoletaResponse::desde)
                .toList();

        return new BoletaResponse(
                boleta.getId(),
                boleta.getNumero(),
                boleta.getFecha(),
                boleta.getEstado().name(),
                boleta.getCliente().getUsername(),
                boleta.getCliente().getNombre(),
                boleta.getObservacion(),
                boleta.getTotal(),
                detalles);
    }
}
