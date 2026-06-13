package com.renzo.beercatalogue.beer.infrastructure.web;

import com.renzo.beercatalogue.beer.application.BeerQueryCriteria;
import com.renzo.beercatalogue.beer.domain.Beer;

final class BeerWebMapper {

    private BeerWebMapper() {
    }

    static Beer toDomain(BeerCreateRequest request) {
        return Beer.builder()
                .name(request.name())
                .abv(request.abv())
                .type(request.type())
                .description(request.description())
                .manufacturerId(request.manufacturerId())
                .build();
    }

    static Beer toDomain(BeerUpdateRequest request) {
        return Beer.builder()
                .name(request.name())
                .abv(request.abv())
                .type(request.type())
                .description(request.description())
                .manufacturerId(request.manufacturerId())
                .build();
    }

    static BeerResponse toResponse(Beer beer) {
        return new BeerResponse(
                beer.getId(),
                beer.getName(),
                beer.getAbv(),
                beer.getType(),
                beer.getDescription(),
                beer.getManufacturerId(),
                beer.getManufacturerName()
        );
    }

    static BeerQueryCriteria toCriteria(BeerQueryRequest request) {
        return new BeerQueryCriteria(
                request.name(),
                request.type(),
                request.minAbv(),
                request.maxAbv(),
                request.manufacturerName(),
                request.page(),
                request.size(),
                request.sortBy(),
                request.direction()
        );
    }
}
