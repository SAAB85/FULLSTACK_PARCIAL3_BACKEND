package cl.duoc.fullstack.ep3.model;

/**
 * Estados posibles de una boleta.
 * Una boleta anulada devuelve el stock de sus productos.
 */
public enum EstadoBoleta {
    PENDIENTE,
    PAGADA,
    ANULADA
}
