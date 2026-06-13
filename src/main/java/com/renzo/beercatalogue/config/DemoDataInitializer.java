package com.renzo.beercatalogue.config;

import com.renzo.beercatalogue.beer.domain.BeerType;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerEntity;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerJpaRepository;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerJpaRepository;
import com.renzo.beercatalogue.security.domain.Role;
import com.renzo.beercatalogue.security.infrastructure.persistence.UserEntity;
import com.renzo.beercatalogue.security.infrastructure.persistence.UserJpaRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.init-demo-data",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DemoDataInitializer implements ApplicationRunner {

    private final UserJpaRepository userRepository;
    private final ManufacturerJpaRepository manufacturerRepository;
    private final BeerJpaRepository beerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        findOrCreateUser("admin", "admin123", Role.ADMIN);
        UserEntity guinnessUser = findOrCreateUser(
                "guinness_user",
                "manufacturer123",
                Role.MANUFACTURER
        );
        UserEntity heinekenUser = findOrCreateUser(
                "heineken_user",
                "manufacturer123",
                Role.MANUFACTURER
        );

        ManufacturerEntity guinness = findOrCreateManufacturer("Guinness", "Ireland", guinnessUser);
        ManufacturerEntity heineken = findOrCreateManufacturer(
                "Heineken",
                "Netherlands",
                heinekenUser
        );

        createBeerIfMissing(
                "Guinness Draught",
                new BigDecimal("4.2"),
                BeerType.STOUT,
                "Irish dry stout",
                guinness
        );
        createBeerIfMissing(
                "Heineken Lager",
                new BigDecimal("5.0"),
                BeerType.LAGER,
                "Dutch pale lager",
                heineken
        );
    }

    private UserEntity findOrCreateUser(String username, String password, Role role) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(UserEntity.builder()
                        .username(username)
                        .password(passwordEncoder.encode(password))
                        .role(role)
                        .enabled(true)
                        .build()));
    }

    private ManufacturerEntity findOrCreateManufacturer(
            String name,
            String countryOfOrigin,
            UserEntity owner
    ) {
        ManufacturerEntity manufacturer = manufacturerRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> ManufacturerEntity.builder()
                        .name(name)
                        .countryOfOrigin(countryOfOrigin)
                        .build());

        manufacturer.setOwner(owner);
        return manufacturerRepository.save(manufacturer);
    }

    private void createBeerIfMissing(
            String name,
            BigDecimal abv,
            BeerType type,
            String description,
            ManufacturerEntity manufacturer
    ) {
        if (beerRepository.existsByNameIgnoreCaseAndManufacturerId(name, manufacturer.getId())) {
            return;
        }

        beerRepository.save(BeerEntity.builder()
                .name(name)
                .abv(abv)
                .type(type)
                .description(description)
                .manufacturer(manufacturer)
                .build());
    }
}
