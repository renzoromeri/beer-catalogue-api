package com.renzo.beercatalogue.beer.infrastructure.web;

import com.renzo.beercatalogue.beer.domain.BeerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.data.domain.Sort;

@Schema(description = "Optional beer filters with pagination and sorting")
public record BeerQueryRequest(
        @Schema(description = "Case-insensitive partial name filter; blank values do not filter")
        String name,
        @Schema(
                description = "Exact beer type filter",
                allowableValues = {
                    "IPA", "LAGER", "STOUT", "PILSNER", "WHEAT",
                    "PALE_ALE", "PORTER", "SOUR", "OTHER"
                }
        )
        BeerType type,
        @Schema(description = "Inclusive minimum ABV", example = "4.0")
        @Positive BigDecimal minAbv,
        @Schema(description = "Inclusive maximum ABV", example = "8.0")
        @Positive BigDecimal maxAbv,
        @Schema(
                description = "Case-insensitive partial manufacturer-name filter; blank values do not filter"
        )
        String manufacturerName,
        @Schema(description = "Zero-based page number", defaultValue = "0", example = "0")
        @PositiveOrZero Integer page,
        @Schema(description = "Number of items per page", defaultValue = "10", example = "10")
        @Positive Integer size,
        @Schema(
                description = "Sort field",
                defaultValue = "name",
                allowableValues = {"name", "abv", "type", "manufacturerName"}
        )
        String sortBy,
        @Schema(
                description = "Sort direction",
                defaultValue = "ASC",
                allowableValues = {"ASC", "DESC"}
        )
        Sort.Direction direction
) {

    public BeerQueryRequest {
        page = page == null ? 0 : page;
        size = size == null ? 10 : size;
        sortBy = sortBy == null || sortBy.isBlank() ? "name" : sortBy;
        direction = direction == null ? Sort.Direction.ASC : direction;
    }
}
