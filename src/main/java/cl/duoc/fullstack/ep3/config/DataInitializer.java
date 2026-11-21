package cl.duoc.fullstack.ep3.config;

import cl.duoc.fullstack.ep3.model.Categoria;
import cl.duoc.fullstack.ep3.model.Producto;
import cl.duoc.fullstack.ep3.model.Rol;
import cl.duoc.fullstack.ep3.model.Usuario;
import cl.duoc.fullstack.ep3.repository.CategoriaRepository;
import cl.duoc.fullstack.ep3.repository.ProductoRepository;
import cl.duoc.fullstack.ep3.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * Datos de demostración para partir con la aplicación funcionando.
 * Solo inserta registros cuando las tablas están vacías.
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner cargarDatos(UsuarioRepository usuarios,
                                         CategoriaRepository categorias,
                                         ProductoRepository productos,
                                         PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarios.count() == 0) {
                usuarios.save(crearUsuario("admin", "admin123", "Administrador", Rol.ADMIN, passwordEncoder));
                usuarios.save(crearUsuario("vendedor", "vendedor123", "Vendedor de tienda", Rol.VENDEDOR, passwordEncoder));
                usuarios.save(crearUsuario("cliente", "cliente123", "Cliente de prueba", Rol.CLIENTE, passwordEncoder));
            }

            Categoria electricas = obtenerOCrear(categorias,
                    "Herramientas eléctricas", "Taladros, esmeriles y otras máquinas.");
            Categoria manuales = obtenerOCrear(categorias,
                    "Herramientas manuales", "Herramientas de uso directo con la mano.");
            Categoria medicion = obtenerOCrear(categorias,
                    "Medición", "Instrumentos para medir y verificar.");
            Categoria seguridad = obtenerOCrear(categorias,
                    "Seguridad", "Equipo de protección personal.");

            if (productos.count() == 0) {
                productos.save(crearProducto("FER-001", "Taladro percutor 750W", "Taladro con velocidad variable y mango ergonómico.", electricas, "45990", 12));
                productos.save(crearProducto("FER-002", "Juego de destornilladores", "Set de 6 piezas con puntas planas y Phillips.", manuales, "12990", 30));
                productos.save(crearProducto("FER-003", "Martillo de carpintero", "Martillo de acero con mango de fibra de vidrio.", manuales, "9990", 25));
                productos.save(crearProducto("FER-004", "Cinta métrica 5 m", "Cinta métrica retráctil con freno y clip.", medicion, "4990", 50));
                productos.save(crearProducto("FER-005", "Guantes de seguridad", "Guantes anti-deslizantes para trabajo pesado.", seguridad, "3490", 40));
            }
        };
    }

    private Usuario crearUsuario(String username, String password, String nombre, Rol rol,
                                 PasswordEncoder passwordEncoder) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombre(nombre);
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }

    private Categoria obtenerOCrear(CategoriaRepository categorias, String nombre, String descripcion) {
        return categorias.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            Categoria categoria = new Categoria();
            categoria.setNombre(nombre);
            categoria.setDescripcion(descripcion);
            return categorias.save(categoria);
        });
    }

    private Producto crearProducto(String codigo, String nombre, String descripcion,
                                   Categoria categoria, String precio, int stock) {
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        return producto;
    }
}
