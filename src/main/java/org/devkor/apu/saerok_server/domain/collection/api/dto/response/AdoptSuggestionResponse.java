package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

public record AdoptSuggestionResponse(Long collectionId,
                                      Long birdId,
                                      String birdName) {}