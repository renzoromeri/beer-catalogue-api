package com.renzo.beercatalogue.beer.application.port;

import java.util.Optional;

public interface BeerPictureStorage {

    void store(String fileName, byte[] content);

    Optional<byte[]> load(String fileName);

    void deleteIfExists(String fileName);
}
