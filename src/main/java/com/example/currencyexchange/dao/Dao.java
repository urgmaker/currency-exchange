package com.example.currencyexchange.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<K, T> {
    List<T> findAll();

    Optional<T> findById(K id);

    void update(T entity);

    void save(T entity);
}
