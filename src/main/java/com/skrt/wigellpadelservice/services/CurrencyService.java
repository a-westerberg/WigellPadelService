package com.skrt.wigellpadelservice.services;


import java.math.BigDecimal;

public interface CurrencyService {

    BigDecimal toEur(BigDecimal amountSek);
}
