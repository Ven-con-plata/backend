package com.upc.ven_con_plata_backend.estimating.domain.model.aggregates;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.UpdateBondValuationCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.entities.CashFlowPeriod;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import com.upc.ven_con_plata_backend.estimating.domain.services.FinancialCalculatorService;
import com.upc.ven_con_plata_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.List;

@Getter
@Setter(AccessLevel.PROTECTED) // Solo el mapper de persistencia debe poder setear el ID y otros campos
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class  BondValuation extends AuditableAbstractAggregateRoot<BondValuation> {

    private String valuationName;
    private UserId userId;
    private BondParameters parameters;
    private FinancialMetrics metrics;
    private List<CashFlowPeriod> cashFlow;

    // El constructor es público
    // La responsabilidad de esta clase es "ser" una valoración, no "crearse a sí misma".
    public BondValuation(String valuationName, UserId userId, BondParameters parameters) {
        this.valuationName = valuationName;
        this.userId = userId;
        this.parameters = parameters;
    }

    /**
     * Metodo para completar el aggregate con los resultados del cálculo.
     * Este metodo será llamado por el servicio de aplicación después de usar el servicio de cálculo.
     */
    public void completeValuation(FinancialMetrics metrics, List<CashFlowPeriod> cashFlow) {
        this.metrics = metrics;
        this.cashFlow = cashFlow;
    }

    public void update(UpdateBondValuationCommand command, FinancialCalculatorService calculator) {
        if (command == null || calculator == null) {
            throw new IllegalArgumentException("Command and calculator cannot be null");
        }
        // Opcional: Validar que el ID del comando coincida con el ID del aggregate
        // if (!this.getId().equals(command.valuationId())) { ... }

        this.valuationName = command.valuationName();
        this.parameters = new BondParameters(command);

        // Recalcular para mantener la consistencia
        recalculate(calculator);
    }

    private void recalculate(FinancialCalculatorService calculator) {
        var calculationResult = calculator.calculate(this.parameters);
        this.metrics = calculationResult.metrics();
        this.cashFlow = calculationResult.cashFlow();
    }

    /**
     * Metodo para actualizar los parámetros.
     */
    public void updateParameters(String valuationName, BondParameters parameters) {
        this.valuationName = valuationName;
        this.parameters = parameters;
        // Invalida los resultados anteriores. La capa de aplicación deberá recalcular.
        this.metrics = null;
        this.cashFlow = null;
    }
}