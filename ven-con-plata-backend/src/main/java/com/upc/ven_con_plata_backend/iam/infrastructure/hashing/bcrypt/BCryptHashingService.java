package com.upc.ven_con_plata_backend.iam.infrastructure.hashing.bcrypt;

import com.upc.ven_con_plata_backend.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
