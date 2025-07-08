package com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform;

import com.upc.ven_con_plata_backend.estimating.domain.model.commands.CreateBondValuationCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Capitalization;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Frequency;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.GraceType;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.RateType;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.CreateValuationResource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValuationCommandFromResourceAssembler {
    /**
     * Convierte un CreateValuationResource (de la API) en un CreateBondValuationCommand (para la aplicación).
     */
    public CreateBondValuationCommand toCommandFromResource(CreateValuationResource resource) {
        return new CreateBondValuationCommand(
                resource.valuationName(),
                resource.userId(),
                resource.faceValue(),
                resource.issuePrice(),
                resource.purchasePrice(),
                resource.issueDate(),
                resource.maturityDate(),
                resource.totalPeriods(),
                RateType.valueOf(resource.rateType().toUpperCase()),
                resource.rateValue(),
                resource.capitalization() != null ? Capitalization.valueOf(resource.capitalization().toUpperCase()) : null,
                Frequency.valueOf(resource.frequency().toUpperCase()),
                GraceType.valueOf(resource.graceType().toUpperCase()),
                resource.graceCapital(),
                resource.graceInterest(),
                resource.commission(),
                resource.marketRate(),
                resource.issuerStructuringCost(),
                resource.issuerPlacementCost(),
                resource.issuerCavaliCost(),
                resource. investorSabCost(),
                resource.investorCavaliCost()
        );
    }
}

/*

        @NotNull @DecimalMin(value = "0.0", message = "Cost cannot be negative")
        BigDecimal investorSabCost,

        @NotNull @DecimalMin(value = "0.0", message = "Cost cannot be negative")
        BigDecimal investorCavaliCost
 */