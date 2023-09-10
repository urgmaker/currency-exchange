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

    private static final String FIND_BY_ID = """
            SELECT base_currency_id, target_currency_id, rate
            FROM currency_repository.currency_exchanger.exchange_rates
            WHERE id = ?
            """;

    private static final String UPDATE = """
            UPDATE currency_repository.currency_exchanger.exchange_rates
            SET base_currency_id = ?, target_currency_id = ?, rate = ?
            WHERE id = ?
            """;

    private static final String SAVE = """
            INSERT INTO currency_repository.currency_exchanger.exchange_rates
            (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
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
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
            ExchangeRateModel exchangeRateModel = null;

            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                exchangeRateModel = builderExchangeRates(resultSet);
            }

            return Optional.ofNullable(exchangeRateModel);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ExchangeRateModel entity) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {
            preparedStatement.setObject(1, entity.getBaseCurrencyId());
            preparedStatement.setObject(2, entity.getTargetCurrencyId());
            preparedStatement.setObject(3, entity.getRate());
            preparedStatement.setObject(4, entity.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ExchangeRateModel entity) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE)) {
            preparedStatement.setObject(1, entity.getBaseCurrencyId());
            preparedStatement.setObject(2, entity.getTargetCurrencyId());
            preparedStatement.setObject(3, entity.getRate());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
