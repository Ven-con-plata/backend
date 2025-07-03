package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class CostesInicialesDeudor {

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal notariales;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal registrales;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal tasacion;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal comisionEstudio;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal comisionActivacion;

    public CostesInicialesDeudor(){}

    public CostesInicialesDeudor(BigDecimal notariales, BigDecimal registrales,
                                 BigDecimal tasacion, BigDecimal comisionEstudio,
                                 BigDecimal comisionActivacion) {
        validarCostes(notariales, registrales, tasacion, comisionEstudio, comisionActivacion);
        this.notariales = notariales != null ? notariales : BigDecimal.ZERO;
        this.registrales = registrales != null ? registrales : BigDecimal.ZERO;
        this.tasacion = tasacion != null ? tasacion : BigDecimal.ZERO;
        this.comisionEstudio = comisionEstudio != null ? comisionEstudio : BigDecimal.ZERO;
        this.comisionActivacion = comisionActivacion != null ? comisionActivacion : BigDecimal.ZERO;
    }

    private void validarCostes(BigDecimal... costes) {
        for (BigDecimal coste : costes) {
            if (coste != null && coste.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Los costes no pueden ser negativos");
            }
        }
    }

    public BigDecimal calcularTotal(BigDecimal valorNominal) {
        BigDecimal total = notariales.add(registrales).add(tasacion)
                .add(comisionEstudio).add(comisionActivacion);

        // Si el total es menor a 1, asumimos que está en porcentaje
        if (total.compareTo(BigDecimal.ONE) <= 0) {
            return valorNominal.multiply(total);
        }
        return total;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CostesInicialesDeudor that = (CostesInicialesDeudor) obj;
        return Objects.equals(notariales, that.notariales) &&
                Objects.equals(registrales, that.registrales) &&
                Objects.equals(tasacion, that.tasacion) &&
                Objects.equals(comisionEstudio, that.comisionEstudio) &&
                Objects.equals(comisionActivacion, that.comisionActivacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notariales, registrales, tasacion, comisionEstudio, comisionActivacion);
    }
}
