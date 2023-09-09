package com.example.currencyexchange.dao;

import com.example.currencyexchange.model.CurrencyModel;
import com.example.currencyexchange.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao implements Dao<Long, CurrencyModel> {
    private static final CurrencyDao CURRENCY_DAO = new CurrencyDao();

    private static final String FIND_ALL = """
            SELECT id, code, full_name, sign
            FROM currency_exchanger.currencies
            """;

    private CurrencyDao() {

    }

    @Override
    public List<CurrencyModel> findAll() {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            List<CurrencyModel> currencies = new ArrayList<>();

            while (resultSet.next()) {
                currencies.add(builderCurrency(resultSet));
            }

            return currencies;

        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    @Override
    public Optional<CurrencyModel> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public void update(CurrencyModel entity) {

    }

    @Override
    public CurrencyModel save(CurrencyModel entity) {
        return null;
    }
}
