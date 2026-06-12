package com.renzo.beercatalogue.manufacturer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.renzo.beercatalogue.common.exception.ConflictException;
import com.renzo.beercatalogue.common.exception.ResourceNotFoundException;
import com.renzo.beercatalogue.manufacturer.domain.Manufacturer;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerJpaRepository;
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

    private ManufacturerService service;

    @BeforeEach
    void setUp() {
        service = new ManufacturerService(repository);
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
        verify(repository).save(existing);
    }

    @Test
    void shouldDeleteManufacturer() {
        ManufacturerEntity existing = entity(1L, "Guinness", "Ireland");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
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
