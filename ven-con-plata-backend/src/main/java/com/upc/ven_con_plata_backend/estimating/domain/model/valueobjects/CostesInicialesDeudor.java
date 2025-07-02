package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class CostesInicialesDeudor {

    @Column(nullable = false)
    private BigDecimal notariales;

    @Column(nullable = false)
    private BigDecimal registrales;

    @Column(nullable = false)
    private BigDecimal tasacion;

    @Column(nullable = false)
    private BigDecimal comisionEstudio;

    @Column(nullable = false)
    private BigDecimal comisionActivacion;
}
