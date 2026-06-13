package com.renzo.beercatalogue.manufacturer.application;

import com.renzo.beercatalogue.manufacturer.domain.Manufacturer;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;

public final class ManufacturerMapper {

    private ManufacturerMapper() {
    }

    public static Manufacturer toDomain(ManufacturerEntity entity) {
        return Manufacturer.builder()
                .id(entity.getId())
                .name(entity.getName())
                .countryOfOrigin(entity.getCountryOfOrigin())
                .build();
    }

    public static ManufacturerEntity toEntity(Manufacturer manufacturer) {
        return ManufacturerEntity.builder()
                .id(manufacturer.getId())
                .name(manufacturer.getName())
                .countryOfOrigin(manufacturer.getCountryOfOrigin())
                .build();
    }
}
