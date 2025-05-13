package org.devkor.apu.saerok_server.domain.collection.application.dto;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CollectionCommandService {

    public Long createCollection(CreateCollectionCommand command) {
        return 0L;
    }
}
