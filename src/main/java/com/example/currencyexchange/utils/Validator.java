package com.example.currencyexchange.utils;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public final class Validator {
    private static Set<String> currencyCodes;

    public static boolean isValidCurrencyCode(String code) {
        if (currencyCodes == null) {
            Set<Currency> availableCurrencies = Currency.getAvailableCurrencies();
            currencyCodes = availableCurrencies.stream()
                    .map(Currency::getCurrencyCode)
                    .collect(Collectors.toSet());
        }
        return currencyCodes.contains(code);
    }
}
