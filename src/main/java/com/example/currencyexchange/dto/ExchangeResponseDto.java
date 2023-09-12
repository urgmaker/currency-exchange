package com.example.currencyexchange.dto;

import com.example.currencyexchange.models.CurrencyModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeResponseDto {
    private CurrencyModel baseCurrency;
    private CurrencyModel targetCurrency;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
}
