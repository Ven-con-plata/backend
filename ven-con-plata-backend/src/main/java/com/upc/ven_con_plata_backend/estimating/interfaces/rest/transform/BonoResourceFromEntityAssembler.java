package com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.CashFlowEntry;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.BonoResource;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.CashFlowEntryResource;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.CashFlowScheduleResource;

import java.util.List;

public class BonoResourceFromEntityAssembler {

    public static BonoResource toResourceFromEntity(Bono e) {
        //var cronogramaEmisor = entity.getCronogramaEmisor();
        //var cronogramaInversor = entity.getCronogramaInversor();

        return new BonoResource(
                e.getId(),
                e.getCreatedAt(),
                e.getUpdatedAt().toLocalDate(),
                e.getBeneficioInversion().getPrimaVencimiento(),
                e.getCok().getUnidad().getMeses(),
                e.getCok().getValor(),
                e.getCostesInicialesDeudor().getComisionActivacion(),
                e.getCostesInicialesDeudor().getComisionEstudio(),
                e.getCostesInversion().getFlotacion(),
                e.getCostesInversion().getCavali(),
                e.getCostesInicialesDeudor().getNotariales(),
                e.getCostesInicialesDeudor().getRegistrales(),
                e.getCostesInicialesDeudor().getTasacion(),
                e.getEstado().name(),
                e.getFechaVencimiento(),
                e.getFrecuenciaPago().name(),
                e.getGastosPeriodicosDeudor().getComisionPeriodica(),
                e.getGastosPeriodicosDeudor().getGastosAdministrativos(),
                e.getGastosPeriodicosDeudor().getPortes(),
                e.getGastosPeriodicosDeudor().getSeguroDesgravamen(),
                e.getGastosPeriodicosDeudor().getSeguroRiesgo(),
                e.getGracia().getParcial(),
                e.getGracia().getTotal(),
                e.getMoneda().name(),
                e.getPlazoEnAnios(),
                e.getTasaInteres().getUnidad().getMeses(),
                e.getTasaInteres().getValor(),
                e.getValorComercial(),
                e.getValorNominal(),
                e.getCronogramas().stream()
                        .map(CashFlowScheduleResource::fromEntity)
                        .toList()
        );
    }
}
