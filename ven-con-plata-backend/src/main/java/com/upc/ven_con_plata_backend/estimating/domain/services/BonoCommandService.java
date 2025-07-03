package com.upc.ven_con_plata_backend.estimating.domain.services;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.CreateBonoCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.UpdateBonoCommand;

import java.util.Optional;

public interface BonoCommandService {
    Optional<Bono> handle(CreateBonoCommand command);
    Optional<Bono> handle(UpdateBonoCommand command);
}
