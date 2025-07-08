package com.upc.ven_con_plata_backend.iam.interfaces.acl;

import java.util.Optional;

/**
 * La fachada pública del Bounded Context IAM.
 * Otros contextos se comunicarán con IAM a través de esta interfaz.
 */
public interface IIamUserIdFacade {

    /**
     * DTO que expone los datos de un usuario de forma segura.
     * No expone la contraseña ni otros detalles internos.
     */
    record UserDto(Long userId, String username) {}

    /**
     * Busca un usuario por su ID y devuelve sus datos públicos.
     * @param userId El ID del usuario.
     * @return Un Optional con el DTO del usuario si se encuentra.
     */
    Optional<UserDto> fetchUserById(Long userId);
}