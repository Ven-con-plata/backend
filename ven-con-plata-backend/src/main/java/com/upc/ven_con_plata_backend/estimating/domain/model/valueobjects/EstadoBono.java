package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

public enum EstadoBono {
    BORRADOR("Borrador", "Bono en proceso de creación"),
    ACTIVO("Activo", "Bono publicado y disponible"),
    VENCIDO("Vencido", "Bono que ha alcanzado su fecha de vencimiento"),
    CANCELADO("Cancelado", "Bono cancelado antes del vencimiento");

    private final String nombre;
    private final String descripcion;

    EstadoBono(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}
