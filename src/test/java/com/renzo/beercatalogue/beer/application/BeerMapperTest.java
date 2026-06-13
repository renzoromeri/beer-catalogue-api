package com.renzo.beercatalogue.beer.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.renzo.beercatalogue.beer.domain.Beer;
import com.renzo.beercatalogue.beer.domain.BeerType;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BeerMapperTest {

    @Test
    void shouldMapEntityToDomain() {
        ManufacturerEntity manufacturer = manufacturer();
        BeerEntity entity = BeerEntity.builder()
                .id(10L)
                .name("Guinness Draught")
                .abv(new BigDecimal("4.20"))
                .type(BeerType.STOUT)
                .description("Irish dry stout")
                .manufacturer(manufacturer)
                .build();

        Beer beer = BeerMapper.toDomain(entity);

        assertThat(beer.getId()).isEqualTo(10L);
        assertThat(beer.getName()).isEqualTo("Guinness Draught");
        assertThat(beer.getAbv()).isEqualByComparingTo("4.20");
        assertThat(beer.getType()).isEqualTo(BeerType.STOUT);
        assertThat(beer.getDescription()).isEqualTo("Irish dry stout");
        assertThat(beer.getManufacturerId()).isEqualTo(1L);
        assertThat(beer.getManufacturerName()).isEqualTo("Guinness");
    }

    @Test
    void shouldMapDomainToEntityWithManufacturer() {
        ManufacturerEntity manufacturer = manufacturer();
        Beer beer = Beer.builder()
                .id(20L)
                .name("Guinness Foreign Extra Stout")
                .abv(new BigDecimal("7.50"))
                .type(BeerType.STOUT)
                .description("Full-bodied stout")
                .manufacturerId(1L)
                .manufacturerName("Guinness")
                .build();

        BeerEntity entity = BeerMapper.toEntity(beer, manufacturer);

        assertThat(entity.getId()).isEqualTo(20L);
        assertThat(entity.getName()).isEqualTo("Guinness Foreign Extra Stout");
        assertThat(entity.getAbv()).isEqualByComparingTo("7.50");
        assertThat(entity.getType()).isEqualTo(BeerType.STOUT);
        assertThat(entity.getDescription()).isEqualTo("Full-bodied stout");
        assertThat(entity.getManufacturer()).isSameAs(manufacturer);
    }

    private ManufacturerEntity manufacturer() {
        return ManufacturerEntity.builder()
                .id(1L)
                .name("Guinness")
                .countryOfOrigin("Ireland")
                .build();
    }
}
