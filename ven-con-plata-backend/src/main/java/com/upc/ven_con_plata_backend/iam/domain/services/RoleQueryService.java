package com.upc.ven_con_plata_backend.iam.domain.services;

import com.upc.ven_con_plata_backend.iam.domain.model.entities.Role;
import com.upc.ven_con_plata_backend.iam.domain.model.queries.GetAllRolesQuery;
import com.upc.ven_con_plata_backend.iam.domain.model.queries.GetRoleByNameQuery;

import java.util.List;
import java.util.Optional;

public interface RoleQueryService {
    List<Role> handle(GetAllRolesQuery query);
    Optional<Role> handle(GetRoleByNameQuery query);
}
