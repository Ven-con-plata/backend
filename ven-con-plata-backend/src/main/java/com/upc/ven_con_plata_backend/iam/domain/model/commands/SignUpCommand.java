package com.upc.ven_con_plata_backend.iam.domain.model.commands;

import com.upc.ven_con_plata_backend.iam.domain.model.entities.Role;

import java.util.List;

public record SignUpCommand(String username, String password, List<Role> roles) {
}
