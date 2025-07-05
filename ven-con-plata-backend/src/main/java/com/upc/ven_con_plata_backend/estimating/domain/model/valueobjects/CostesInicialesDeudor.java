package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
@Getter
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

        return valorNominal.subtract(total);
    }
}
