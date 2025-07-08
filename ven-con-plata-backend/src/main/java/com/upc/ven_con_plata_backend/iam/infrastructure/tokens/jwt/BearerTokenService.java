package com.upc.ven_con_plata_backend.iam.infrastructure.tokens.jwt;

import jakarta.servlet.http.HttpServletRequest;
import com.upc.ven_con_plata_backend.iam.application.internal.outboundservices.tokens.TokenService;

public interface BearerTokenService extends TokenService {

    String getBearerTokenFrom(HttpServletRequest token);

}
