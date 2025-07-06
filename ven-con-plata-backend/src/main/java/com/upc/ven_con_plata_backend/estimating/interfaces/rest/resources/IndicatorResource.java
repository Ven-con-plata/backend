package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Indicator;

import java.math.BigDecimal;

public record IndicatorResource(
        String nombre,
        BigDecimal valor,
        String unidad
) {
    public static IndicatorResource fromEntity(Indicator i) {
        return new IndicatorResource(
                i.getNombre().name(),
                i.getValor(),
                i.getUnidad().name()
        );
    }
}
