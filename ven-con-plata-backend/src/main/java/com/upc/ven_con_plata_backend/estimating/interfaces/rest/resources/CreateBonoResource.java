package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBonoResource(String moneda,
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
                                 Integer periodosGraciaTotal,
                                 Integer periodosGraciaParcial,
                                 BigDecimal costeFlotacion,
                                 BigDecimal costeCavali,
                                 BigDecimal primaVencimiento,
                                 BigDecimal costesNotariales,
                                 BigDecimal costesRegistrales,
                                 BigDecimal costesTasacion,
                                 BigDecimal comisionEstudio,
                                 BigDecimal comisionActivacion,
                                 BigDecimal seguroDesgravamen,
                                 BigDecimal seguroRiesgo,
                                 BigDecimal comisionPeriodica,
                                 BigDecimal portes,
                                 BigDecimal gastosAdministrativos) {
}
