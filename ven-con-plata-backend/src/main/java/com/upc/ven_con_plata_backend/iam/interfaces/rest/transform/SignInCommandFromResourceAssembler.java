package com.upc.ven_con_plata_backend.iam.interfaces.rest.transform;

import com.upc.ven_con_plata_backend.iam.domain.model.commands.SignInCommand;
import com.upc.ven_con_plata_backend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.username(), resource.password());
    }
}
