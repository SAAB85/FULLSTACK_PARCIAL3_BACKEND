package cl.duoc.fullstack.ep3.dto;

import cl.duoc.fullstack.ep3.model.Usuario;

public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        String rol,
        boolean activo) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol().name(),
                usuario.isActivo());
    }
}
