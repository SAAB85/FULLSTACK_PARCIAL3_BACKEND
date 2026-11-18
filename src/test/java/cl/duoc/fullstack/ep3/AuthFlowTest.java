package cl.duoc.fullstack.ep3;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ep3test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginEntregaTokenYRechazaCredencialesInvalidas() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void productosExigenToken() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/productos")
                        .header("Authorization", "Bearer " + token("cliente", "cliente123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].codigo").isNotEmpty());
    }

    @Test
    void soloAdminPuedeListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token("cliente", "cliente123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='admin')]").exists());
    }

    @Test
    void vendedorCreaProductosPeroNoLosElimina() throws Exception {
        String codigo = "TEST-" + System.currentTimeMillis();
        String cuerpo = """
                {"codigo":"%s","nombre":"Producto de prueba","descripcion":"Temporal",
                 "categoria":"Pruebas","precio":1000,"stock":3}
                """.formatted(codigo);

        MvcResult creado = mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token("vendedor", "vendedor123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andReturn();

        Number id = JsonPath.read(creado.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/productos/" + id.longValue())
                        .header("Authorization", "Bearer " + token("vendedor", "vendedor123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/productos/" + id.longValue())
                        .header("Authorization", "Bearer " + token("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    private String token(String username, String password) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }
}
