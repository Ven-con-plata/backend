package com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform;

import com.upc.ven_con_plata_backend.estimating.domain.model.commands.CreateBonoCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Currency;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Periodicidad;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.CreateBonoResource;

public class CreateBonoCommandFromResourceAssembler {

    public static CreateBonoCommand toCommandFromResource(CreateBonoResource resource) {
        return new CreateBonoCommand(
                Currency.valueOf(resource.moneda()),
                resource.valorNominal(),
                resource.valorComercial(),
                resource.fechaEmision(),
                resource.fechaVencimiento(),
                resource.plazoEnAnios(),
                Periodicidad.valueOf(resource.frecuenciaPago()),
                resource.tasaInteres(),
                Periodicidad.valueOf(resource.periodicidadInteres()),
                resource.cok(),
                Periodicidad.valueOf(resource.periodicidadCok()),
                resource.periodosGraciaTotal(),
                resource.periodosGraciaParcial(),
                resource.costeFlotacion(),
                resource.costeCavali(),
                resource.primaVencimiento(),
                resource.costesNotariales(),
                resource.costesRegistrales(),
                resource.costesTasacion(),
                resource.comisionEstudio(),
                resource.comisionActivacion(),
                resource.seguroDesgravamen(),
                resource.seguroRiesgo(),
                resource.comisionPeriodica(),
                resource.portes(),
                resource.gastosAdministrativos()
        );
    }
}
