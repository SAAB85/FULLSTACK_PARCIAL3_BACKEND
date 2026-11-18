package cl.duoc.fullstack.ep3.service;

import cl.duoc.fullstack.ep3.dto.ProductoRequest;
import cl.duoc.fullstack.ep3.dto.ProductoResponse;
import cl.duoc.fullstack.ep3.exception.RecursoNoEncontradoException;
import cl.duoc.fullstack.ep3.model.Producto;
import cl.duoc.fullstack.ep3.repository.ProductoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll(Sort.by("id")).stream()
                .map(ProductoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.desde(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest solicitud) {
        if (productoRepository.existsByCodigo(solicitud.codigo())) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + solicitud.codigo());
        }

        Producto producto = new Producto();
        aplicarCambios(producto, solicitud);
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest solicitud) {
        Producto producto = buscar(id);

        if (!producto.getCodigo().equals(solicitud.codigo())
                && productoRepository.existsByCodigo(solicitud.codigo())) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + solicitud.codigo());
        }

        aplicarCambios(producto, solicitud);
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        productoRepository.delete(buscar(id));
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    private void aplicarCambios(Producto producto, ProductoRequest solicitud) {
        producto.setCodigo(solicitud.codigo());
        producto.setNombre(solicitud.nombre());
        producto.setDescripcion(solicitud.descripcion());
        producto.setCategoria(solicitud.categoria());
        producto.setPrecio(solicitud.precio());
        producto.setStock(solicitud.stock());
    }
}
