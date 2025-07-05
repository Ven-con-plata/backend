package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Embeddable
public class BeneficioInversion {

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal primaVencimiento;

    protected BeneficioInversion() {}

    public BeneficioInversion(BigDecimal primaVencimiento) {
        this.primaVencimiento = primaVencimiento != null ? primaVencimiento : BigDecimal.ZERO;
    }
}
