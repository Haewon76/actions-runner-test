package com.cashmallow.api.interfaces.authme.dto;

public record TravelerImageSender(
        Long travelerId,
        String fileName,
        String base64Image,
        String type
) {
    public TravelerImageSender(TravelerImage image, String base64Image) {
        this(image.getTravelerId(), image.getFileName(), base64Image, image.getType());
    }
}
