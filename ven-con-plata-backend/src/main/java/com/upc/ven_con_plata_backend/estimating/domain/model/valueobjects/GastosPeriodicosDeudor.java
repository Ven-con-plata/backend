package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class GastosPeriodicosDeudor {

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal seguroDesgravamen;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal seguroRiesgo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal comisionPeriodica;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal portes;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal gastosAdministrativos;

    protected GastosPeriodicosDeudor() {}

    public GastosPeriodicosDeudor(BigDecimal seguroDesgravamen, BigDecimal seguroRiesgo,
                                  BigDecimal comisionPeriodica, BigDecimal portes,
                                  BigDecimal gastosAdministrativos) {
        this.seguroDesgravamen = seguroDesgravamen != null ? seguroDesgravamen : BigDecimal.ZERO;
        this.seguroRiesgo = seguroRiesgo != null ? seguroRiesgo : BigDecimal.ZERO;
        this.comisionPeriodica = comisionPeriodica != null ? comisionPeriodica : BigDecimal.ZERO;
        this.portes = portes != null ? portes : BigDecimal.ZERO;
        this.gastosAdministrativos = gastosAdministrativos != null ? gastosAdministrativos : BigDecimal.ZERO;
    }

    public BigDecimal calcularTotal(BigDecimal valorNominal, int periodo) {
        BigDecimal total = seguroDesgravamen.add(seguroRiesgo)
                .add(comisionPeriodica).add(portes)
                .add(gastosAdministrativos);

        // Si el total es menor a 1, asumimos que está en porcentaje del valor nominal
        if (total.compareTo(BigDecimal.ONE) <= 0) {
            return valorNominal.multiply(total);
        }
        return total;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GastosPeriodicosDeudor that = (GastosPeriodicosDeudor) obj;
        return Objects.equals(seguroDesgravamen, that.seguroDesgravamen) &&
                Objects.equals(seguroRiesgo, that.seguroRiesgo) &&
                Objects.equals(comisionPeriodica, that.comisionPeriodica) &&
                Objects.equals(portes, that.portes) &&
                Objects.equals(gastosAdministrativos, that.gastosAdministrativos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seguroDesgravamen, seguroRiesgo, comisionPeriodica, portes, gastosAdministrativos);
    }
}
