package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
@Getter
public class CostesInversion {

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal flotacion;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cavali;

    public CostesInversion(BigDecimal flotacion, BigDecimal cavali) {
        validarCostes(flotacion, cavali);
        this.flotacion = flotacion != null ? flotacion : BigDecimal.ZERO;
        this.cavali = cavali != null ? cavali : BigDecimal.ZERO;
    }

    public CostesInversion() {}

    private void validarCostes(BigDecimal flotacion, BigDecimal cavali) {
        if (flotacion != null && flotacion.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El coste de flotación no puede ser negativo");
        }
        if (cavali != null && cavali.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El coste de CAVALI no puede ser negativo");
        }
    }

    public BigDecimal calcularTotal(BigDecimal valorNominal) {
        BigDecimal total = flotacion.add(cavali);
        // Si los costes están en porcentaje, aplicar al valor nominal
        if (total.compareTo(BigDecimal.ONE) <= 0) {
            return valorNominal.multiply(total);
        }
        return total;
    }

    public boolean tieneCostes() {
        return flotacion.compareTo(BigDecimal.ZERO) > 0 || cavali.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CostesInversion that = (CostesInversion) obj;
        return Objects.equals(flotacion, that.flotacion) && Objects.equals(cavali, that.cavali);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flotacion, cavali);
    }
}
