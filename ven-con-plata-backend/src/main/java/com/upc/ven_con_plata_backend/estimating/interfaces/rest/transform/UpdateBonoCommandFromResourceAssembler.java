package com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform;

import com.upc.ven_con_plata_backend.estimating.domain.model.commands.UpdateBonoCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Periodicidad;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.UpdateBonoResource;

public class UpdateBonoCommandFromResourceAssembler {
    public static UpdateBonoCommand toCommandFromResource(Long bonoId, UpdateBonoResource resource) {
        return new UpdateBonoCommand(
                bonoId,
                resource.valorNominal(),
                resource.valorComercial(),
                resource.fechaVencimiento(),
                resource.tasaInteres(),
                resource.cok(),
                Periodicidad.valueOf(resource.periodicidadCok()),
                resource.periodosGraciaTotal(),
                resource.periodosGraciaParcial()
        );
    }
}
