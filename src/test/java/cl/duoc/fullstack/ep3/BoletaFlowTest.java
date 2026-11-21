package cl.duoc.fullstack.ep3;

import cl.duoc.fullstack.ep3.model.Rol;
import cl.duoc.fullstack.ep3.model.Usuario;
import cl.duoc.fullstack.ep3.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BoletaFlowTest extends ApiTestBase {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void asegurarClienteSecundario() {
        if (!usuarioRepository.existsByUsername("cliente2")) {
            Usuario cliente = new Usuario();
            cliente.setUsername("cliente2");
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            cliente.setNombre("Cliente secundario");
            cliente.setRol(Rol.CLIENTE);
            cliente.setActivo(true);
            usuarioRepository.save(cliente);
        }
    }

    @Test
    void clienteCreaBoletaYDescuentaStock() throws Exception {
        String admin = token("admin", "admin123");
        String cliente = token("cliente", "cliente123");
        long productoId = crearProducto(admin, 10, "2500");

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoBoleta(productoId, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.cliente").value("cliente"))
                .andExpect(jsonPath("$.total").value(5000.0))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(2))
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(2500.0))
                .andExpect(jsonPath("$.detalles[0].subtotal").value(5000.0));

        assertEquals(8, stock(admin, productoId));
    }

    @Test
    void vendedorSoloVisualizaBoletas() throws Exception {
        String admin = token("admin", "admin123");
        String cliente = token("cliente", "cliente123");
        String vendedor = token("vendedor", "vendedor123");
        long productoId = crearProducto(admin, 5, "1000");

        MvcResult creada = mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoBoleta(productoId, 1)))
                .andExpect(status().isCreated())
                .andReturn();

        Number boletaId = JsonPath.read(creada.getResponse().getContentAsString(), "$.id");
        String numero = JsonPath.read(creada.getResponse().getContentAsString(), "$.numero");

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", "Bearer " + vendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoBoleta(productoId, 1)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/boletas")
                        .header("Authorization", "Bearer " + vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.numero=='%s')]".formatted(numero)).exists());

        mockMvc.perform(get("/api/v1/boletas/" + boletaId.longValue())
                        .header("Authorization", "Bearer " + vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(numero));
    }

    @Test
    void clienteNoPuedeVerBoletasDeOtroCliente() throws Exception {
        String admin = token("admin", "admin123");
        String cliente = token("cliente", "cliente123");
        String cliente2 = token("cliente2", "cliente123");
        long productoId = crearProducto(admin, 5, "1000");

        MvcResult creada = mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoBoleta(productoId, 1)))
                .andExpect(status().isCreated())
                .andReturn();

        Number boletaId = JsonPath.read(creada.getResponse().getContentAsString(), "$.id");
        String numero = JsonPath.read(creada.getResponse().getContentAsString(), "$.numero");

        mockMvc.perform(get("/api/v1/boletas/" + boletaId.longValue())
                        .header("Authorization", "Bearer " + cliente2))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/boletas")
                        .header("Authorization", "Bearer " + cliente2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.numero=='%s')]".formatted(numero)).isEmpty());

        mockMvc.perform(get("/api/v1/boletas/" + boletaId.longValue())
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(numero));
    }

    @Test
    void stockInsuficienteRechazaLaBoleta() throws Exception {
        String admin = token("admin", "admin123");
        String cliente = token("cliente", "cliente123");
        long productoId = crearProducto(admin, 1, "1000");

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoBoleta(productoId, 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Stock insuficiente")));

        assertEquals(1, stock(admin, productoId));
    }

    @Test
    void adminPagaYAnulaBoletaReponiendoStock() throws Exception {
        String admin = token("admin", "admin123");
        String cliente = token("cliente", "cliente123");
        long productoId = crearProducto(admin, 5, "1000");

        MvcResult creada = mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoBoleta(productoId, 2)))
                .andExpect(status().isCreated())
                .andReturn();

        Number boletaId = JsonPath.read(creada.getResponse().getContentAsString(), "$.id");
        assertEquals(3, stock(admin, productoId));

        mockMvc.perform(patch("/api/v1/boletas/" + boletaId.longValue() + "/estado")
                        .header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"PAGADA"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/boletas/" + boletaId.longValue() + "/estado")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"PAGADA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"));

        mockMvc.perform(patch("/api/v1/boletas/" + boletaId.longValue() + "/estado")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"ANULADA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADA"));

        assertEquals(5, stock(admin, productoId));

        mockMvc.perform(patch("/api/v1/boletas/" + boletaId.longValue() + "/estado")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"PAGADA"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La boleta está anulada y no admite cambios"));
    }

    private long crearProducto(String token, int stock, String precio) throws Exception {
        long categoriaId = primeraCategoria(token);
        String codigo = "P-" + System.nanoTime();

        MvcResult creado = mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"%s","nombre":"Producto de boleta","descripcion":"Temporal",
                                 "categoriaId":%d,"precio":%s,"stock":%d}
                                """.formatted(codigo, categoriaId, precio, stock)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(creado.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private int stock(String token, long productoId) throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/v1/productos/" + productoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        Number stock = JsonPath.read(resultado.getResponse().getContentAsString(), "$.stock");
        return stock.intValue();
    }

    private String cuerpoBoleta(long productoId, int cantidad) {
        return """
                {"items":[{"productoId":%d,"cantidad":%d}],"observacion":"Compra de prueba"}
                """.formatted(productoId, cantidad);
    }
}
