package com.upc.ven_con_plata_backend.estimating.domain.model.aggregates;

import com.upc.ven_con_plata_backend.estimating.domain.model.entities.CashFlowSchedule;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import com.upc.ven_con_plata_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter(AccessLevel.PROTECTED)
public class Bono extends AuditableAbstractAggregateRoot<Bono> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Moneda moneda;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorNominal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorComercial;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime actualizadoEn;

    @Column(nullable = false)
    private int plazoEnAnios;

    private Periodicidad frecuenciaPago;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valor", column = @Column(name = "interes_valor", nullable = false)),
            @AttributeOverride(name = "unidad", column = @Column(name = "interes_unidad", nullable = false))
    })
    private Tasa tasaInteres;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valor", column = @Column(name = "cok_valor", nullable = false)),
            @AttributeOverride(name = "unidad", column = @Column(name = "cok_unidad", nullable = false))
    })
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

    private EstadoBono estado = EstadoBono.BORRADOR;

    // Relación con los cronogramas (emisor e inversor)
    @OneToMany(mappedBy = "bono", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CashFlowSchedule> cronogramas = new ArrayList<>();

    protected Bono() {}

    public Bono(Moneda moneda, BigDecimal valorNominal, BigDecimal valorComercial,
                LocalDate fechaEmision, LocalDate fechaVencimiento, Integer plazoEnAnios,
                Periodicidad frecuenciaPago, Tasa tasaInteres, Tasa cok,
                PeriodosGracia gracia, CostesInversion costesInversion,
                BeneficioInversion beneficioInversion, CostesInicialesDeudor costesInicialesDeudor,
                GastosPeriodicosDeudor gastosPeriodicosDeudor) {

        validarDatosBasicos(valorNominal, valorComercial, fechaEmision, fechaVencimiento);
        validarTasas(tasaInteres, cok);

        this.moneda = moneda;
        this.valorNominal = valorNominal;
        this.valorComercial = valorComercial;
        this.fechaEmision = fechaEmision;
        this.fechaVencimiento = fechaVencimiento;
        this.actualizadoEn = LocalDateTime.now();
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

    public void generarCronogramas() {
        if (!cronogramas.isEmpty()) {
            cronogramas.clear();
        }

        // Generar cronograma para emisor
        CashFlowSchedule cronogramaEmisor = new CashFlowSchedule(this, RolSchedule.EMISOR);
        generarEntriesParaEmisor(cronogramaEmisor);
        cronogramas.add(cronogramaEmisor);

        // Generar cronograma para inversor
        CashFlowSchedule cronogramaInversor = new CashFlowSchedule(this, RolSchedule.INVERSOR);
        generarEntriesParaInversor(cronogramaInversor);
        cronogramas.add(cronogramaInversor);

        this.actualizadoEn = LocalDateTime.now();
    }

    private void generarEntriesParaEmisor(CashFlowSchedule cronograma) {
        // Flujo inicial: ingreso por emisión (positivo para emisor)
        cronograma.agregarEntry(new CashFlowEntry(
                fechaEmision,
                valorNominal.subtract(costesInversion.calcularTotal(valorNominal)),
                TipoEntry.COSTE_INICIAL
        ));

        // Generar flujos usando metodo francés
        generarFlujosFrances(cronograma, true); // true = perspectiva emisor
    }

    private void generarEntriesParaInversor(CashFlowSchedule cronograma) {
        // Flujo inicial: egreso por compra (negativo para inversor)
        cronograma.agregarEntry(new CashFlowEntry(
                fechaEmision,
                valorComercial.negate(),
                TipoEntry.COSTE_INICIAL
        ));

        // Generar flujos usando metodo francés
        generarFlujosFrances(cronograma, false); // false = perspectiva inversor
    }

    private void generarFlujosFrances(CashFlowSchedule cronograma, boolean esEmisor) {
        int periodos = calcularPeriodosTotales();
        BigDecimal tasaPeriodica = calcularTasaPeriodica();
        BigDecimal cuotaConstante = calcularCuotaConstante(tasaPeriodica, periodos);
        BigDecimal saldoCapital = valorNominal;
        LocalDate fechaPago = fechaEmision.plusMonths(frecuenciaPago.getMesesEntrePagos());

        for (int periodo = 1; periodo <= periodos; periodo++) {
            BigDecimal interes = saldoCapital.multiply(tasaPeriodica);
            BigDecimal amortizacion = cuotaConstante.subtract(interes);

            // Aplicar período de gracia
            if (periodo <= gracia.getTotal()) {
                if (periodo <= gracia.getParcial()) {
                    // Gracia parcial: solo se pagan intereses
                    amortizacion = BigDecimal.ZERO;
                } else {
                    // Gracia total: no se paga nada
                    interes = BigDecimal.ZERO;
                    amortizacion = BigDecimal.ZERO;
                }
            }

            saldoCapital = saldoCapital.subtract(amortizacion);

            // Agregar entries según perspectiva
            if (interes.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal montoInteres = esEmisor ? interes.negate() : interes;
                cronograma.agregarEntry(new CashFlowEntry(fechaPago, montoInteres, TipoEntry.CUPON));
            }

            if (amortizacion.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal montoAmortizacion = esEmisor ? amortizacion.negate() : amortizacion;
                cronograma.agregarEntry(new CashFlowEntry(fechaPago, montoAmortizacion, TipoEntry.AMORTIZACION));
            }

            // Agregar gastos periódicos para el emisor
            if (esEmisor) {
                BigDecimal gastosPeriodicosTotal = gastosPeriodicosDeudor.calcularTotal(valorNominal, periodo);
                if (gastosPeriodicosTotal.compareTo(BigDecimal.ZERO) > 0) {
                    cronograma.agregarEntry(new CashFlowEntry(fechaPago, gastosPeriodicosTotal.negate(), TipoEntry.GASTO_PERIODICO));
                }
            }

            fechaPago = fechaPago.plusMonths(frecuenciaPago.getMesesEntrePagos());
        }

        // Beneficio al vencimiento para el inversor
        if (!esEmisor && beneficioInversion.getPrimaVencimiento().compareTo(BigDecimal.ZERO) > 0) {
            cronograma.agregarEntry(new CashFlowEntry(
                    fechaVencimiento,
                    beneficioInversion.getPrimaVencimiento(),
                    TipoEntry.BENEFICIO_VENCIMIENTO
            ));
        }
    }

    private void validarDatosBasicos(BigDecimal valorNominal, BigDecimal valorComercial,
                                     LocalDate fechaEmision, LocalDate fechaVencimiento) {
        if (valorNominal == null || valorNominal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor nominal debe ser mayor a cero");
        }
        if (valorComercial == null || valorComercial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor comercial debe ser mayor a cero");
        }
        if (fechaEmision == null || fechaVencimiento == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        if (fechaVencimiento.isBefore(fechaEmision)) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser posterior a la emisión");
        }
    }

    private void validarTasas(Tasa tasaInteres, Tasa cok) {
        if (tasaInteres == null || cok == null) {
            throw new IllegalArgumentException("Las tasas no pueden ser nulas");
        }
    }

    private void validarPuedeSerModificado() {
        if (this.estado != EstadoBono.BORRADOR) {
            throw new IllegalStateException(
                    String.format("No se puede modificar un bono en estado %s. Solo se pueden modificar bonos en estado BORRADOR.",
                            this.estado.name())
            );
        }

        if (estaVencido()) {
            throw new IllegalStateException("No se puede modificar un bono vencido");
        }
    }

    private void validarDatosFinancieros(BigDecimal valorNominal, BigDecimal valorComercial, LocalDate fechaVencimiento) {
        if (valorNominal == null || valorNominal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor nominal debe ser mayor a cero");
        }

        if (valorComercial == null || valorComercial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor comercial debe ser mayor a cero");
        }

        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser nula");
        }

        if (fechaVencimiento.isBefore(this.fechaEmision)) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser posterior a la fecha de emisión");
        }

        if (fechaVencimiento.isBefore(LocalDate.now().plusDays(30))) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser al menos 30 días en el futuro");
        }
    }

    public boolean estaVencido() {
        return LocalDate.now().isAfter(this.fechaVencimiento);
    }


    public int calcularPeriodosTotales() {
        long mesesTotales = ChronoUnit.MONTHS.between(fechaEmision, fechaVencimiento);
        return switch (frecuenciaPago) {
            case MENSUAL -> (int) mesesTotales;
            case BIMESTRAL -> (int) (mesesTotales / 2);
            case TRIMESTRAL -> (int) (mesesTotales / 3);
            case SEMESTRAL -> (int) (mesesTotales / 6);
            case ANUAL -> (int) (mesesTotales / 12);
            case QUINCENAL -> (int) (mesesTotales * 2);
        };
    }

    public BigDecimal calcularTasaPeriodica() {
        BigDecimal tasaAnual = tasaInteres.getValor();
        return switch (frecuenciaPago) {
            case ANUAL -> tasaAnual;
            case SEMESTRAL -> tasaAnual.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
            case TRIMESTRAL -> tasaAnual.divide(BigDecimal.valueOf(4), 10, RoundingMode.HALF_UP);
            case BIMESTRAL -> tasaAnual.divide(BigDecimal.valueOf(6), 10, RoundingMode.HALF_UP);
            case MENSUAL -> tasaAnual.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
            case QUINCENAL -> tasaAnual.divide(BigDecimal.valueOf(24), 10, RoundingMode.HALF_UP);
        };
    }

    private BigDecimal calcularCuotaConstante(BigDecimal tasaPeriodica, int periodos) {
        if (tasaPeriodica.compareTo(BigDecimal.ZERO) == 0) {
            return valorNominal.divide(BigDecimal.valueOf(periodos), 2, RoundingMode.HALF_UP);
        }
        // Fórmula de la cuota constante usando el metodo francés
        BigDecimal factor = BigDecimal.ONE.add(tasaPeriodica).pow(periodos);
        return valorNominal.multiply(tasaPeriodica).multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }

    // Getters
    public List<CashFlowSchedule> getCronogramas() {
        return new ArrayList<>(cronogramas);
    }

    public CashFlowSchedule getCronogramaEmisor() {
        return cronogramas.stream()
                .filter(c -> c.getRol() == RolSchedule.EMISOR)
                .findFirst()
                .orElse(null);
    }

    public CashFlowSchedule getCronogramaInversor() {
        return cronogramas.stream()
                .filter(c -> c.getRol() == RolSchedule.INVERSOR)
                .findFirst()
                .orElse(null);
    }

    private Integer calcularPlazoEnAnios(LocalDate fechaInicio, LocalDate fechaFin) {
        return (int) ChronoUnit.YEARS.between(fechaInicio, fechaFin);
    }

    public boolean puedeSerModificado() {
        return this.estado == EstadoBono.BORRADOR && !estaVencido();
    }

    public void activar() {
        if (this.estado != EstadoBono.BORRADOR) {
            throw new IllegalStateException("Solo se pueden activar bonos en estado borrador");
        }
        this.estado = EstadoBono.ACTIVO;
        this.actualizadoEn = LocalDateTime.now();
    }

    // Metodo de conveniencia para saber que siempre usa metodo francés
    public String getMetodoAmortizacion() {
        return "Método Francés";
    }

    public void actualizarDatosFinancieros(BigDecimal valorNominal, BigDecimal valorComercial, LocalDate fechaVencimiento) {
        // Validar los datos antes de actualizarlos
        validarDatosFinancieros(valorNominal, valorComercial, fechaVencimiento);

        // Actualizar los campos del bono
        this.valorNominal = valorNominal;
        this.valorComercial = valorComercial;
        this.fechaVencimiento = fechaVencimiento;

        // Puedes agregar un control de cambios aquí si es necesario
        this.actualizadoEn = LocalDateTime.now();
    }

    public void actualizarTasas(BigDecimal tasaInteres, Periodicidad periodicidadInteres, BigDecimal cok, Periodicidad periodicidadCok) {
        // Validar las tasas antes de actualizarlas
        validarTasas(new Tasa(tasaInteres, periodicidadInteres), new Tasa(cok, periodicidadCok));

        // Actualizar las tasas
        this.tasaInteres = new Tasa(tasaInteres, periodicidadInteres);
        this.cok = new Tasa(cok, periodicidadCok);

        // Puedes agregar un control de cambios aquí si es necesario
        this.actualizadoEn = LocalDateTime.now();
    }

    public void actualizarPeriodosGracia(Integer periodosGraciaTotal, Integer periodosGraciaParcial) {
        // Validar los periodos de gracia antes de actualizarlos
        if (periodosGraciaTotal < 0 || periodosGraciaParcial < 0 || periodosGraciaParcial > periodosGraciaTotal) {
            throw new IllegalArgumentException("Los períodos de gracia no son válidos");
        }

        // Actualizar los períodos de gracia
        this.gracia = new PeriodosGracia(periodosGraciaTotal, periodosGraciaParcial);

        // Puedes agregar un control de cambios aquí si es necesario
        this.actualizadoEn = LocalDateTime.now();
    }


}
