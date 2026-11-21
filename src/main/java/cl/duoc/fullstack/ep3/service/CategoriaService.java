package cl.duoc.fullstack.ep3.service;

import cl.duoc.fullstack.ep3.dto.CategoriaRequest;
import cl.duoc.fullstack.ep3.dto.CategoriaResponse;
import cl.duoc.fullstack.ep3.exception.RecursoNoEncontradoException;
import cl.duoc.fullstack.ep3.model.Categoria;
import cl.duoc.fullstack.ep3.repository.CategoriaRepository;
import cl.duoc.fullstack.ep3.repository.ProductoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll(Sort.by("nombre")).stream()
                .map(CategoriaResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return CategoriaResponse.desde(buscar(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest solicitud) {
        String nombre = limpiar(solicitud.nombre());
        validarNombreDisponible(nombre, null);

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(limpiar(solicitud.descripcion()));
        return CategoriaResponse.desde(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest solicitud) {
        Categoria categoria = buscar(id);
        String nombre = limpiar(solicitud.nombre());
        validarNombreDisponible(nombre, id);

        categoria.setNombre(nombre);
        categoria.setDescripcion(limpiar(solicitud.descripcion()));
        return CategoriaResponse.desde(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);

        if (productoRepository.existsByCategoriaId(id)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar la categoría porque tiene productos asociados");
        }

        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría con id " + id));
    }

    private void validarNombreDisponible(String nombre, Long idActual) {
        categoriaRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            boolean esOtraCategoria = idActual == null || !existente.getId().equals(idActual);
            if (esOtraCategoria) {
                throw new IllegalArgumentException("Ya existe una categoría con el nombre " + nombre);
            }
        });
    }

    private String limpiar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
