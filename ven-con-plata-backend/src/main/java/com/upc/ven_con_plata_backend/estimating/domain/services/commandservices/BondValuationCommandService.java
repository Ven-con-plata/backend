package com.upc.ven_con_plata_backend.estimating.domain.services.commandservices;

import com.upc.ven_con_plata_backend.estimating.domain.model.commands.CreateBondValuationCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.DeleteBondValuationCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.UpdateBondValuationCommand;

import java.util.Optional;

/**
 * Contrato para los casos de uso de escritura del aggregate BondValuation.
 */
public interface BondValuationCommandService {
    Optional<Long> handle(CreateBondValuationCommand command);
    Optional<Long> handle(UpdateBondValuationCommand command);
    void handle(DeleteBondValuationCommand command);
}