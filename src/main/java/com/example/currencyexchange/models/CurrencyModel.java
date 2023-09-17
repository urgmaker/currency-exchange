package com.example.currencyexchange.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("name")
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
