package com.example.currencyexchange.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Dao<K, T> {
    List<T> findAll() throws SQLException;

    Optional<T> findById(K id);

    void update(T entity);

    void save(T entity);
}
