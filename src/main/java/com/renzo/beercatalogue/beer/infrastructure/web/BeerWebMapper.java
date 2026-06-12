package com.renzo.beercatalogue.beer.infrastructure.web;

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
}
