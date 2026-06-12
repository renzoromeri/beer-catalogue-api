package com.renzo.beercatalogue.beer.infrastructure.web;

import com.renzo.beercatalogue.beer.application.BeerService;
import com.renzo.beercatalogue.beer.domain.Beer;
import com.renzo.beercatalogue.common.pagination.PageResponse;
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
@RequestMapping("/api/beers")
@RequiredArgsConstructor
@Tag(name = "Beers")
public class BeerController {

    private final BeerService service;

    @GetMapping
    @Operation(summary = "List beers", security = {})
    public PageResponse<BeerResponse> list(
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        Page<BeerResponse> page = service.list(pageable).map(BeerWebMapper::toResponse);

        return toPageResponse(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a beer by id", security = {})
    public BeerResponse getById(@PathVariable Long id) {
        return BeerWebMapper.toResponse(service.getById(id));
    }

    @PostMapping("/query")
    @Operation(summary = "Query beers with optional filters", security = {})
    public PageResponse<BeerResponse> query(@Valid @RequestBody BeerQueryRequest request) {
        Page<BeerResponse> page = service.query(BeerWebMapper.toCriteria(request))
                .map(BeerWebMapper::toResponse);

        return toPageResponse(page);
    }

    @PostMapping
    @Operation(summary = "Create a beer")
    public ResponseEntity<BeerResponse> create(@Valid @RequestBody BeerCreateRequest request) {
        Beer created = service.create(BeerWebMapper.toDomain(request));

        return ResponseEntity
                .created(URI.create("/api/beers/" + created.getId()))
                .body(BeerWebMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a beer")
    public BeerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody BeerUpdateRequest request
    ) {
        return BeerWebMapper.toResponse(service.update(id, BeerWebMapper.toDomain(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a beer")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private PageResponse<BeerResponse> toPageResponse(Page<BeerResponse> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
