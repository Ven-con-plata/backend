package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Embeddable
@Getter
public class GastosPeriodicosDeudor {

    private static final MathContext MATH_CTX = new MathContext(10, RoundingMode.HALF_UP);

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

    public BigDecimal calcularTotal(BigDecimal saldoPrevio, BigDecimal valorComercial) {
        // 2) Seguro de desgravamen: % sobre saldo pendiente
        //    (asegúrate de que gp.getSeguroDesgravamen() esté en [0,1], p.ej. 0.005 = 0.5%)
        BigDecimal seguroDesgravamen = saldoPrevio
                .multiply(this.seguroDesgravamen.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));

        // 3) Seguro “contra todo riesgo”: % sobre el precio de venta del bien
        //    Usamos valorComercial como precio de venta
        BigDecimal seguroTodoRiesgo = valorComercial
                .multiply(this.seguroRiesgo.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));

        return comisionPeriodica
                .add(this.portes)
                .add(this.gastosAdministrativos)
                .add(seguroDesgravamen)
                .add(seguroTodoRiesgo);
    }
}
