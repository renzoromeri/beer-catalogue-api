package com.renzo.beercatalogue.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerJpaRepository;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerJpaRepository;
import com.renzo.beercatalogue.security.infrastructure.persistence.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@DataJpaTest
class DemoDataInitializerTest {

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ManufacturerJpaRepository manufacturerRepository;

    @Autowired
    private BeerJpaRepository beerRepository;

    @Test
    void shouldCreateDemoDataWithoutDuplicates() throws Exception {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        DemoDataInitializer initializer = new DemoDataInitializer(
                userRepository,
                manufacturerRepository,
                beerRepository,
                passwordEncoder
        );

        initializer.run(null);
        initializer.run(null);

        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(manufacturerRepository.count()).isEqualTo(2);
        assertThat(beerRepository.count()).isEqualTo(2);
        assertThat(passwordEncoder.matches(
                "admin123",
                userRepository.findByUsername("admin").orElseThrow().getPassword()
        )).isTrue();
        assertThat(
                manufacturerRepository.findByNameIgnoreCase("Guinness")
                        .orElseThrow()
                        .getOwner()
                        .getUsername()
        ).isEqualTo("guinness_user");
        assertThat(
                manufacturerRepository.findByNameIgnoreCase("Heineken")
                        .orElseThrow()
                        .getOwner()
                        .getUsername()
        ).isEqualTo("heineken_user");
    }
}
