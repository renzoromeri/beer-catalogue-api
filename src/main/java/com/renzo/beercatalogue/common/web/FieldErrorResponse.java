package com.renzo.beercatalogue.common.web;

public record FieldErrorResponse(
        String field,
        String message,
        Object rejectedValue
) {
}
