package com.renzo.beercatalogue.beer.application;

import com.renzo.beercatalogue.beer.domain.Beer;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;

public final class BeerMapper {

    private BeerMapper() {
    }

    public static Beer toDomain(BeerEntity entity) {
        return Beer.builder()
                .id(entity.getId())
                .name(entity.getName())
                .abv(entity.getAbv())
                .type(entity.getType())
                .description(entity.getDescription())
                .manufacturerId(entity.getManufacturer().getId())
                .manufacturerName(entity.getManufacturer().getName())
                .build();
    }

    public static BeerEntity toEntity(Beer beer, ManufacturerEntity manufacturer) {
        return BeerEntity.builder()
                .id(beer.getId())
                .name(beer.getName())
                .abv(beer.getAbv())
                .type(beer.getType())
                .description(beer.getDescription())
                .manufacturer(manufacturer)
                .build();
    }
}
