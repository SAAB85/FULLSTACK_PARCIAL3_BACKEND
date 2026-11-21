package cl.duoc.fullstack.ep3;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoriaFlowTest extends ApiTestBase {

    @Test
    void soloAdminGestionaCategorias() throws Exception {
        String admin = token("admin", "admin123");
        String vendedor = token("vendedor", "vendedor123");
        String nombre = "Pruebas " + System.currentTimeMillis();
        String cuerpo = """
                {"nombre":"%s","descripcion":"Categoría temporal"}
                """.formatted(nombre);

        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + vendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());

        MvcResult creada = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value(nombre))
                .andReturn();

        Number id = JsonPath.read(creada.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/categorias/" + id.longValue())
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"%s actualizada","descripcion":"Editada"}
                                """.formatted(nombre)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nombre + " actualizada"));

        mockMvc.perform(delete("/api/v1/categorias/" + id.longValue())
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void noEliminaCategoriaConProductos() throws Exception {
        String admin = token("admin", "admin123");
        String nombre = "Con productos " + System.currentTimeMillis();

        MvcResult creada = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"%s"}
                                """.formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();

        Number categoriaId = JsonPath.read(creada.getResponse().getContentAsString(), "$.id");
        String codigo = "CAT-" + System.currentTimeMillis();

        MvcResult producto = mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"%s","nombre":"Producto con categoría","categoriaId":%d,
                                 "precio":1500,"stock":2}
                                """.formatted(codigo, categoriaId.longValue())))
                .andExpect(status().isCreated())
                .andReturn();

        mockMvc.perform(delete("/api/v1/categorias/" + categoriaId.longValue())
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede eliminar la categoría porque tiene productos asociados"));

        Number productoId = JsonPath.read(producto.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(delete("/api/v1/productos/" + productoId.longValue())
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/categorias/" + categoriaId.longValue())
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
    }
}
