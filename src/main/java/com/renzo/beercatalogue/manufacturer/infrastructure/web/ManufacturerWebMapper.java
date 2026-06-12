package com.renzo.beercatalogue.manufacturer.infrastructure.web;

import com.renzo.beercatalogue.manufacturer.domain.Manufacturer;

final class ManufacturerWebMapper {

    private ManufacturerWebMapper() {
    }

    static Manufacturer toDomain(ManufacturerCreateRequest request) {
        return Manufacturer.builder()
                .name(request.name())
                .countryOfOrigin(request.countryOfOrigin())
                .build();
    }

    static Manufacturer toDomain(ManufacturerUpdateRequest request) {
        return Manufacturer.builder()
                .name(request.name())
                .countryOfOrigin(request.countryOfOrigin())
                .build();
    }

    static ManufacturerResponse toResponse(Manufacturer manufacturer) {
        return new ManufacturerResponse(
                manufacturer.getId(),
                manufacturer.getName(),
                manufacturer.getCountryOfOrigin()
        );
    }
}
