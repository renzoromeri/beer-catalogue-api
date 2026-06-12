package com.renzo.beercatalogue.security.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerEntity;
import com.renzo.beercatalogue.common.exception.ForbiddenOperationException;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.security.domain.Role;
import com.renzo.beercatalogue.security.infrastructure.auth.UserPrincipal;
import com.renzo.beercatalogue.security.infrastructure.persistence.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class OwnershipAuthorizationServiceTest {

    private final OwnershipAuthorizationService service = new OwnershipAuthorizationService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowAdminToManageEverything() {
        authenticate(1L, Role.ADMIN);

        assertThatCode(service::requireAdmin).doesNotThrowAnyException();
        assertThatCode(() -> service.requireCanManageManufacturer(manufacturer(2L)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowManufacturerToManageOwnManufacturerAndBeer() {
        authenticate(2L, Role.MANUFACTURER);
        ManufacturerEntity ownedManufacturer = manufacturer(2L);

        assertThatCode(() -> service.requireCanManageManufacturer(ownedManufacturer))
                .doesNotThrowAnyException();
        assertThatCode(() -> service.requireCanManageBeer(beer(ownedManufacturer)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectManufacturerFromAdminOnlyOperation() {
        authenticate(2L, Role.MANUFACTURER);

        assertThatThrownBy(service::requireAdmin)
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void shouldRejectManufacturerManagingAnotherManufacturerOrBeer() {
        authenticate(2L, Role.MANUFACTURER);
        ManufacturerEntity anotherManufacturer = manufacturer(3L);

        assertThatThrownBy(() -> service.requireCanManageManufacturer(anotherManufacturer))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> service.requireCanManageBeer(beer(anotherManufacturer)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void shouldRejectAnonymousUser() {
        assertThatThrownBy(() -> service.requireCanManageManufacturer(manufacturer(2L)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private void authenticate(Long id, Role role) {
        UserPrincipal principal = new UserPrincipal(id, "user", "password", role, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                )
        );
    }

    private ManufacturerEntity manufacturer(Long ownerId) {
        return ManufacturerEntity.builder()
                .id(10L)
                .name("Manufacturer")
                .countryOfOrigin("Country")
                .owner(UserEntity.builder().id(ownerId).build())
                .build();
    }

    private BeerEntity beer(ManufacturerEntity manufacturer) {
        return BeerEntity.builder()
                .id(20L)
                .manufacturer(manufacturer)
                .build();
    }
}
