package com.upc.ven_con_plata_backend.estimating.interfaces.acl;


import java.util.Optional;

/**
 * Contrato de la Fachada que expone las capacidades que el contexto 'Estimating'
 * necesita del contexto 'IAM'.
 */
public interface IIamContextFacade {

    /**
     * DTO para representar los datos mínimos que necesitamos de un usuario.
     * Este DTO pertenece a la ACL del contexto Estimating.
     */
    record UserDataDto(Long userId, String username) {}

    /**
     * Obtiene los datos de un usuario por su ID.
     * @param userId El ID del usuario a buscar.
     * @return Un Optional que contiene el DTO del usuario si existe.
     */
    Optional<UserDataDto> fetchUserById(Long userId);
}