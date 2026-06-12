package com.renzo.beercatalogue.beer.application;

import com.renzo.beercatalogue.beer.domain.Beer;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerEntity;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerJpaRepository;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerSpecifications;
import com.renzo.beercatalogue.common.exception.BadRequestException;
import com.renzo.beercatalogue.common.exception.ConflictException;
import com.renzo.beercatalogue.common.exception.ResourceNotFoundException;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerJpaRepository;
import com.renzo.beercatalogue.security.application.OwnershipAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BeerService {

    private static final String MANUFACTURER_NAME_SORT = "manufacturerName";

    private final BeerJpaRepository beerRepository;
    private final ManufacturerJpaRepository manufacturerRepository;
    private final OwnershipAuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public Page<Beer> list(Pageable pageable) {
        return beerRepository.findAll(pageable).map(BeerMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Beer getById(Long id) {
        return BeerMapper.toDomain(findBeerById(id));
    }

    @Transactional(readOnly = true)
    public Page<Beer> query(BeerQueryCriteria criteria) {
        Pageable pageable = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(criteria.direction(), resolveSortProperty(criteria.sortBy()))
        );

        return beerRepository.findAll(BeerSpecifications.from(criteria), pageable)
                .map(BeerMapper::toDomain);
    }

    @Transactional
    public Beer create(Beer beer) {
        ManufacturerEntity manufacturer = findManufacturerById(beer.getManufacturerId());
        authorizationService.requireCanManageManufacturer(manufacturer);
        validateUniqueName(beer.getName(), manufacturer.getId());

        BeerEntity saved = beerRepository.save(BeerMapper.toEntity(beer, manufacturer));
        return BeerMapper.toDomain(saved);
    }

    @Transactional
    public Beer update(Long id, Beer beer) {
        BeerEntity existing = findBeerById(id);
        authorizationService.requireCanManageBeer(existing);
        ManufacturerEntity manufacturer = findManufacturerById(beer.getManufacturerId());
        authorizationService.requireCanManageManufacturer(manufacturer);

        if (beerRepository.existsByNameIgnoreCaseAndManufacturerIdAndIdNot(
                beer.getName(),
                manufacturer.getId(),
                id
        )) {
            throw duplicateNameConflict();
        }

        existing.setName(beer.getName());
        existing.setAbv(beer.getAbv());
        existing.setType(beer.getType());
        existing.setDescription(beer.getDescription());
        existing.setManufacturer(manufacturer);

        return BeerMapper.toDomain(beerRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        BeerEntity existing = findBeerById(id);
        authorizationService.requireCanManageBeer(existing);
        beerRepository.delete(existing);
    }

    private void validateUniqueName(String name, Long manufacturerId) {
        if (beerRepository.existsByNameIgnoreCaseAndManufacturerId(name, manufacturerId)) {
            throw duplicateNameConflict();
        }
    }

    private ConflictException duplicateNameConflict() {
        return new ConflictException("A beer with this name already exists for the manufacturer");
    }

    private BeerEntity findBeerById(Long id) {
        return beerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beer with id %d was not found".formatted(id)
                ));
    }

    private ManufacturerEntity findManufacturerById(Long id) {
        return manufacturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manufacturer with id %d was not found".formatted(id)
                ));
    }

    private String resolveSortProperty(String sortBy) {
        return switch (sortBy) {
            case "name", "abv", "type" -> sortBy;
            case MANUFACTURER_NAME_SORT -> "manufacturer.name";
            default -> throw new BadRequestException("Unsupported beer sort field: " + sortBy);
        };
    }
}
