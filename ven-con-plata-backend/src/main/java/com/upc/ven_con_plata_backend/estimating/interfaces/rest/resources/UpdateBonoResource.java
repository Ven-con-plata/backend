package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateBonoResource(
        BigDecimal valorNominal,
        BigDecimal valorComercial,
        LocalDate fechaVencimiento,
        BigDecimal tasaInteres,
        BigDecimal cok,
        String periodicidadCok,
        Integer periodosGraciaTotal,
        Integer periodosGraciaParcial
) {}
