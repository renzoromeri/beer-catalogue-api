package com.renzo.beercatalogue.beer.application;

import com.renzo.beercatalogue.beer.domain.BeerType;
import java.math.BigDecimal;
import org.springframework.data.domain.Sort;

public record BeerQueryCriteria(
        String name,
        BeerType type,
        BigDecimal minAbv,
        BigDecimal maxAbv,
        String manufacturerName,
        int page,
        int size,
        String sortBy,
        Sort.Direction direction
) {
}
