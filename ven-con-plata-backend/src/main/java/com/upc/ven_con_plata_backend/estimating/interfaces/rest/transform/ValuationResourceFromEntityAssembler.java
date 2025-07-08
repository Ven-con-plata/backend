package com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform;

import com.upc.ven_con_plata_backend.estimating.application.internal.dtos.ValuationResponseDto;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.CashFlowPeriodResource;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.ValuationResource;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

@Configuration
public class ValuationResourceFromEntityAssembler {
    public ValuationResource toResourceFromDto(ValuationResponseDto dto) {
        var cashFlowResources = dto.cashFlow().stream()
                .map(period -> new CashFlowPeriodResource(
                        period.number(),
                        period.gracePeriodState().name(),
                        period.initialBalance().amount(),
                        period.interest().amount(),
                        period.coupon().amount(),
                        period.amortization().amount(),
                        period.finalBalance().amount(),
                        period.cashFlow().amount()
                )).collect(Collectors.toList());

        return new ValuationResource(
                dto.id(),
                dto.valuationName(),
                dto.userId(),
                dto.parameters().faceValue().amount(),
                dto.parameters().issueDate(),
                dto.metrics().tcea(),
                dto.metrics().trea(),
                dto.metrics().macaulayDuration(),
                dto.metrics().modifiedDuration(),
                dto.metrics().convexity(),
                dto.metrics().dirtyPrice().amount(),
                dto.metrics().cleanPrice().amount(),
                cashFlowResources
        );
    }
}
