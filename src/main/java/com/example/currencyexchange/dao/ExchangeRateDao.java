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
            SELECT
                CAST(er.id AS bigint) AS id,
                CAST(bc.id AS bigint) AS base_id,
                bc.code AS base_code,
                bc.full_name AS base_name,
                bc.sign AS base_sign,
                CAST(tc.id AS bigint) AS target_id,
                tc.code AS target_code,
                tc.full_name AS target_name,
                tc.sign AS target_sign,
                er.rate AS rate
            FROM public.exchange_rates AS er
            JOIN public.currencies bc ON er.base_currency_id = bc.id
            JOIN public.currencies tc ON er.target_currency_id = tc.id
            ORDER BY er.id
            """;

    private static final String FIND_BY_ID = """
            SELECT
                CAST(er.id AS bigint) AS id,
                CAST(bc.id AS bigint) AS base_id,
                bc.code AS base_code,
                bc.full_name AS base_name,
                bc.sign AS base_sign,
                CAST(tc.id AS bigint) AS target_id,
                tc.code AS target_code,
                tc.full_name AS target_name,
                tc.sign AS target_sign,
                er.rate AS rate
            FROM public.exchange_rates AS er
            JOIN public.currencies bc ON er.base_currency_id = bc.id
            JOIN public.currencies tc ON er.target_currency_id = tc.id
            WHERE er.id = ?
            """;

    private static final String UPDATE = """
            UPDATE public.exchange_rates
            SET (base_currency_id, target_currency_id, rate) = (?, ?, ?)
            WHERE id = ?
            """;

    private static final String SAVE = """
            INSERT INTO public.exchange_rates
            (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            """;

    private static final String FIND_BY_CODE = """
            SELECT
                CAST(er.id AS bigint) AS id,
                CAST(bc.id AS bigint) AS base_id,
                bc.code AS base_code,
                bc.full_name AS base_name,
                bc.sign AS base_sign,
                CAST(tc.id AS bigint) AS target_id,
                tc.code AS target_code,
                tc.full_name AS target_name,
                tc.sign AS target_sign,
                er.rate AS rate
            FROM public.exchange_rates AS er
            JOIN public.currencies bc ON er.base_currency_id = bc.id
            JOIN public.currencies tc ON er.target_currency_id = tc.id
            WHERE (
                base_currency_id = (SELECT c.id FROM public.currencies c WHERE c.code = CAST(? AS varchar)) AND
                target_currency_id = (SELECT  c2.id FROM public.currencies c2 WHERE c2.code = CAST(? AS varchar))
            )
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
            preparedStatement.setObject(1, baseCurrencyCode);
            preparedStatement.setObject(2, targetCurrencyCode);

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
            preparedStatement.setObject(1, entity.getBaseCurrency().getId());
            preparedStatement.setObject(2, entity.getTargetCurrency().getId());
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
                    new CurrencyModel(
                            resultSet.getObject("base_id", Long.class),
                            resultSet.getObject("base_code", String.class),
                            resultSet.getObject("base_name", String.class),
                            resultSet.getObject("base_sign", String.class)
                    ),
                    new CurrencyModel(
                            resultSet.getObject("target_id", Long.class),
                            resultSet.getObject("target_code", String.class),
                            resultSet.getObject("target_name", String.class),
                            resultSet.getObject("target_sign", String.class)
                    ),
                    resultSet.getObject("rate", BigDecimal.class)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
