package cl.duoc.fullstack.ep3.dto;

public record LoginResponse(
        String token,
        String tipo,
        String username,
        String nombre,
        String rol,
        long expiraEn) {
}
