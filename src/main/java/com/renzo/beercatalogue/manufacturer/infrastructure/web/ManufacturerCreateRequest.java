package com.renzo.beercatalogue.manufacturer.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

public record ManufacturerCreateRequest(
        @NotBlank String name,
        @NotBlank String countryOfOrigin
) {
}
