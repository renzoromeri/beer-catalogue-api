package com.renzo.beercatalogue.manufacturer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerJpaRepository;
import com.renzo.beercatalogue.common.exception.ConflictException;
import com.renzo.beercatalogue.common.exception.ResourceNotFoundException;
import com.renzo.beercatalogue.manufacturer.domain.Manufacturer;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerJpaRepository;
import com.renzo.beercatalogue.security.application.OwnershipAuthorizationService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManufacturerServiceTest {

    @Mock
    private ManufacturerJpaRepository repository;

    @Mock
    private BeerJpaRepository beerRepository;

    @Mock
    private OwnershipAuthorizationService authorizationService;

    private ManufacturerService service;

    @BeforeEach
    void setUp() {
        service = new ManufacturerService(repository, beerRepository, authorizationService);
    }

    @Test
    void shouldCreateManufacturer() {
        Manufacturer manufacturer = manufacturer(null, "Guinness", "Ireland");
        ManufacturerEntity saved = entity(1L, "Guinness", "Ireland");
        when(repository.existsByNameIgnoreCase("Guinness")).thenReturn(false);
        when(repository.save(any(ManufacturerEntity.class))).thenReturn(saved);

        Manufacturer result = service.create(manufacturer);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Guinness");
        verify(authorizationService).requireAdmin();
    }

    @Test
    void shouldRejectDuplicateManufacturerOnCreate() {
        Manufacturer manufacturer = manufacturer(null, "Guinness", "Ireland");
        when(repository.existsByNameIgnoreCase("Guinness")).thenReturn(true);

        assertThatThrownBy(() -> service.create(manufacturer))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFindManufacturerById() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L, "Guinness", "Ireland")));

        Manufacturer result = service.getById(1L);

        assertThat(result.getName()).isEqualTo("Guinness");
    }

    @Test
    void shouldThrowWhenManufacturerDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateManufacturer() {
        ManufacturerEntity existing = entity(1L, "Old name", "Ireland");
        Manufacturer update = manufacturer(null, "Guinness", "Ireland");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByNameIgnoreCaseAndIdNot("Guinness", 1L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);

        Manufacturer result = service.update(1L, update);

        assertThat(result.getName()).isEqualTo("Guinness");
        verify(authorizationService).requireCanManageManufacturer(existing);
        verify(repository).save(existing);
    }

    @Test
    void shouldDeleteManufacturerWithoutBeers() {
        ManufacturerEntity existing = entity(1L, "Guinness", "Ireland");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(beerRepository.existsByManufacturerId(1L)).thenReturn(false);

        service.delete(1L);

        verify(authorizationService).requireAdmin();
        verify(repository).delete(existing);
    }

    @Test
    void shouldRejectDeleteWhenManufacturerDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(beerRepository, never()).existsByManufacturerId(any());
        verify(repository, never()).delete(any());
    }

    @Test
    void shouldRejectDeleteWhenManufacturerHasBeers() {
        ManufacturerEntity existing = entity(1L, "Guinness", "Ireland");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(beerRepository.existsByManufacturerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("associated beers");
        verify(repository, never()).delete(any());
    }

    private Manufacturer manufacturer(Long id, String name, String countryOfOrigin) {
        return Manufacturer.builder()
                .id(id)
                .name(name)
                .countryOfOrigin(countryOfOrigin)
                .build();
    }

    private ManufacturerEntity entity(Long id, String name, String countryOfOrigin) {
        return ManufacturerEntity.builder()
                .id(id)
                .name(name)
                .countryOfOrigin(countryOfOrigin)
                .build();
    }
}
