package com.renzo.beercatalogue.beer.infrastructure.web;

import com.renzo.beercatalogue.beer.application.BeerPicture;
import com.renzo.beercatalogue.beer.application.BeerPictureService;
import com.renzo.beercatalogue.common.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/beers/{beerId}/picture")
@RequiredArgsConstructor
@Tag(name = "Beers")
public class BeerPictureController {

    private final BeerPictureService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or replace a beer picture")
    public ResponseEntity<Void> upload(
            @PathVariable Long beerId,
            @RequestPart("file") MultipartFile file
    ) {
        service.upload(beerId, content(file), file.getContentType());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get a beer picture", security = {})
    public ResponseEntity<byte[]> get(@PathVariable Long beerId) {
        BeerPicture picture = service.get(beerId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(picture.contentType()))
                .contentLength(picture.size())
                .body(picture.content());
    }

    private byte[] content(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BadRequestException("Picture file could not be read");
        }
    }
}
