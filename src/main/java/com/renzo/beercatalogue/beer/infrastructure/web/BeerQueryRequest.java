package com.renzo.beercatalogue.beer.infrastructure.web;

import com.renzo.beercatalogue.beer.domain.BeerType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.data.domain.Sort;

public record BeerQueryRequest(
        String name,
        BeerType type,
        @Positive BigDecimal minAbv,
        @Positive BigDecimal maxAbv,
        String manufacturerName,
        @PositiveOrZero Integer page,
        @Positive Integer size,
        String sortBy,
        Sort.Direction direction
) {

    public BeerQueryRequest {
        page = page == null ? 0 : page;
        size = size == null ? 10 : size;
        sortBy = sortBy == null || sortBy.isBlank() ? "name" : sortBy;
        direction = direction == null ? Sort.Direction.ASC : direction;
    }
}
