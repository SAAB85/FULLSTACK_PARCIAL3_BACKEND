package cl.duoc.fullstack.ep3;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowTest extends ApiTestBase {

    @Test
    void loginEntregaTokenYRechazaCredencialesInvalidas() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void productosExigenToken() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/productos")
                        .header("Authorization", "Bearer " + token("cliente", "cliente123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].codigo").isNotEmpty())
                .andExpect(jsonPath("$[0].categoria").isNotEmpty());
    }

    @Test
    void soloAdminPuedeListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + token("cliente", "cliente123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + token("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='admin')]").exists());
    }

    @Test
    void vendedorSoloConsultaElCatalogo() throws Exception {
        String admin = token("admin", "admin123");
        String vendedor = token("vendedor", "vendedor123");
        long categoriaId = primeraCategoria(admin);
        String codigo = "TEST-" + System.currentTimeMillis();
        String cuerpo = """
                {"codigo":"%s","nombre":"Producto de prueba","descripcion":"Temporal",
                 "categoriaId":%d,"precio":1000,"stock":3}
                """.formatted(codigo, categoriaId);

        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + vendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());

        MvcResult creado = mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andReturn();

        Number id = JsonPath.read(creado.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/productos/" + id.longValue())
                        .header("Authorization", "Bearer " + vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigo));

        mockMvc.perform(delete("/api/v1/productos/" + id.longValue())
                        .header("Authorization", "Bearer " + vendedor))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/productos/" + id.longValue())
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
    }
}
