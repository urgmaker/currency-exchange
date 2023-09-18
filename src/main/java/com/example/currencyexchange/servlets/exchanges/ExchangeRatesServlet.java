package com.example.currencyexchange.servlets.exchanges;

import com.example.currencyexchange.dao.CurrencyDao;
import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.dto.ErrorResponseDto;
import com.example.currencyexchange.models.ExchangeRateModel;
import com.example.currencyexchange.utils.Validator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

@WebServlet(name = "ExchangeRatesServlet", urlPatterns = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String INTEGRITY_CONSTRAINT_VIOLATION_CODE = "23505";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateModel> exchangeRates = exchangeRateDao.findAll();
            objectMapper.writeValue(resp.getWriter(), exchangeRates);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error. Please, try again later!"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateParam = req.getParameter("rate");

        Validator.isValidParams(baseCurrencyCode, resp, objectMapper);
        Validator.isValidParams(targetCurrencyCode, resp, objectMapper);
        Validator.isValidParams(rateParam, resp, objectMapper);

        if (Validator.isNotValidCurrencyCode(baseCurrencyCode)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Currency code must to be in ISO 4217 format"
            ));
        }

//        if (baseCurrencyCode == null || baseCurrencyCode.isBlank()) {
//            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
//                    HttpServletResponse.SC_BAD_REQUEST,
//                    "Missing parameter - baseCurrencyCode"
//            ));
//        }
//
//        if (targetCurrencyCode == null || targetCurrencyCode.isBlank()) {
//            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
//                    HttpServletResponse.SC_BAD_REQUEST,
//                    "Missing parameter - targetCurrencyCode"
//            ));
//        }
//
//        if (rateParam == null || rateParam.isBlank()) {
//            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
//                    HttpServletResponse.SC_BAD_REQUEST,
//                    "Missing parameter - rate"
//            ));
//        }

        BigDecimal rate = null;
        try {
            rate = BigDecimal.valueOf(Double.parseDouble(rateParam));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Incorrect value of rate parameter"
            ));
        }

        try {
            ExchangeRateModel exchangeRate = new ExchangeRateModel(
                    currencyDao.findByCode(baseCurrencyCode).orElseThrow(),
                    currencyDao.findByCode(targetCurrencyCode).orElseThrow(),
                    rate
            );

            Long savedExchangeRateId = exchangeRateDao.save(exchangeRate);
            exchangeRate.setId(savedExchangeRateId);

            objectMapper.writeValue(resp.getWriter(), exchangeRate);
        } catch (NoSuchElementException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_NOT_FOUND,
                    "One or both currencies for which you are trying" +
                    "to add an exchange rate does not exist in the database"
            ));
        } catch (SQLException e) {
            if (e.getSQLState().equals(INTEGRITY_CONSTRAINT_VIOLATION_CODE)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                        HttpServletResponse.SC_CONFLICT,
                        e.getMessage()
                ));
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error! Please, try again later"
            ));
        }
    }
}
