package com.renzo.beercatalogue.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class BeerPictureStorageProperties {

    private String beerPicturesDir = "./storage/beer-pictures";
    private long maxFileSizeBytes = 1_048_576;
}
