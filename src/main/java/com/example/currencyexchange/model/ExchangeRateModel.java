package com.example.currencyexchange.model;

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
    private CurrencyModel baseCurrencyId;
    @NonNull
    private CurrencyModel targetCurrencyId;
    @NonNull
    private BigDecimal rate;

    @Override
    public String toString() {
        return "ExchangeRate{" +
               "id=" + id +
               ", baseCurrencyId=" + baseCurrencyId +
               ", targetCurrencyId=" + targetCurrencyId +
               ", rate=" + rate +
               '}';
    }
}
