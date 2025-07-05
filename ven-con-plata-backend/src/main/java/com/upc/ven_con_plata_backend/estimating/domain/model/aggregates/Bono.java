package com.upc.ven_con_plata_backend.estimating.domain.model.aggregates;

import com.upc.ven_con_plata_backend.estimating.domain.model.commands.CreateBonoCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.entities.CashFlowSchedule;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import com.upc.ven_con_plata_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter(AccessLevel.PROTECTED)
public class Bono extends AuditableAbstractAggregateRoot<Bono> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Currency moneda;


    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorNominal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorComercial;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private int plazoEnAnios;

    @Enumerated(EnumType.STRING)
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

    public Bono(CreateBonoCommand cmd) {

        validarDatosBasicos(cmd.valorNominal(), cmd.valorComercial());
        validarTasas(cmd.tasaInteres(), cmd.cok());

        this.moneda = Currency.valueOf(cmd.moneda());
        this.valorNominal = cmd.valorNominal();
        this.valorComercial = cmd.valorComercial();
        this.plazoEnAnios = cmd.plazoEnAnios();
        // periocidad
        this.frecuenciaPago = cmd.frecuenciaPago();
        // periocidad interes
        this.tasaInteres = new Tasa(cmd.tasaInteres(), cmd.periodicidadInteres());
        // periocidad cok
        this.cok = new Tasa(cmd.cok(), cmd.periodicidadCok());

        this.gracia = new PeriodosGracia(cmd.periodosGraciaTotal(), cmd.periodosGraciaParcial());


        this.costesInversion = new CostesInversion(cmd.costeFlotacion(), cmd.costeCavali());
        this.beneficioInversion = new BeneficioInversion(cmd.primaVencimiento());
        this.costesInicialesDeudor = new CostesInicialesDeudor(cmd.costesNotariales(),cmd.costesRegistrales(), cmd.costesTasacion(), cmd.comisionEstudio(),cmd.comisionActivacion());
        this.gastosPeriodicosDeudor = new GastosPeriodicosDeudor(cmd.seguroDesgravamen(), cmd.seguroRiesgo(), cmd.comisionPeriodica(), cmd.portes(), cmd.gastosAdministrativos());

        //calcular vencimiento
        this.fechaVencimiento = LocalDate.now().plusYears(plazoEnAnios);

        // Genera en cascada los dos cronogramas (Emisor e Inversor)
        this.generarCashflowEIndicadores();
    }

    private void validarDatosBasicos(BigDecimal valorNominal, BigDecimal valorComercial) {
        if (valorNominal == null || valorNominal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor nominal debe ser mayor a cero");
        }
        if (valorComercial == null || valorComercial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor comercial debe ser mayor a cero");
        }
    }

    private void validarTasas(BigDecimal tasaInteres, BigDecimal cok) {
        if (tasaInteres == null || cok == null) {
            throw new IllegalArgumentException("Las tasas no pueden ser nulas");
        }
    }


    public void generarCashflowEIndicadores() {
        if (!this.cronogramas.isEmpty()) {
            this.cronogramas.clear();
        }
        // Generar cronograma para emisor
        CashFlowSchedule cashFlowScheduleEmisor = new CashFlowSchedule(this, RolSchedule.EMISOR);
        this.cronogramas.add(
                cashFlowScheduleEmisor
        );
        // Generar cronograma para inversor
        /*this.cronogramas.add(
                CashFlowSchedule.crearPara(this, RolSchedule.INVERSOR)
        );*/
    }
/*
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
    }

    public void actualizarTasas(BigDecimal tasaInteres, Periodicidad periodicidadInteres, BigDecimal cok, Periodicidad periodicidadCok) {
        // Validar las tasas antes de actualizarlas
        validarTasas(new Tasa(tasaInteres, periodicidadInteres), new Tasa(cok, periodicidadCok));

        // Actualizar las tasas
        this.tasaInteres = new Tasa(tasaInteres, periodicidadInteres);
        this.cok = new Tasa(cok, periodicidadCok);

        // Puedes agregar un control de cambios aquí si es necesario
    }

    public void actualizarPeriodosGracia(Integer periodosGraciaTotal, Integer periodosGraciaParcial) {
        // Validar los periodos de gracia antes de actualizarlos
        if (periodosGraciaTotal < 0 || periodosGraciaParcial < 0 || periodosGraciaParcial > periodosGraciaTotal) {
            throw new IllegalArgumentException("Los períodos de gracia no son válidos");
        }

        // Actualizar los períodos de gracia
        this.gracia = new PeriodosGracia(periodosGraciaTotal, periodosGraciaParcial);

        // Puedes agregar un control de cambios aquí si es necesario
    }

*/
}
