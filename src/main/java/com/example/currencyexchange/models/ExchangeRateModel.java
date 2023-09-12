package com.example.currencyexchange.models;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ExchangeRateModel {
    private Long id;
    @NonNull
    private CurrencyModel baseCurrency;
    @NonNull
    private CurrencyModel targetCurrency;
    @NonNull
    private BigDecimal rate;

    @Override
    public String toString() {
        return "ExchangeRate{" +
               "id=" + id +
               ", baseCurrencyId=" + baseCurrency +
               ", targetCurrencyId=" + targetCurrency +
               ", rate=" + rate +
               '}';
    }
}
