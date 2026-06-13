package com.renzo.beercatalogue.beer.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.renzo.beercatalogue.beer.application.BeerQueryCriteria;
import com.renzo.beercatalogue.beer.domain.BeerType;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerJpaRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
class BeerSpecificationsTest {

    @Autowired
    private BeerJpaRepository beerRepository;

    @Autowired
    private ManufacturerJpaRepository manufacturerRepository;

    @BeforeEach
    void setUp() {
        ManufacturerEntity guinness = manufacturerRepository.save(ManufacturerEntity.builder()
                .name("Guinness")
                .countryOfOrigin("Ireland")
                .build());
        ManufacturerEntity heineken = manufacturerRepository.save(ManufacturerEntity.builder()
                .name("Heineken")
                .countryOfOrigin("Netherlands")
                .build());

        beerRepository.save(beer("Guinness Draught", "4.20", BeerType.STOUT, guinness));
        beerRepository.save(beer("Guinness Extra Stout", "7.50", BeerType.STOUT, guinness));
        beerRepository.save(beer("Heineken Lager", "5.00", BeerType.LAGER, heineken));
    }

    @Test
    void shouldQueryByNameIgnoringCase() {
        Page<BeerEntity> result = query(criteria("DRAUGHT", null, null, null, null));

        assertThat(result.getContent()).extracting(BeerEntity::getName)
                .containsExactly("Guinness Draught");
    }

    @Test
    void shouldQueryByType() {
        Page<BeerEntity> result = query(criteria(null, BeerType.STOUT, null, null, null));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldQueryByAbvRange() {
        Page<BeerEntity> result = query(criteria(
                null,
                null,
                new BigDecimal("4.50"),
                new BigDecimal("6.00"),
                null
        ));

        assertThat(result.getContent()).extracting(BeerEntity::getName)
                .containsExactly("Heineken Lager");
    }

    @Test
    void shouldQueryByManufacturerNameIgnoringCase() {
        Page<BeerEntity> result = query(criteria(null, null, null, null, "guinn"));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldReturnPaginatedResultsForEmptyQuery() {
        BeerQueryCriteria criteria = new BeerQueryCriteria(
                null,
                null,
                null,
                null,
                null,
                0,
                2,
                "name",
                Sort.Direction.ASC
        );

        Page<BeerEntity> result = beerRepository.findAll(
                BeerSpecifications.from(criteria),
                PageRequest.of(0, 2, Sort.by("name"))
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    private Page<BeerEntity> query(BeerQueryCriteria criteria) {
        return beerRepository.findAll(
                BeerSpecifications.from(criteria),
                PageRequest.of(0, 10, Sort.by("name"))
        );
    }

    private BeerQueryCriteria criteria(
            String name,
            BeerType type,
            BigDecimal minAbv,
            BigDecimal maxAbv,
            String manufacturerName
    ) {
        return new BeerQueryCriteria(
                name,
                type,
                minAbv,
                maxAbv,
                manufacturerName,
                0,
                10,
                "name",
                Sort.Direction.ASC
        );
    }

    private BeerEntity beer(
            String name,
            String abv,
            BeerType type,
            ManufacturerEntity manufacturer
    ) {
        return BeerEntity.builder()
                .name(name)
                .abv(new BigDecimal(abv))
                .type(type)
                .manufacturer(manufacturer)
                .build();
    }
}
