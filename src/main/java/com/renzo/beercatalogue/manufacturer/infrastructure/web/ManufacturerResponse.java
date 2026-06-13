package com.renzo.beercatalogue.manufacturer.infrastructure.web;

public record ManufacturerResponse(
        Long id,
        String name,
        String countryOfOrigin
) {
}
