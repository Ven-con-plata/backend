package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BonoResource(
        Long id,
        String moneda,
        BigDecimal valorNominal,
        BigDecimal valorComercial,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        Integer plazoEnAnios,
        String frecuenciaPago,
        BigDecimal tasaInteres,
        String periodicidadInteres,
        BigDecimal cok,
        String periodicidadCok,
        String estado,
        LocalDateTime actualizadoEn,
        String metodoAmortizacion,

        // Indicadores del emisor
        BigDecimal vanEmisor,
        BigDecimal tirEmisor,
        BigDecimal tceaEmisor,

        // Indicadores del inversor
        BigDecimal vanInversor,
        BigDecimal tirInversor,
        BigDecimal precioBono,
        BigDecimal treaInversor,
        BigDecimal duracion,
        BigDecimal duracionModificada,
        BigDecimal convexidad,

        // Cronogramas
        List<CashFlowEntryResource> cronogramaEmisor,
        List<CashFlowEntryResource> cronogramaInversor
) {
}
