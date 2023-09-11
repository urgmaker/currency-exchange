package com.example.currencyexchange.dto;

import com.example.currencyexchange.model.CurrencyModel;
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
