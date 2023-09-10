package com.example.currencyexchange.dao;

import com.example.currencyexchange.model.CurrencyModel;
import com.example.currencyexchange.model.ExchangeRateModel;
import com.example.currencyexchange.util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao implements Dao<Long, ExchangeRateModel> {
    private static final String FIND_ALL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM currency_repository.currency_exchanger.exchange_rates
            """;

    @Override
    public List<ExchangeRateModel> findAll() {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)) {
            List<ExchangeRateModel> exchangeRateModels = new ArrayList<>();

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                exchangeRateModels.add(builderExchangeRates(resultSet));
            }

            return exchangeRateModels;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Optional<ExchangeRateModel> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public void update(ExchangeRateModel entity) {

    }

    @Override
    public void save(ExchangeRateModel entity) {

    }

    private ExchangeRateModel builderExchangeRates(ResultSet resultSet) {
        try {
            return new ExchangeRateModel(
                    resultSet.getObject("id", Long.class),
                    resultSet.getObject("base_currency_id", Long.class),
                    resultSet.getObject("target_currency_id", Long.class),
                    resultSet.getObject("rate", BigDecimal.class)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
