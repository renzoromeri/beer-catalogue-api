package com.renzo.beercatalogue.manufacturer.application;

import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerJpaRepository;
import com.renzo.beercatalogue.common.exception.ConflictException;
import com.renzo.beercatalogue.common.exception.ResourceNotFoundException;
import com.renzo.beercatalogue.manufacturer.domain.Manufacturer;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManufacturerService {

    private final ManufacturerJpaRepository repository;
    private final BeerJpaRepository beerRepository;

    @Transactional(readOnly = true)
    public Page<Manufacturer> list(Pageable pageable) {
        return repository.findAll(pageable).map(ManufacturerMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Manufacturer getById(Long id) {
        return ManufacturerMapper.toDomain(findEntityById(id));
    }

    @Transactional
    public Manufacturer create(Manufacturer manufacturer) {
        if (repository.existsByNameIgnoreCase(manufacturer.getName())) {
            throw new ConflictException("A manufacturer with this name already exists");
        }

        ManufacturerEntity saved = repository.save(ManufacturerMapper.toEntity(manufacturer));
        return ManufacturerMapper.toDomain(saved);
    }

    @Transactional
    public Manufacturer update(Long id, Manufacturer manufacturer) {
        ManufacturerEntity existing = findEntityById(id);

        if (repository.existsByNameIgnoreCaseAndIdNot(manufacturer.getName(), id)) {
            throw new ConflictException("A manufacturer with this name already exists");
        }

        existing.setName(manufacturer.getName());
        existing.setCountryOfOrigin(manufacturer.getCountryOfOrigin());

        return ManufacturerMapper.toDomain(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        ManufacturerEntity existing = findEntityById(id);

        if (beerRepository.existsByManufacturerId(id)) {
            throw new ConflictException("Manufacturer cannot be deleted because it has associated beers");
        }

        repository.delete(existing);
    }

    private ManufacturerEntity findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manufacturer with id %d was not found".formatted(id)
                ));
    }
}
