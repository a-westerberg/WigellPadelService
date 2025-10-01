package com.skrt.wigellpadelservice.services.impl;

import com.skrt.wigellpadelservice.services.PadelPricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PadelPricingServiceImpl implements PadelPricingService {
    private final BigDecimal base;
    private final BigDecimal extraPerPlayer;
    private final int basePlayers;

    @Autowired
    public PadelPricingServiceImpl(
            @Value("${wigell.pricing.base}") BigDecimal base,
            @Value("${wigell.pricing.extraPerPlayer}") BigDecimal extraPerPlayers,
            @Value("${wigell.pricing.basePlayers}") int basePlayers) {
        this.base = base;
        this.extraPerPlayer = extraPerPlayers;
        this.basePlayers = basePlayers;
    }

    @Override
    public BigDecimal calculatePriceSek(int players) {
        int extra = Math.max(0, players - basePlayers);
        return base.add(extraPerPlayer.multiply(BigDecimal.valueOf(extra)));
    }
}
