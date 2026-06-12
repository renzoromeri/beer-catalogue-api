package com.renzo.beercatalogue.beer.infrastructure.web;

import com.renzo.beercatalogue.beer.domain.BeerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record BeerCreateRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal abv,
        @NotNull BeerType type,
        String description,
        @NotNull Long manufacturerId
) {
}
