package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;
import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public Money(BigDecimal amount) {
        // Constructor de conveniencia para la moneda por defecto (PEN)
        this(amount, Currency.getInstance("PEN"));
    }

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null.");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null.");
        }
    }
    // Podríamos añadir métodos de negocio como add, subtract, etc.
}