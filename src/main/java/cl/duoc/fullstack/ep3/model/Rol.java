package cl.duoc.fullstack.ep3.model;

/**
 * Roles disponibles en la aplicación.
 * En Spring Security cada rol se expone como authority con prefijo ROLE_.
 */
public enum Rol {
    ADMIN,
    VENDEDOR,
    CLIENTE
}
