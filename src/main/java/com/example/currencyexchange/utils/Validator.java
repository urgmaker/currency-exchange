package com.example.currencyexchange.utils;

import com.example.currencyexchange.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public final class Validator {
    private static Set<String> currencyCodes;

    public static boolean isNotValidCurrencyCode(String code) {
        if (currencyCodes == null) {
            Set<Currency> availableCurrencies = Currency.getAvailableCurrencies();
            currencyCodes = availableCurrencies.stream()
                    .map(Currency::getCurrencyCode)
                    .collect(Collectors.toSet());
        }
        return !currencyCodes.contains(code);
    }

    public static void validateParams(String paramValue, HttpServletResponse resp, ObjectMapper objectMapper) throws IOException {
        if (paramValue == null || paramValue.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing parameter: " + paramValue
            ));
        }
    }

    public static void validateParams(HttpServletResponse resp, String baseCurrencyCode, String targetCurrencyCode, ObjectMapper objectMapper) throws IOException {
        if (isNotValidCurrencyCode(baseCurrencyCode)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Base currency code must be in ISO 4217 format"
            ));
        }

        if (isNotValidCurrencyCode(targetCurrencyCode)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Target currency code must be in ISO 4217 format"
            ));
        }
    }
}
