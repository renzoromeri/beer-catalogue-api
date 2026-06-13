package com.renzo.beercatalogue.beer.application;

import com.renzo.beercatalogue.beer.application.port.BeerPictureStorage;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerEntity;
import com.renzo.beercatalogue.beer.infrastructure.persistence.BeerJpaRepository;
import com.renzo.beercatalogue.common.exception.BadRequestException;
import com.renzo.beercatalogue.common.exception.ResourceNotFoundException;
import com.renzo.beercatalogue.config.BeerPictureStorageProperties;
import com.renzo.beercatalogue.security.application.OwnershipAuthorizationService;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BeerPictureService {

    private static final Map<String, String> FILE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );
    private static final Set<String> ALLOWED_CONTENT_TYPES = FILE_EXTENSIONS.keySet();

    private final BeerJpaRepository beerRepository;
    private final BeerPictureStorage storage;
    private final BeerPictureStorageProperties properties;
    private final OwnershipAuthorizationService authorizationService;

    @Transactional
    public void upload(Long beerId, byte[] content, String contentType) {
        BeerEntity beer = findBeerById(beerId);
        authorizationService.requireCanManageBeer(beer);
        validate(content, contentType);

        String newFileName = safeFileName(beerId, contentType);
        String previousFileName = beer.getPictureFileName();

        storage.store(newFileName, content);
        beer.setPictureFileName(newFileName);
        beer.setPictureContentType(contentType);
        beer.setPictureSize((long) content.length);
        beerRepository.save(beer);

        if (previousFileName != null && !previousFileName.equals(newFileName)) {
            storage.deleteIfExists(previousFileName);
        }
    }

    @Transactional(readOnly = true)
    public BeerPicture get(Long beerId) {
        BeerEntity beer = findBeerById(beerId);
        if (beer.getPictureFileName() == null || beer.getPictureContentType() == null) {
            throw pictureNotFound();
        }

        byte[] content = storage.load(beer.getPictureFileName())
                .orElseThrow(this::pictureNotFound);
        return new BeerPicture(content, beer.getPictureContentType(), content.length);
    }

    private void validate(byte[] content, String contentType) {
        if (content == null || content.length == 0) {
            throw new BadRequestException("Picture file must not be empty");
        }
        if (content.length > properties.getMaxFileSizeBytes()) {
            throw new BadRequestException("Picture file exceeds the maximum allowed size");
        }
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Picture content type must be JPEG, PNG, or WebP");
        }
    }

    private String safeFileName(Long beerId, String contentType) {
        return beerId + "-" + UUID.randomUUID() + FILE_EXTENSIONS.get(contentType);
    }

    private BeerEntity findBeerById(Long beerId) {
        return beerRepository.findById(beerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beer with id %d was not found".formatted(beerId)
                ));
    }

    private ResourceNotFoundException pictureNotFound() {
        return new ResourceNotFoundException("Beer picture was not found");
    }
}
