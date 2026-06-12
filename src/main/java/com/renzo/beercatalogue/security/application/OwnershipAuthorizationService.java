package com.renzo.beercatalogue.security.application;

import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerEntity;
import com.renzo.beercatalogue.common.exception.ForbiddenOperationException;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.security.domain.Role;
import com.renzo.beercatalogue.security.infrastructure.auth.UserPrincipal;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OwnershipAuthorizationService {

    public void requireAdmin() {
        if (!isAdmin(currentUser())) {
            throw forbidden();
        }
    }

    public void requireCanManageManufacturer(ManufacturerEntity manufacturer) {
        UserPrincipal user = currentUser();

        if (isAdmin(user)) {
            return;
        }

        if (user.getRole() == Role.MANUFACTURER
                && manufacturer.getOwner() != null
                && Objects.equals(manufacturer.getOwner().getId(), user.getId())) {
            return;
        }

        throw forbidden();
    }

    public void requireCanManageBeer(BeerEntity beer) {
        requireCanManageManufacturer(beer.getManufacturer());
    }

    private UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal user)) {
            throw forbidden();
        }

        return user;
    }

    private boolean isAdmin(UserPrincipal user) {
        return user.getRole() == Role.ADMIN;
    }

    private ForbiddenOperationException forbidden() {
        return new ForbiddenOperationException("You are not allowed to manage this resource");
    }
}
