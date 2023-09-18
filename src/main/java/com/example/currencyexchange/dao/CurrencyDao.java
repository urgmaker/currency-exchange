package com.example.currencyexchange.dao;

import com.example.currencyexchange.models.CurrencyModel;
import com.example.currencyexchange.utils.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao implements Dao<Long, CurrencyModel> {
    private static final CurrencyDao CURRENCY_DAO = new CurrencyDao();

    private static final String FIND_ALL = """
            SELECT CAST(public.currencies.id AS bigint), code, full_name, sign
            FROM public.currencies
            """;

    private static final String FIND_BY_ID = """
            SELECT code, full_name, sign
            FROM public.currencies
            WHERE id = ?
            """;

    private static final String UPDATE = """
            UPDATE public.currencies
            SET code = ?, full_name = ?, sign = ?
            WHERE id = ?
            """;

    private static final String SAVE = """
            INSERT INTO public.currencies (code, full_name, sign)
            VALUES (?, ?, ?)
            """;

    private static final String FIND_BY_CODE = """
            SELECT CAST(public.currencies.id AS bigint), code, full_name, sign
            FROM public.currencies
            WHERE code = ?
            """;

    private CurrencyDao() {

    }

    public static CurrencyDao getInstance() {
        return CURRENCY_DAO;
    }

    @Override
    public List<CurrencyModel> findAll() throws SQLException {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)) {
            List<CurrencyModel> currencies = new ArrayList<>();

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                currencies.add(builderCurrency(resultSet));
            }

            return currencies;
        }
    }

    @Override
    public Optional<CurrencyModel> findById(Long id) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
            CurrencyModel currencyModel = null;

            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                currencyModel = builderCurrency(resultSet);
            }
            return Optional.ofNullable(currencyModel);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<CurrencyModel> findByCode(String code) throws SQLException {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE)) {
            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(builderCurrency(resultSet));
        }
    }

    @Override
    public void update(CurrencyModel entity) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {
            preparedStatement.setObject(1, entity.getCode());
            preparedStatement.setObject(2, entity.getFullName());
            preparedStatement.setObject(3, entity.getSign());
            preparedStatement.setObject(4, entity.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long save(CurrencyModel entity) throws SQLException {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            preparedStatement.setObject(1, entity.getCode());
            preparedStatement.setObject(2, entity.getFullName());
            preparedStatement.setObject(3, entity.getSign());

            preparedStatement.executeUpdate();

            Long savedId = null;

            try {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    savedId = generatedKeys.getLong(1);
                } else {
                    connection.rollback();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return savedId;
        }
    }

    private CurrencyModel builderCurrency(ResultSet resultSet) {
        try {
            return new CurrencyModel(
                    resultSet.getObject("id", Long.class),
                    resultSet.getObject("code", String.class),
                    resultSet.getObject("full_name", String.class),
                    resultSet.getObject("sign", String.class)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
