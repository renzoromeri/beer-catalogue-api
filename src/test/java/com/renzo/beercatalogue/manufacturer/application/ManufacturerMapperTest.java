package com.renzo.beercatalogue.manufacturer.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.renzo.beercatalogue.manufacturer.domain.Manufacturer;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import org.junit.jupiter.api.Test;

class ManufacturerMapperTest {

    @Test
    void shouldMapEntityToDomain() {
        ManufacturerEntity entity = ManufacturerEntity.builder()
                .id(1L)
                .name("Guinness")
                .countryOfOrigin("Ireland")
                .build();

        Manufacturer manufacturer = ManufacturerMapper.toDomain(entity);

        assertThat(manufacturer.getId()).isEqualTo(1L);
        assertThat(manufacturer.getName()).isEqualTo("Guinness");
        assertThat(manufacturer.getCountryOfOrigin()).isEqualTo("Ireland");
    }

    @Test
    void shouldMapDomainToEntity() {
        Manufacturer manufacturer = Manufacturer.builder()
                .id(2L)
                .name("Heineken")
                .countryOfOrigin("Netherlands")
                .build();

        ManufacturerEntity entity = ManufacturerMapper.toEntity(manufacturer);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getName()).isEqualTo("Heineken");
        assertThat(entity.getCountryOfOrigin()).isEqualTo("Netherlands");
    }
}
