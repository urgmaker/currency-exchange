package com.example.currencyexchange.dao;

import com.example.currencyexchange.models.CurrencyModel;
import com.example.currencyexchange.models.ExchangeRateModel;
import com.example.currencyexchange.utils.ConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao implements Dao<Long, ExchangeRateModel> {
    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();
    private static final String FIND_ALL = """
            SELECT CAST(public.exchange_rates.id AS bigint), base_currency_id, target_currency_id, rate
            FROM public.exchange_rates
            """;

    private static final String FIND_BY_ID = """
            SELECT base_currency_id, target_currency_id, rate
            FROM public.exchange_rates
            WHERE id = ?
            """;

    private static final String UPDATE = """
            UPDATE public.exchange_rates
            SET base_currency_id = ?, target_currency_id = ?, rate = ?
            WHERE id = ?
            """;

    private static final String SAVE = """
            INSERT INTO public.exchange_rates
            (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            """;

    private static final String FIND_BY_CODE = """
            SELECT CAST(public.exchange_rates.id AS bigint), base_currency_id, target_currency_id, rate
            FROM public.exchange_rates
            WHERE base_currency_id = ? AND target_currency_id = ?
            """;

    private ExchangeRateDao() {

    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }

    @Override
    public List<ExchangeRateModel> findAll() throws SQLException {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)) {
            List<ExchangeRateModel> exchangeRateModels = new ArrayList<>();

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                exchangeRateModels.add(builderExchangeRates(resultSet));
            }

            return exchangeRateModels;

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

    public Optional<ExchangeRateModel> findByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE)) {
            preparedStatement.setString(1, baseCurrencyCode);
            preparedStatement.setString(2, targetCurrencyCode);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(builderExchangeRates(resultSet));
        }
    }

    @Override
    public void update(ExchangeRateModel entity) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {
            preparedStatement.setObject(1, entity.getBaseCurrency());
            preparedStatement.setObject(2, entity.getTargetCurrency());
            preparedStatement.setObject(3, entity.getRate());
            preparedStatement.setObject(4, entity.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long save(ExchangeRateModel entity) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, entity.getBaseCurrency());
            preparedStatement.setObject(2, entity.getTargetCurrency());
            preparedStatement.setObject(3, entity.getRate());

            ResultSet savedExchangeRate = preparedStatement.getGeneratedKeys();
            savedExchangeRate.next();
            Long savedId = savedExchangeRate.getLong("id");
            connection.commit();
            return savedId;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ExchangeRateModel builderExchangeRates(ResultSet resultSet) {
        try {
            return new ExchangeRateModel(
                    resultSet.getObject("id", Long.class),
                    resultSet.getObject("base_currency_id", CurrencyModel.class),
                    resultSet.getObject("target_currency_id", CurrencyModel.class),
                    resultSet.getObject("rate", BigDecimal.class)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
