package com.skrt.wigellpadelservice.services.impl;

import com.skrt.wigellpadelservice.exceptions.CurrencyConversionException;
import com.skrt.wigellpadelservice.services.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.Map;

@Service
public class ApiPluginCurrencyService implements CurrencyService {



    private final RestClient client;
    private final String apiKey;
    private final boolean failOpen;

    private static final String PROVIDER = "apiplugin";
    private static final String ENDPOINT = "/v1/currency/{apiKey}/convert";


    @Autowired
    public ApiPluginCurrencyService(
            @Qualifier("currencyRestClient") RestClient client,
            @Value("${wigell.currency.apiplugin.apiKey}") String apiKey,
            @Value("${wigell.currency.failOpen:true}") boolean failOpen
    ) {
        this.client = client;
        this.apiKey = apiKey;
        this.failOpen = failOpen;
    }

    @Override
    public BigDecimal toEur(BigDecimal amountSek) {
        if(amountSek == null) return null;
        if(amountSek.signum() == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        try{
            ResponseEntity<Map> entity = client.get()
                    .uri(uriBuilder -> uriBuilder
                                    .path("/v1/currency/{apiKey}/convert")
                                    .queryParam("from", "SEK")
                                    .queryParam("to", "EUR")
                                    .queryParam("amount", amountSek)
                                    .build(apiKey))
                    .retrieve()
                    .toEntity(Map.class);

            if(entity.getStatusCode().isError()){
                String body = entity.hasBody() ? String.valueOf(entity.getBody()) : null;
                throw new CurrencyConversionException(
                        PROVIDER,
                        ENDPOINT,
                        entity.getStatusCode().value(),
                        body,
                        "Currency API error");
            }
            
            Map<String, Object> json = entity.getBody();
            if(json == null){
                throw new CurrencyConversionException(PROVIDER, ENDPOINT, "Empty response");
            }
            
            BigDecimal eur = readResult(json, amountSek);
            return eur.setScale(2, RoundingMode.HALF_UP);
        } catch (RestClientResponseException ex){
            if(failOpen) return null;
            throw new CurrencyConversionException(PROVIDER, ENDPOINT, ex.getRawStatusCode(), ex.getResponseBodyAsString(), "Currency API error");
        } catch (CurrencyConversionException ex){
            if(failOpen) return null;
            throw ex;
        }catch (Exception ex){
            if(failOpen) return null;
            throw new CurrencyConversionException(PROVIDER, ENDPOINT, ex.getMessage());
        }
    }

    private static BigDecimal readResult(Map<String, Object> json, BigDecimal amountSek) {
        BigDecimal eur = asBigDecimal(json.get("result"));
        if(eur != null) return eur;

        Object dataObj = json.get("data");
        if(dataObj instanceof Map<?,?> data){
            BigDecimal amount = asBigDecimal(data.get("amount"));
            if(amount != null) return amount;

            BigDecimal rate = asBigDecimal(data.get("rate"));
            if(rate != null) return amountSek.multiply(rate);
        }

        BigDecimal value = asBigDecimal(json.get("value"));
        if(value != null) return value;
        throw new CurrencyConversionException("apiplugin", ENDPOINT, "Unexpected currency API response shape: " +json);
    }

    private static BigDecimal asBigDecimal(Object value) {
        if(value == null) return null;

        if(value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if(value instanceof Number number) {
            if(number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte) {
                return BigDecimal.valueOf(number.longValue());
            }
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString().trim());
    }

}
