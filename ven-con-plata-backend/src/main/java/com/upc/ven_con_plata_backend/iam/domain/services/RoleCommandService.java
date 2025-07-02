package com.upc.ven_con_plata_backend.iam.domain.services;

import com.upc.ven_con_plata_backend.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}
