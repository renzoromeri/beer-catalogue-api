package com.renzo.beercatalogue.common.pagination;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
