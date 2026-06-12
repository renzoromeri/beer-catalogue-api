package com.renzo.beercatalogue.beer.infrastructure.web;

import com.renzo.beercatalogue.beer.domain.BeerType;
import java.math.BigDecimal;

public record BeerResponse(
        Long id,
        String name,
        BigDecimal abv,
        BeerType type,
        String description,
        Long manufacturerId,
        String manufacturerName
) {
}
