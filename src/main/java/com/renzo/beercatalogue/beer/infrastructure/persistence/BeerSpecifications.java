package com.renzo.beercatalogue.beer.infrastructure.persistence;

import com.renzo.beercatalogue.beer.application.BeerQueryCriteria;
import com.renzo.beercatalogue.beer.domain.BeerType;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class BeerSpecifications {

    private BeerSpecifications() {
    }

    public static Specification<BeerEntity> from(BeerQueryCriteria criteria) {
        return Specification.allOf(
                nameContains(criteria.name()),
                typeEquals(criteria.type()),
                abvGreaterThanOrEqualTo(criteria.minAbv()),
                abvLessThanOrEqualTo(criteria.maxAbv()),
                manufacturerNameContains(criteria.manufacturerName())
        );
    }

    private static Specification<BeerEntity> nameContains(String name) {
        return (root, query, builder) -> hasText(name)
                ? builder.like(builder.lower(root.get("name")), containsPattern(name))
                : null;
    }

    private static Specification<BeerEntity> typeEquals(BeerType type) {
        return (root, query, builder) -> type == null
                ? null
                : builder.equal(root.get("type"), type);
    }

    private static Specification<BeerEntity> abvGreaterThanOrEqualTo(BigDecimal minAbv) {
        return (root, query, builder) -> minAbv == null
                ? null
                : builder.greaterThanOrEqualTo(root.get("abv"), minAbv);
    }

    private static Specification<BeerEntity> abvLessThanOrEqualTo(BigDecimal maxAbv) {
        return (root, query, builder) -> maxAbv == null
                ? null
                : builder.lessThanOrEqualTo(root.get("abv"), maxAbv);
    }

    private static Specification<BeerEntity> manufacturerNameContains(String manufacturerName) {
        return (root, query, builder) -> hasText(manufacturerName)
                ? builder.like(
                        builder.lower(root.join("manufacturer").get("name")),
                        containsPattern(manufacturerName)
                )
                : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String containsPattern(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
