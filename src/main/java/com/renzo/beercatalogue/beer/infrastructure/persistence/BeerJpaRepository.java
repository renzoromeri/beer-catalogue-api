package com.renzo.beercatalogue.beer.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface BeerJpaRepository extends JpaRepository<BeerEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "manufacturer")
    Page<BeerEntity> findAll(Pageable pageable);

    boolean existsByNameIgnoreCaseAndManufacturerId(String name, Long manufacturerId);

    boolean existsByManufacturerId(Long manufacturerId);

    boolean existsByNameIgnoreCaseAndManufacturerIdAndIdNot(
            String name,
            Long manufacturerId,
            Long id
    );
}
