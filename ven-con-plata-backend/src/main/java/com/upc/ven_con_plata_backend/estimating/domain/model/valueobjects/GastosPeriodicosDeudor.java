package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class GastosPeriodicosDeudor {

    @Column(nullable = false)
    private BigDecimal seguroDesgravamen;

    @Column(nullable = false)
    private BigDecimal seguroRiesgo;

    @Column(nullable = false)
    private BigDecimal comisionPeriodica;

    @Column(nullable = false)
    private BigDecimal portes;

    @Column(nullable = false)
    private BigDecimal gastosAdministrativos;
}
