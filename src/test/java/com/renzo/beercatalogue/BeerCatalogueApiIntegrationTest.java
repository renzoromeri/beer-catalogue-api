package com.renzo.beercatalogue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockMultipartFile;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class BeerCatalogueApiIntegrationTest {

    @TempDir
    static Path pictureStorageDirectory;

    @DynamicPropertySource
    static void pictureStorageProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "app.storage.beer-pictures-dir",
                () -> pictureStorageDirectory.toString()
        );
    }

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
    private BeerEntity guinnessBeer;
    private BeerEntity heinekenBeer;

    @BeforeEach
    void setUp() throws IOException {
        try (var files = Files.list(pictureStorageDirectory)) {
            files.forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            });
        }
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
        guinnessBeer = createBeer("Guinness Draught", "4.20", BeerType.STOUT, guinness);
        heinekenBeer = createBeer("Heineken Lager", "5.00", BeerType.LAGER, heineken);
    }

    @Test
    void publicReadEndpointsShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/beers")).andExpect(status().isOk());
        mockMvc.perform(get("/api/manufacturers")).andExpect(status().isOk());
        mockMvc.perform(jsonPost("/api/beers/query", Map.of())).andExpect(status().isOk());
    }

    @Test
    void openApiDocsShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Beer Catalogue API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists());
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

    @Test
    void adminShouldUploadPictureForAnyBeerAndPictureShouldBePublic() throws Exception {
        byte[] picture = "jpeg-content".getBytes();

        mockMvc.perform(withBearer(
                        pictureUpload(heinekenBeer.getId(), "image/jpeg", picture),
                        loginAsAdmin()
                ))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/beers/{id}/picture", heinekenBeer.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes(picture));
    }

    @Test
    void ownerManufacturerShouldUploadPictureForOwnBeer() throws Exception {
        mockMvc.perform(withBearer(
                        pictureUpload(guinnessBeer.getId(), "image/png", "png-content".getBytes()),
                        loginAsGuinnessUser()
                ))
                .andExpect(status().isNoContent());
    }

    @Test
    void nonOwnerManufacturerShouldNotUploadPicture() throws Exception {
        mockMvc.perform(withBearer(
                        pictureUpload(heinekenBeer.getId(), "image/webp", "webp-content".getBytes()),
                        loginAsGuinnessUser()
                ))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserShouldNotUploadPicture() throws Exception {
        mockMvc.perform(pictureUpload(
                        guinnessBeer.getId(),
                        "image/png",
                        "png-content".getBytes()
                ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void beerWithoutPictureShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/beers/{id}/picture", guinnessBeer.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidPictureContentTypeShouldReturnBadRequest() throws Exception {
        mockMvc.perform(withBearer(
                        pictureUpload(guinnessBeer.getId(), "text/plain", "not-an-image".getBytes()),
                        loginAsAdmin()
                ))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyPictureShouldReturnBadRequest() throws Exception {
        mockMvc.perform(withBearer(
                        pictureUpload(guinnessBeer.getId(), "image/png", new byte[0]),
                        loginAsAdmin()
                ))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oversizedPictureShouldReturnBadRequest() throws Exception {
        byte[] oversizedPicture = new byte[1_048_577];

        mockMvc.perform(withBearer(
                        pictureUpload(guinnessBeer.getId(), "image/png", oversizedPicture),
                        loginAsAdmin()
                ))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadForMissingBeerShouldReturnNotFound() throws Exception {
        mockMvc.perform(withBearer(
                        pictureUpload(Long.MAX_VALUE, "image/png", "png-content".getBytes()),
                        loginAsAdmin()
                ))
                .andExpect(status().isNotFound());
    }

    @Test
    void replacingPictureShouldDeletePreviousFile() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(withBearer(
                        pictureUpload(guinnessBeer.getId(), "image/png", "first".getBytes()),
                        token
                ))
                .andExpect(status().isNoContent());
        mockMvc.perform(withBearer(
                        pictureUpload(guinnessBeer.getId(), "image/webp", "second".getBytes()),
                        token
                ))
                .andExpect(status().isNoContent());

        try (var files = Files.list(pictureStorageDirectory)) {
            org.assertj.core.api.Assertions.assertThat(files).hasSize(1);
        }
    }

    @Test
    void deletingBeerShouldDeletePictureFile() throws Exception {
        String token = loginAsAdmin();
        mockMvc.perform(withBearer(
                        pictureUpload(guinnessBeer.getId(), "image/png", "picture".getBytes()),
                        token
                ))
                .andExpect(status().isNoContent());

        mockMvc.perform(withBearer(delete("/api/beers/{id}", guinnessBeer.getId()), token))
                .andExpect(status().isNoContent());

        try (var files = Files.list(pictureStorageDirectory)) {
            org.assertj.core.api.Assertions.assertThat(files).isEmpty();
        }
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

    private MockHttpServletRequestBuilder pictureUpload(
            Long beerId,
            String contentType,
            byte[] content
    ) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "untrusted-name",
                contentType,
                content
        );
        return multipart("/api/beers/{id}/picture", beerId).file(file);
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

    private BeerEntity createBeer(
            String name,
            String abv,
            BeerType type,
            ManufacturerEntity manufacturer
    ) {
        return beerRepository.save(BeerEntity.builder()
                .name(name)
                .abv(new BigDecimal(abv))
                .type(type)
                .description("Integration test beer")
                .manufacturer(manufacturer)
                .build());
    }
}
