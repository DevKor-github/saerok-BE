package org.devkor.apu.saerok_server.domain.collection.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CollectionQueryService {
}
