package com.upc.ven_con_plata_backend.iam.interfaces.rest.transform;

import com.upc.ven_con_plata_backend.iam.domain.model.entities.Role;
import com.upc.ven_con_plata_backend.iam.interfaces.rest.resources.RoleResource;

public class RoleResourceFromEntityAssembler {
    public static RoleResource toResourceFromEntity(Role entity) {
        return new RoleResource(entity.getId(), entity.getStringName());

    }
}
