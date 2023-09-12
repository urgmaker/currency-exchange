package com.example.currencyexchange.services;

import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.dto.ExchangeResponseDto;
import com.example.currencyexchange.models.ExchangeRateModel;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


public class ExchangeService {
    private static final ExchangeService INSTANCE = new ExchangeService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    private ExchangeService() {

    }

    public static ExchangeService getInstance() {
        return INSTANCE;
    }

    public ExchangeResponseDto convertCurrency(
            String baseCurrencyCode,
            String targetCurrencyCode,
            BigDecimal amount) throws SQLException, NoSuchElementException {

        ExchangeRateModel exchangeRateModel = getExchangeRate(baseCurrencyCode, targetCurrencyCode).orElseThrow();

        BigDecimal convertedAmount = amount.multiply((exchangeRateModel.getRate())
                .setScale(2, RoundingMode.HALF_EVEN));

        return new ExchangeResponseDto(
                exchangeRateModel.getBaseCurrency(),
                exchangeRateModel.getTargetCurrency(),
                exchangeRateModel.getRate(),
                amount,
                convertedAmount
        );
    }

    private Optional<ExchangeRateModel> getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) {
        Optional<ExchangeRateModel> exchangeRate = getFromDirectExchangeRate(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate.isEmpty()) {
            exchangeRate = getFromReverseExchangeRate(baseCurrencyCode, targetCurrencyCode);
        }

        return exchangeRate;
    }

    private Optional<ExchangeRateModel> getFromDirectExchangeRate(String baseCurrencyCode, String targetCurrencyCode) {
        return exchangeRateDao.findByCode(baseCurrencyCode, targetCurrencyCode);
    }
    
    private Optional<ExchangeRateModel> getFromReverseExchangeRate(String baseCurrencyCode, String targetCurrencyCode) {
        Optional<ExchangeRateModel> exchangeRateOptional = exchangeRateDao
                .findByCode(targetCurrencyCode, baseCurrencyCode);

        if (exchangeRateOptional.isEmpty()) {
            return Optional.empty();
        }

        ExchangeRateModel reverseExchangeRate = exchangeRateOptional.get();

        ExchangeRateModel directExchangeRate = new ExchangeRateModel(
            reverseExchangeRate.getTargetCurrency(),
                reverseExchangeRate.getBaseCurrency(),
                BigDecimal.ONE.divide(reverseExchangeRate.getRate(), MathContext.DECIMAL64)
        );

        return Optional.of(directExchangeRate);
    }

    private static ExchangeRateModel getExchangeForCode(List<ExchangeRateModel> rateModels, String code) {
        return rateModels.stream()
                .filter(rateModel -> rateModel.getTargetCurrency().getCode().equals(code))
                .findFirst()
                .orElseThrow();
    }

}
