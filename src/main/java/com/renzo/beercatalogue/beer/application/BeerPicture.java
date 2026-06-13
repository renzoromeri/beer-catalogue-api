package com.renzo.beercatalogue.beer.application;

public record BeerPicture(
        byte[] content,
        String contentType,
        long size
) {
}
