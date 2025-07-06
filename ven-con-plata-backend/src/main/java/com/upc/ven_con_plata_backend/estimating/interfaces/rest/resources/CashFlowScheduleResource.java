package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import com.upc.ven_con_plata_backend.estimating.domain.model.entities.CashFlowSchedule;

import java.math.BigInteger;
import java.util.List;

public record CashFlowScheduleResource(
        Long id,
        String rol,
        List<CashFlowEntryResource> entries,
        List<IndicatorResource> indicadores
) {
    public static CashFlowScheduleResource fromEntity(CashFlowSchedule s) {
        return new CashFlowScheduleResource(
                s.getId(),
                s.getRol().name(),
                s.getEntries().stream()
                        .map(CashFlowEntryResource::fromEntity)
                        .toList(),
                s.getIndicadores().stream()
                        .map(IndicatorResource::fromEntity)
                        .toList()
        );
    }
}
