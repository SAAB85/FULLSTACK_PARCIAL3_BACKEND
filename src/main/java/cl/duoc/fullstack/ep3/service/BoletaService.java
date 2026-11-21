package cl.duoc.fullstack.ep3.service;

import cl.duoc.fullstack.ep3.dto.BoletaRequest;
import cl.duoc.fullstack.ep3.dto.BoletaResponse;
import cl.duoc.fullstack.ep3.dto.ItemBoletaRequest;
import cl.duoc.fullstack.ep3.exception.RecursoNoEncontradoException;
import cl.duoc.fullstack.ep3.model.Boleta;
import cl.duoc.fullstack.ep3.model.DetalleBoleta;
import cl.duoc.fullstack.ep3.model.EstadoBoleta;
import cl.duoc.fullstack.ep3.model.Producto;
import cl.duoc.fullstack.ep3.model.Usuario;
import cl.duoc.fullstack.ep3.repository.BoletaRepository;
import cl.duoc.fullstack.ep3.repository.ProductoRepository;
import cl.duoc.fullstack.ep3.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoletaService {

    private static final String ROL_CLIENTE = "ROLE_CLIENTE";

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public BoletaService(BoletaRepository boletaRepository,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listar(Authentication autenticacion) {
        List<Boleta> boletas = esCliente(autenticacion)
                ? boletaRepository.findByClienteUsernameOrderByIdDesc(autenticacion.getName())
                : boletaRepository.findAllByOrderByIdDesc();

        return boletas.stream()
                .map(BoletaResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoletaResponse obtener(Long id, Authentication autenticacion) {
        Boleta boleta = buscar(id);

        if (esCliente(autenticacion)
                && !boleta.getCliente().getUsername().equals(autenticacion.getName())) {
            throw new AccessDeniedException("No puedes consultar boletas de otro cliente");
        }

        return BoletaResponse.desde(boleta);
    }

    @Transactional
    public BoletaResponse crear(BoletaRequest solicitud, Authentication autenticacion) {
        Usuario cliente = usuarioRepository.findByUsername(autenticacion.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Map<Long, Integer> cantidades = agruparCantidades(solicitud.items());
        Boleta boleta = new Boleta();
        boleta.setNumero(siguienteNumero());
        boleta.setCliente(cliente);
        boleta.setObservacion(limpiar(solicitud.observacion()));

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.findById(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No existe el producto con id " + entrada.getKey()));

            int cantidad = entrada.getValue();
            if (producto.getStock() < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre()
                        + ": disponible " + producto.getStock() + ", solicitado " + cantidad);
            }

            producto.setStock(producto.getStock() - cantidad);
            BigDecimal precioUnitario = producto.getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            boleta.agregarDetalle(detalle);

            total = total.add(subtotal);
        }

        boleta.setTotal(total);
        return BoletaResponse.desde(boletaRepository.save(boleta));
    }

    @Transactional
    public BoletaResponse cambiarEstado(Long id, EstadoBoleta nuevoEstado) {
        Boleta boleta = buscar(id);
        EstadoBoleta estadoActual = boleta.getEstado();

        if (estadoActual == nuevoEstado) {
            return BoletaResponse.desde(boleta);
        }
        if (estadoActual == EstadoBoleta.ANULADA) {
            throw new IllegalArgumentException("La boleta está anulada y no admite cambios");
        }
        if (nuevoEstado == EstadoBoleta.PENDIENTE) {
            throw new IllegalArgumentException("No se puede volver al estado PENDIENTE");
        }
        if (nuevoEstado == EstadoBoleta.ANULADA) {
            reponerStock(boleta);
        }

        boleta.setEstado(nuevoEstado);
        return BoletaResponse.desde(boletaRepository.save(boleta));
    }

    private Map<Long, Integer> agruparCantidades(List<ItemBoletaRequest> items) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemBoletaRequest item : items) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }
        return cantidades;
    }

    private void reponerStock(Boleta boleta) {
        for (DetalleBoleta detalle : boleta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
        }
    }

    private String siguienteNumero() {
        long correlativo = boletaRepository.count() + 1;
        String candidato;
        do {
            candidato = "BOL-%05d".formatted(correlativo++);
        } while (boletaRepository.existsByNumero(candidato));
        return candidato;
    }

    private Boleta buscar(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la boleta con id " + id));
    }

    private boolean esCliente(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> ROL_CLIENTE.equals(autoridad.getAuthority()));
    }

    private String limpiar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
