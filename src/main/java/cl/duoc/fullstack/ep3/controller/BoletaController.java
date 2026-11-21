package cl.duoc.fullstack.ep3.controller;

import cl.duoc.fullstack.ep3.dto.BoletaRequest;
import cl.duoc.fullstack.ep3.dto.BoletaResponse;
import cl.duoc.fullstack.ep3.dto.EstadoBoletaRequest;
import cl.duoc.fullstack.ep3.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Órdenes de compra de la tienda")
@SecurityRequirement(name = "bearerAuth")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @GetMapping
    @Operation(summary = "Listar boletas",
            description = "ADMIN y VENDEDOR ven todas; CLIENTE solo las propias.")
    public List<BoletaResponse> listar(Authentication autenticacion) {
        return boletaService.listar(autenticacion);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una boleta por ID",
            description = "ADMIN y VENDEDOR pueden ver cualquiera; CLIENTE solo las propias.")
    public BoletaResponse obtener(@PathVariable Long id, Authentication autenticacion) {
        return boletaService.obtener(id, autenticacion);
    }

    @PostMapping
    @Operation(summary = "Crear una boleta",
            description = "Disponible para ADMIN y CLIENTE. Descuenta stock y calcula el total en el servidor.")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public BoletaResponse crear(@Valid @RequestBody BoletaRequest solicitud,
                                Authentication autenticacion) {
        return boletaService.crear(solicitud, autenticacion);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de una boleta",
            description = "Disponible para ADMIN. Al anular se repone el stock.")
    @PreAuthorize("hasRole('ADMIN')")
    public BoletaResponse cambiarEstado(@PathVariable Long id,
                                        @Valid @RequestBody EstadoBoletaRequest solicitud) {
        return boletaService.cambiarEstado(id, solicitud.estado());
    }
}
