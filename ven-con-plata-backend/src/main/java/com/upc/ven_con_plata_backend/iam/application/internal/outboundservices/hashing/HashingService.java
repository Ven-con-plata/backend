package com.upc.ven_con_plata_backend.iam.application.internal.outboundservices.hashing;


public interface HashingService {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
