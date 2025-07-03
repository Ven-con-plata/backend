package com.upc.ven_con_plata_backend.estimating.domain.model.commands;

import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Periodicidad;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateBonoCommand(
        Long bonoId,
        BigDecimal valorNominal,
        BigDecimal valorComercial,
        LocalDate fechaVencimiento,
        BigDecimal tasaInteres,
        BigDecimal cok,
        Periodicidad periodicidadCok,
        Integer periodosGraciaTotal,
        Integer periodosGraciaParcial
) {
}
