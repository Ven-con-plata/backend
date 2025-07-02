package com.upc.ven_con_plata_backend.estimating.domain.model.aggregates;

import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import com.upc.ven_con_plata_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter(AccessLevel.PROTECTED)
public class Bono extends AuditableAbstractAggregateRoot<Bono> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Moneda moneda;

    @Column(nullable = false)
    private BigDecimal valorNominal;

    @Column(nullable = false)
    private BigDecimal valorComercial;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @LastModifiedDate
    @Column(nullable = false)
    private BigDecimal actualizadoEn;

    @Column(nullable = false)
    private int plazoEnAnios;

    private Periodicidad frecuenciaPago;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valor", column = @Column(name = "interes_valor")),
            @AttributeOverride(name = "unidad", column = @Column(name = "interes_unidad"))
    })
    @Column(nullable = false)
    private Tasa tasaInteres;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valor", column = @Column(name = "cok_valor")),
            @AttributeOverride(name = "unidad", column = @Column(name = "cok_unidad"))
    })
    @Column(nullable = false)
    private Tasa cok;

    @Embedded
    private PeriodosGracia gracia;

    @Embedded
    private CostesInversion costesInversion;

    @Embedded
    private BeneficioInversion beneficioInversion;

    @Embedded
    private CostesInicialesDeudor costesInicialesDeudor;

    @Embedded
    private GastosPeriodicosDeudor gastosPeriodicosDeudor;

    protected Bono() {}

    public Bono(Moneda moneda, BigDecimal valorNominal, BigDecimal valorComercial, LocalDate fechaEmision,
                LocalDate fechaVencimiento, BigDecimal actualizadoEn, int plazoEnAnios, Periodicidad frecuenciaPago,
                Tasa tasaInteres, Tasa cok, PeriodosGracia gracia, CostesInversion costesInversion,
                BeneficioInversion beneficioInversion, CostesInicialesDeudor costesInicialesDeudor,
                GastosPeriodicosDeudor gastosPeriodicosDeudor) {
        this.moneda = moneda;
        this.valorNominal = valorNominal;
        this.valorComercial = valorComercial;
        this.fechaEmision = fechaEmision;
        this.fechaVencimiento = fechaVencimiento;
        this.actualizadoEn = actualizadoEn;
        this.plazoEnAnios = plazoEnAnios;
        this.frecuenciaPago = frecuenciaPago;
        this.tasaInteres = tasaInteres;
        this.cok = cok;
        this.gracia = gracia;
        this.costesInversion = costesInversion;
        this.beneficioInversion = beneficioInversion;
        this.costesInicialesDeudor = costesInicialesDeudor;
        this.gastosPeriodicosDeudor = gastosPeriodicosDeudor;
    }

    public boolean estaVencido() {
        return LocalDate.now().isAfter(this.fechaVencimiento);
    }

    public int calcularPeriodosTotales() {
        long mesesTotales = ChronoUnit.MONTHS.between(fechaEmision, fechaVencimiento);
        return switch (frecuenciaPago) {
            case MENSUAL -> (int) mesesTotales;
            case TRIMESTRAL -> (int) (mesesTotales / 3);
            case SEMESTRAL -> (int) (mesesTotales / 6);
            case ANUAL -> (int) (mesesTotales / 12);
            case BIMESTRAL -> (int) (mesesTotales / 2);
            case QUINCENAL -> (int) (mesesTotales * 2);
        };
    }

}
