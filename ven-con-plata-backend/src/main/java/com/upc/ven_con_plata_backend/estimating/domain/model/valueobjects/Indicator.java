package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

@Embeddable
public class Indicator {

    @Column(nullable=false, length=50)
    private String nombre;

    @Column(nullable=false, precision=18, scale=6)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=12)
    private Unidad unidad;

    protected Indicator(){}

    public Indicator(String nombre, BigDecimal valor, Unidad unidad) {
        this.nombre = nombre;
        this.valor = valor;
        this.unidad = unidad;
    }
}
