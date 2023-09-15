package com.example.currencyexchange.servlets.currencies;

import com.example.currencyexchange.dao.CurrencyDao;
import com.example.currencyexchange.dto.ErrorResponseDto;
import com.example.currencyexchange.models.CurrencyModel;
import com.example.currencyexchange.utils.Validator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "CurrencyServlet", urlPatterns = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getPathInfo().replaceAll("/", "");

        if (Validator.isNotValidCurrencyCode(code)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Currency code must to be in ISO 4217 format"
            ));
        }

        try {
            Optional<CurrencyModel> currency = currencyDao.findByCode(code);

            if (currency.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                        HttpServletResponse.SC_NOT_FOUND,
                        "There is no such currency in database"
                ));
            }

            objectMapper.writeValue(resp.getWriter(), currency.get());
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error. Please, try again later!"
            ));
        }
    }
}
