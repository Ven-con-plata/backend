package com.upc.ven_con_plata_backend.iam.interfaces.rest.transform;

import com.upc.ven_con_plata_backend.iam.domain.model.aggregates.User;
import com.upc.ven_con_plata_backend.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User entity, String token, String role) {
        return new AuthenticatedUserResource(entity.getId(), entity.getUsername(), token, role);
    }
}
