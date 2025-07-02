package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class BeneficioInversion {

    @Column(nullable = false)
    private BigDecimal primaVencimiento;
}
