package com.renzo.beercatalogue.beer.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface BeerJpaRepository
        extends JpaRepository<BeerEntity, Long>, JpaSpecificationExecutor<BeerEntity> {

    @Override
    @EntityGraph(attributePaths = "manufacturer")
    Page<BeerEntity> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "manufacturer")
    Page<BeerEntity> findAll(Specification<BeerEntity> specification, Pageable pageable);

    boolean existsByNameIgnoreCaseAndManufacturerId(String name, Long manufacturerId);

    boolean existsByManufacturerId(Long manufacturerId);

    boolean existsByNameIgnoreCaseAndManufacturerIdAndIdNot(
            String name,
            Long manufacturerId,
            Long id
    );
}
