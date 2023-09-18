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
import java.util.List;

@WebServlet(name = "CurrenciesServlet", urlPatterns = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String INTEGRITY_CONSTRAINT_VIOLATION_CODE = "23505";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyModel> modelList = currencyDao.findAll();
            objectMapper.writeValue(resp.getWriter(), modelList);
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
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        Validator.validateParams(name, resp, objectMapper);
        Validator.validateParams(code, resp, objectMapper);
        Validator.validateParams(sign, resp, objectMapper);

        if (Validator.isNotValidCurrencyCode(code)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDto(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Currency code must to be in ISO 4217 format"
            ));
        }

        try {
            CurrencyModel currency = new CurrencyModel(code, name, sign);
            Long savedCurrencyId = currencyDao.save(currency);
            currency.setId(savedCurrencyId);

            objectMapper.writeValue(resp.getWriter(), currency);
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
