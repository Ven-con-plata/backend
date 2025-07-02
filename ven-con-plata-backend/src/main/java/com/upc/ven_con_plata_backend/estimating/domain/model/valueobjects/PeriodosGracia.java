package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PeriodosGracia {

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private int parcial;
}
