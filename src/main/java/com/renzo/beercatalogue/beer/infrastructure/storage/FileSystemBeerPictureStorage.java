package com.renzo.beercatalogue.beer.infrastructure.storage;

import com.renzo.beercatalogue.beer.application.port.BeerPictureStorage;
import com.renzo.beercatalogue.config.BeerPictureStorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileSystemBeerPictureStorage implements BeerPictureStorage {

    private final BeerPictureStorageProperties properties;

    @Override
    public void store(String fileName, byte[] content) {
        Path target = resolve(fileName);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException exception) {
            throw storageFailure(exception);
        }
    }

    @Override
    public Optional<byte[]> load(String fileName) {
        Path target = resolve(fileName);
        if (!Files.isRegularFile(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(target));
        } catch (IOException exception) {
            throw storageFailure(exception);
        }
    }

    @Override
    public void deleteIfExists(String fileName) {
        if (fileName == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(fileName));
        } catch (IOException exception) {
            throw storageFailure(exception);
        }
    }

    private Path resolve(String fileName) {
        Path directory = Path.of(properties.getBeerPicturesDir()).toAbsolutePath().normalize();
        Path target = directory.resolve(fileName).normalize();
        if (!target.startsWith(directory)) {
            throw new IllegalArgumentException("Invalid picture file name");
        }
        return target;
    }

    private IllegalStateException storageFailure(IOException exception) {
        return new IllegalStateException("Unable to access beer picture storage", exception);
    }
}
