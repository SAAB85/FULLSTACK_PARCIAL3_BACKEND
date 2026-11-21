package cl.duoc.fullstack.ep3.controller;

import cl.duoc.fullstack.ep3.dto.LoginRequest;
import cl.duoc.fullstack.ep3.dto.LoginResponse;
import cl.duoc.fullstack.ep3.dto.UsuarioResponse;
import cl.duoc.fullstack.ep3.exception.RecursoNoEncontradoException;
import cl.duoc.fullstack.ep3.model.Usuario;
import cl.duoc.fullstack.ep3.repository.UsuarioRepository;
import cl.duoc.fullstack.ep3.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioRepository usuarioRepository,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener un JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest solicitud) {
        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(solicitud.username(), solicitud.password()));

        Usuario usuario = usuarioRepository.findByUsername(autenticacion.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        String token = jwtService.generarToken(
                (UserDetails) autenticacion.getPrincipal(),
                usuario.getNombre(),
                usuario.getRol().name());

        return new LoginResponse(
                token,
                "Bearer",
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol().name(),
                jwtService.getExpiracionMs());
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar el usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse me(Authentication autenticacion) {
        Usuario usuario = usuarioRepository.findByUsername(autenticacion.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return UsuarioResponse.desde(usuario);
    }
}
