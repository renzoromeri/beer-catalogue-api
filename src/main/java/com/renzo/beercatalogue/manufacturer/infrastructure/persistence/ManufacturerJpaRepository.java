package com.renzo.beercatalogue.manufacturer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturerJpaRepository extends JpaRepository<ManufacturerEntity, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
