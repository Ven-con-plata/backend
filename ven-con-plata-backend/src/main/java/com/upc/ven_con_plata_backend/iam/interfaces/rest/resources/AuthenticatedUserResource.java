package com.upc.ven_con_plata_backend.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(Long id, String username, String token, String role) {
}
