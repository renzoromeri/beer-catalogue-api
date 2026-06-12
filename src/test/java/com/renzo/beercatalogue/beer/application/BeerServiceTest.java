package com.renzo.beercatalogue.beer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.renzo.beercatalogue.beer.domain.Beer;
import com.renzo.beercatalogue.beer.domain.BeerType;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerEntity;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerJpaRepository;
import com.renzo.beercatalogue.common.exception.ConflictException;
import com.renzo.beercatalogue.common.exception.ResourceNotFoundException;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerJpaRepository;
import com.renzo.beercatalogue.security.application.OwnershipAuthorizationService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeerServiceTest {

    @Mock
    private BeerJpaRepository beerRepository;

    @Mock
    private ManufacturerJpaRepository manufacturerRepository;

    @Mock
    private OwnershipAuthorizationService authorizationService;

    private BeerService service;

    @BeforeEach
    void setUp() {
        service = new BeerService(beerRepository, manufacturerRepository, authorizationService);
    }

    @Test
    void shouldCreateBeer() {
        ManufacturerEntity manufacturer = manufacturer(1L, "Guinness");
        Beer beer = beer(null, "Guinness Draught", 1L);
        BeerEntity saved = entity(10L, "Guinness Draught", manufacturer);
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(manufacturer));
        when(beerRepository.existsByNameIgnoreCaseAndManufacturerId("Guinness Draught", 1L))
                .thenReturn(false);
        when(beerRepository.save(any(BeerEntity.class))).thenReturn(saved);

        Beer result = service.create(beer);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getManufacturerName()).isEqualTo("Guinness");
        verify(authorizationService).requireCanManageManufacturer(manufacturer);
    }

    @Test
    void shouldRejectDuplicateBeerOnCreate() {
        ManufacturerEntity manufacturer = manufacturer(1L, "Guinness");
        Beer beer = beer(null, "Guinness Draught", 1L);
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(manufacturer));
        when(beerRepository.existsByNameIgnoreCaseAndManufacturerId("Guinness Draught", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(beer)).isInstanceOf(ConflictException.class);
        verify(beerRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreateWhenManufacturerDoesNotExist() {
        Beer beer = beer(null, "Guinness Draught", 99L);
        when(manufacturerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(beer))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(beerRepository, never()).save(any());
    }

    @Test
    void shouldFindBeerById() {
        BeerEntity entity = entity(10L, "Guinness Draught", manufacturer(1L, "Guinness"));
        when(beerRepository.findById(10L)).thenReturn(Optional.of(entity));

        Beer result = service.getById(10L);

        assertThat(result.getName()).isEqualTo("Guinness Draught");
    }

    @Test
    void shouldThrowWhenBeerDoesNotExist() {
        when(beerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateBeer() {
        ManufacturerEntity oldManufacturer = manufacturer(1L, "Guinness");
        ManufacturerEntity newManufacturer = manufacturer(2L, "Heineken");
        BeerEntity existing = entity(10L, "Old name", oldManufacturer);
        Beer update = beer(null, "Heineken Lager", 2L);
        when(beerRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(manufacturerRepository.findById(2L)).thenReturn(Optional.of(newManufacturer));
        when(beerRepository.existsByNameIgnoreCaseAndManufacturerIdAndIdNot(
                "Heineken Lager", 2L, 10L
        )).thenReturn(false);
        when(beerRepository.save(existing)).thenReturn(existing);

        Beer result = service.update(10L, update);

        assertThat(result.getName()).isEqualTo("Heineken Lager");
        assertThat(result.getManufacturerId()).isEqualTo(2L);
        verify(authorizationService).requireCanManageBeer(existing);
        verify(authorizationService).requireCanManageManufacturer(newManufacturer);
        verify(beerRepository).save(existing);
    }

    @Test
    void shouldDeleteBeer() {
        BeerEntity existing = entity(10L, "Guinness Draught", manufacturer(1L, "Guinness"));
        when(beerRepository.findById(10L)).thenReturn(Optional.of(existing));

        service.delete(10L);

        verify(authorizationService).requireCanManageBeer(existing);
        verify(beerRepository).delete(existing);
    }

    private Beer beer(Long id, String name, Long manufacturerId) {
        return Beer.builder()
                .id(id)
                .name(name)
                .abv(new BigDecimal("4.20"))
                .type(BeerType.STOUT)
                .description("Description")
                .manufacturerId(manufacturerId)
                .build();
    }

    private BeerEntity entity(Long id, String name, ManufacturerEntity manufacturer) {
        return BeerEntity.builder()
                .id(id)
                .name(name)
                .abv(new BigDecimal("4.20"))
                .type(BeerType.STOUT)
                .description("Description")
                .manufacturer(manufacturer)
                .build();
    }

    private ManufacturerEntity manufacturer(Long id, String name) {
        return ManufacturerEntity.builder()
                .id(id)
                .name(name)
                .countryOfOrigin("Country")
                .build();
    }
}
