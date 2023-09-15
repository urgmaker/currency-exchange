package com.example.currencyexchange.servlets.exchanges;

import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.dto.ErrorResponseDto;
import com.example.currencyexchange.dto.ExchangeResponseDto;
import com.example.currencyexchange.services.ExchangeService;
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
import java.util.NoSuchElementException;

@WebServlet(name = "ExchangeServlet", urlPatterns = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeService exchangeService = ExchangeService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String amountParam = req.getParameter("amount");

        if (baseCurrencyCode == null || baseCurrencyCode.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing parameter - from"
            ));
        }
        if (targetCurrencyCode == null || targetCurrencyCode.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing parameter - to"
            ));
        }
        if (amountParam == null || amountParam.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing parameter - amount"
            ));
        }

        Validator.validate(resp, baseCurrencyCode, targetCurrencyCode, objectMapper);

        BigDecimal amount = null;

        try {
            amount = BigDecimal.valueOf(Double.parseDouble(amountParam));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Incorrect value of amount parameter"
            ));
        }

        try {
            ExchangeResponseDto exchangeResponseDto = exchangeService
                    .convertCurrency(baseCurrencyCode, targetCurrencyCode, amount);
            objectMapper.writeValue(resp.getWriter(), exchangeResponseDto);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error, try again later!"
            ));
        } catch (NoSuchElementException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_NOT_FOUND,
                    "There is no exchange rate for this currency pair"
            ));
        }
    }
}
