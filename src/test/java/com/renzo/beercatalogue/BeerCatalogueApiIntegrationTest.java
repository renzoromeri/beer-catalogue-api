package com.renzo.beercatalogue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renzo.beercatalogue.beer.domain.BeerType;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerEntity;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerJpaRepository;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerEntity;
import com.renzo.beercatalogue.manufacturer.infrastructure.persistence.ManufacturerJpaRepository;
import com.renzo.beercatalogue.security.domain.Role;
import com.renzo.beercatalogue.security.infrastructure.persistence.UserEntity;
import com.renzo.beercatalogue.security.infrastructure.persistence.UserJpaRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class BeerCatalogueApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ManufacturerJpaRepository manufacturerRepository;

    @Autowired
    private BeerJpaRepository beerRepository;

    private ManufacturerEntity guinness;
    private ManufacturerEntity heineken;

    @BeforeEach
    void setUp() {
        beerRepository.deleteAll();
        manufacturerRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity guinnessUser = createUser(
                "guinness_user",
                "manufacturer123",
                Role.MANUFACTURER
        );
        UserEntity heinekenUser = createUser(
                "heineken_user",
                "manufacturer123",
                Role.MANUFACTURER
        );
        createUser("admin", "admin123", Role.ADMIN);

        guinness = createManufacturer("Guinness", "Ireland", guinnessUser);
        heineken = createManufacturer("Heineken", "Netherlands", heinekenUser);
        createBeer("Guinness Draught", "4.20", BeerType.STOUT, guinness);
        createBeer("Heineken Lager", "5.00", BeerType.LAGER, heineken);
    }

    @Test
    void publicReadEndpointsShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/beers")).andExpect(status().isOk());
        mockMvc.perform(get("/api/manufacturers")).andExpect(status().isOk());
        mockMvc.perform(jsonPost("/api/beers/query", Map.of())).andExpect(status().isOk());
    }

    @Test
    void validAdminLoginShouldReturnAccessToken() throws Exception {
        mockMvc.perform(jsonPost("/api/auth/login", loginBody("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void invalidLoginShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(jsonPost("/api/auth/login", loginBody("admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousUsersShouldNotWrite() throws Exception {
        mockMvc.perform(jsonPost("/api/manufacturers", manufacturerBody("Brewdog", "Scotland")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(jsonPost("/api/beers", beerBody("New Beer", guinness.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminShouldCreateManufacturerAndBeer() throws Exception {
        String token = loginAsAdmin();
        Long manufacturerId = createManufacturerAsAdmin(token, "Brewdog", "Scotland");

        createBeerAsAdmin(token, "Punk IPA", manufacturerId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Punk IPA"))
                .andExpect(jsonPath("$.manufacturerId").value(manufacturerId));
    }

    @Test
    void manufacturerShouldUpdateOwnManufacturerButNotAnother() throws Exception {
        String token = loginAsGuinnessUser();

        mockMvc.perform(withBearer(
                        put("/api/manufacturers/{id}", guinness.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(manufacturerBody("Guinness Updated", "Ireland"))),
                        token
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Guinness Updated"));

        mockMvc.perform(withBearer(
                        put("/api/manufacturers/{id}", heineken.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(manufacturerBody("Not Allowed", "Netherlands"))),
                        token
                ))
                .andExpect(status().isForbidden());
    }

    @Test
    void manufacturerShouldCreateBeerOnlyForOwnManufacturer() throws Exception {
        String token = loginAsGuinnessUser();

        mockMvc.perform(withBearer(
                        jsonPost("/api/beers", beerBody("Guinness Extra Stout", guinness.getId())),
                        token
                ))
                .andExpect(status().isCreated());

        mockMvc.perform(withBearer(
                        jsonPost("/api/beers", beerBody("Forbidden Lager", heineken.getId())),
                        token
                ))
                .andExpect(status().isForbidden());
    }

    @Test
    void blankManufacturerNameShouldReturnBadRequestWithoutRejectedValue() throws Exception {
        mockMvc.perform(withBearer(
                        jsonPost("/api/manufacturers", manufacturerBody(" ", "Scotland")),
                        loginAsAdmin()
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].rejectedValue").doesNotExist());
    }

    @Test
    void missingBeerShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/beers/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateBeerForSameManufacturerShouldReturnConflict() throws Exception {
        mockMvc.perform(withBearer(
                        jsonPost("/api/beers", beerBody("Guinness Draught", guinness.getId())),
                        loginAsAdmin()
                ))
                .andExpect(status().isConflict());
    }

    @Test
    void queryShouldFilterByName() throws Exception {
        mockMvc.perform(jsonPost("/api/beers/query", Map.of("name", "draught")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Guinness Draught"));
    }

    @Test
    void queryShouldFilterByManufacturerName() throws Exception {
        mockMvc.perform(jsonPost("/api/beers/query", Map.of("manufacturerName", "Heineken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Heineken Lager"));
    }

    @Test
    void emptyQueryShouldReturnPaginatedResponse() throws Exception {
        mockMvc.perform(jsonPost("/api/beers/query", Map.of("size", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    private String loginAsAdmin() throws Exception {
        return login("admin", "admin123");
    }

    private String loginAsGuinnessUser() throws Exception {
        return login("guinness_user", "manufacturer123");
    }

    @SuppressWarnings("unused")
    private String loginAsHeinekenUser() throws Exception {
        return login("heineken_user", "manufacturer123");
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(jsonPost("/api/auth/login", loginBody(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return extractAccessToken(result);
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private MockHttpServletRequestBuilder withBearer(
            MockHttpServletRequestBuilder request,
            String token
    ) {
        return request.header("Authorization", bearer(token));
    }

    private Long createManufacturerAsAdmin(String token, String name, String country) throws Exception {
        MvcResult result = mockMvc.perform(withBearer(
                        jsonPost("/api/manufacturers", manufacturerBody(name, country)),
                        token
                ))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions createBeerAsAdmin(
            String token,
            String name,
            Long manufacturerId
    ) throws Exception {
        return mockMvc.perform(withBearer(
                jsonPost("/api/beers", beerBody(name, manufacturerId)),
                token
        ));
    }

    private MockHttpServletRequestBuilder jsonPost(String path, Object body) throws Exception {
        return post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private Map<String, Object> loginBody(String username, String password) {
        return Map.of("username", username, "password", password);
    }

    private Map<String, Object> manufacturerBody(String name, String country) {
        return Map.of("name", name, "countryOfOrigin", country);
    }

    private Map<String, Object> beerBody(String name, Long manufacturerId) {
        return Map.of(
                "name", name,
                "abv", 5.0,
                "type", "LAGER",
                "description", "Integration test beer",
                "manufacturerId", manufacturerId
        );
    }

    private UserEntity createUser(String username, String password, Role role) {
        return userRepository.save(UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .enabled(true)
                .build());
    }

    private ManufacturerEntity createManufacturer(
            String name,
            String country,
            UserEntity owner
    ) {
        return manufacturerRepository.save(ManufacturerEntity.builder()
                .name(name)
                .countryOfOrigin(country)
                .owner(owner)
                .build());
    }

    private void createBeer(
            String name,
            String abv,
            BeerType type,
            ManufacturerEntity manufacturer
    ) {
        beerRepository.save(BeerEntity.builder()
                .name(name)
                .abv(new BigDecimal(abv))
                .type(type)
                .description("Integration test beer")
                .manufacturer(manufacturer)
                .build());
    }
}
