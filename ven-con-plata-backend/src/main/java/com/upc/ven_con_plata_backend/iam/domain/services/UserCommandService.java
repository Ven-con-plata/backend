package com.upc.ven_con_plata_backend.iam.domain.services;

import com.upc.ven_con_plata_backend.iam.domain.model.aggregates.User;
import com.upc.ven_con_plata_backend.iam.domain.model.commands.SignInCommand;
import com.upc.ven_con_plata_backend.iam.domain.model.commands.SignUpCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

public interface UserCommandService {
    Optional<User> handle(SignUpCommand command);
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);
}
