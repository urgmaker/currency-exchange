package com.example.currencyexchange.servlets.exchanges;

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
import java.util.Optional;

@WebServlet(name = "ExchangeRateServlet", urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getPathInfo().replaceAll("/", "");

        if (url.length() != 6) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Currency codes are either not provided or provided in an incorrect format"
            ));
        }

        String baseCurrencyCode = url.substring(0, 3);
        String targetCurrencyCode = url.substring(3);

        isValidUrl(resp, baseCurrencyCode, targetCurrencyCode);

        try {
            Optional<ExchangeRateModel> exchangeRateOptional = exchangeRateDao.findByCode(baseCurrencyCode, targetCurrencyCode);

            if (exchangeRateOptional.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                        HttpServletResponse.SC_NOT_FOUND,
                        "There is no exchange rate for this currency pair"
                ));
            }

            objectMapper.writeValue(resp.getWriter(), exchangeRateOptional.get());
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error. Please, try again later!"
            ));
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getPathInfo().replaceAll("/", "");

        if (url.length() != 6) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Currency codes are either not provided or provided in an incorrect format"
            ));
        }

        String parameter = req.getReader().readLine();

        if (parameter == null || !parameter.contains("rate")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing required parameter rate"
            ));
        }

        String baseCurrencyCode = url.substring(0, 3);
        String targetCurrencyCode = url.substring(3);
        String paramRateValue = parameter.replace("rate=", "");

        isValidUrl(resp, baseCurrencyCode, targetCurrencyCode);

        BigDecimal rate = null;
        try {
            rate = BigDecimal.valueOf(Double.parseDouble(paramRateValue));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Incorrect value of rate parameter"
            ));
        }

        try {
            Optional<ExchangeRateModel> exchangeRateOptional = exchangeRateDao.findByCode(baseCurrencyCode, targetCurrencyCode);

            if (exchangeRateOptional.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                        HttpServletResponse.SC_NOT_FOUND,
                        "There is no exchange rate for this currency pair"
                ));
            }

            ExchangeRateModel exchangeRate = exchangeRateOptional.get();
            exchangeRate.setRate(rate);
            exchangeRateDao.update(exchangeRate);

            objectMapper.writeValue(resp.getWriter(), exchangeRate);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error. Please, try again later!"
            ));
        }

    }

    private void isValidUrl(HttpServletResponse resp, String baseCurrencyCode, String targetCurrencyCode) throws IOException {
        if (!Validator.isValidCurrencyCode(baseCurrencyCode)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Base currency code must be in ISO 4217 format"
            ));
        }

        if (!Validator.isValidCurrencyCode(targetCurrencyCode)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Base currency code must be in ISO 4217 format"
            ));
        }
    }
}
