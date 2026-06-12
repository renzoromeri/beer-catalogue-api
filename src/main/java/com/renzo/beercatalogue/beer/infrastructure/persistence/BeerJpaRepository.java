package com.renzo.beercatalogue.beer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeerJpaRepository extends JpaRepository<BeerEntity, Long> {

    boolean existsByNameIgnoreCaseAndManufacturerId(String name, Long manufacturerId);
}
