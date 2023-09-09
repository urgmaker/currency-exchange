package com.example.currencyexchange.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class CurrencyModel {
    private Long id;
    @NonNull
    private String code;
    @NonNull
    private String fullName;
    @NonNull
    private String sign;

    @Override
    public String toString() {
        return "{\"id\": " + id + ", " +
               "\"name\": " + "\"" + fullName + "\", " +
               "\"code\": " + "\"" + code + "\", " +
               "\"sign\": " + "\"" + sign + "\"}";
    }
}
