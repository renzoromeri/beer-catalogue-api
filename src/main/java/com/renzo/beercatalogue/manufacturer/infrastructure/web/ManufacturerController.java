package com.renzo.beercatalogue.manufacturer.infrastructure.web;

import com.renzo.beercatalogue.common.pagination.PageResponse;
import com.renzo.beercatalogue.manufacturer.application.ManufacturerService;
import com.renzo.beercatalogue.manufacturer.domain.Manufacturer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manufacturers")
@RequiredArgsConstructor
@Tag(name = "Manufacturers")
public class ManufacturerController {

    private final ManufacturerService service;

    @GetMapping
    @Operation(summary = "List manufacturers", security = {})
    public PageResponse<ManufacturerResponse> list(
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        Page<ManufacturerResponse> page = service.list(pageable)
                .map(ManufacturerWebMapper::toResponse);

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a manufacturer by id", security = {})
    public ManufacturerResponse getById(@PathVariable Long id) {
        return ManufacturerWebMapper.toResponse(service.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a manufacturer")
    public ResponseEntity<ManufacturerResponse> create(
            @Valid @RequestBody ManufacturerCreateRequest request
    ) {
        Manufacturer created = service.create(ManufacturerWebMapper.toDomain(request));

        return ResponseEntity
                .created(URI.create("/api/manufacturers/" + created.getId()))
                .body(ManufacturerWebMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a manufacturer")
    public ManufacturerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ManufacturerUpdateRequest request
    ) {
        return ManufacturerWebMapper.toResponse(
                service.update(id, ManufacturerWebMapper.toDomain(request))
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a manufacturer")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
