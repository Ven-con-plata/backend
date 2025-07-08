package com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.GracePeriodState;

import java.math.BigDecimal;
import java.util.Currency;

@Entity
@Table(name = "cashflow_periods") // Corregido el nombre de la tabla
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowPeriodJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int periodNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GracePeriodState gracePeriodState;

    // --- Campos aplanados del VO Money ---

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal initialBalanceAmount;
    @Column(nullable = false, length = 3)
    private Currency initialBalanceCurrency;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal interestAmount;
    @Column(nullable = false, length = 3)
    private Currency interestCurrency;

    // CAMPOS AÑADIDOS
    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal couponAmount;
    @Column(nullable = false, length = 3)
    private Currency couponCurrency;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amortizationAmount;
    @Column(nullable = false, length = 3)
    private Currency amortizationCurrency;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal finalBalanceAmount;
    @Column(nullable = false, length = 3)
    private Currency finalBalanceCurrency;
    // FIN DE CAMPOS AÑADIDOS

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal cashFlowAmount; // Renombrado de cashFlowAmount a cashflowAmount
    @Column(nullable = false, length = 3)
    private Currency cashFlowCurrency; // Renombrado de cashFlowCurrency a cashflowCurrency
}