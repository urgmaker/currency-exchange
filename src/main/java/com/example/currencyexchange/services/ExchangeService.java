package com.example.currencyexchange.services;

import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.dto.ExchangeResponseDto;

import java.util.List;

public class ExchangeService {
    private static final ExchangeService INSTANCE = new ExchangeService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    private ExchangeService() {

    }

    public static ExchangeService getInstance() {
        return INSTANCE;
    }


}
