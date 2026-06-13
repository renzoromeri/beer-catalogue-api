package com.renzo.beercatalogue.beer.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beer {

    private Long id;
    private String name;
    private BigDecimal abv;
    private BeerType type;
    private String description;
    private Long manufacturerId;
    private String manufacturerName;
}
