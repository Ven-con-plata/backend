package com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.CashFlowEntry;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.BonoResource;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.CashFlowEntryResource;

import java.util.List;

public class BonoResourceFromEntityAssembler {

    public static BonoResource toResourceFromEntity(Bono entity) {
        var cronogramaEmisor = entity.getCronogramaEmisor();
        var cronogramaInversor = entity.getCronogramaInversor();

        return new BonoResource(
                entity.getId(),
                entity.getCurrency().name(),
                entity.getValorNominal(),
                entity.getValorComercial(),
                entity.getFechaEmision(),
                entity.getFechaVencimiento(),
                entity.getPlazoEnAnios(),
                entity.getFrecuenciaPago().name(),
                entity.getTasaInteres().getValor(),
                entity.getTasaInteres().getUnidad().name(),
                entity.getCok().getValor(),
                entity.getCok().getUnidad().name(),
                entity.getEstado().name(),
                entity.getActualizadoEn(),
                entity.getMetodoAmortizacion(),

                // Indicadores del emisor
                cronogramaEmisor != null && cronogramaEmisor.getIndicadoresEmisor() != null ?
                        cronogramaEmisor.getIndicadoresEmisor().getVan() : null,
                cronogramaEmisor != null && cronogramaEmisor.getIndicadoresEmisor() != null ?
                        cronogramaEmisor.getIndicadoresEmisor().getTir() : null,
                cronogramaEmisor != null && cronogramaEmisor.getIndicadoresEmisor() != null ?
                        cronogramaEmisor.getIndicadoresEmisor().getTcea() : null,

                // Indicadores del inversor
                cronogramaInversor != null && cronogramaInversor.getIndicadoresInversor() != null ?
                        cronogramaInversor.getIndicadoresInversor().getVan() : null,
                cronogramaInversor != null && cronogramaInversor.getIndicadoresInversor() != null ?
                        cronogramaInversor.getIndicadoresInversor().getTir() : null,
                cronogramaInversor != null && cronogramaInversor.getIndicadoresInversor() != null ?
                        cronogramaInversor.getIndicadoresInversor().getPrecioBono() : null,
                cronogramaInversor != null && cronogramaInversor.getIndicadoresInversor() != null ?
                        cronogramaInversor.getIndicadoresInversor().getTrea() : null,
                cronogramaInversor != null && cronogramaInversor.getIndicadoresInversor() != null ?
                        cronogramaInversor.getIndicadoresInversor().getDuracion() : null,
                cronogramaInversor != null && cronogramaInversor.getIndicadoresInversor() != null ?
                        cronogramaInversor.getIndicadoresInversor().getDuracionModificada() : null,
                cronogramaInversor != null && cronogramaInversor.getIndicadoresInversor() != null ?
                        cronogramaInversor.getIndicadoresInversor().getConvexidad() : null,

                // Cronogramas
                cronogramaEmisor != null ? mapCashFlowEntries(cronogramaEmisor.getEntries()) : List.of(),
                cronogramaInversor != null ? mapCashFlowEntries(cronogramaInversor.getEntries()) : List.of()
        );
    }

    private static List<CashFlowEntryResource> mapCashFlowEntries(
            List<CashFlowEntry> entries) {
        return entries.stream()
                .map(entry -> new CashFlowEntryResource(
                        entry.getFecha(),
                        entry.getMonto()
                ))
                .toList();
    }
}
