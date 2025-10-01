package com.skrt.wigellpadelservice.services;

import java.math.BigDecimal;

public interface PadelPricingService {
    BigDecimal calculatePriceSek(int players);
}
