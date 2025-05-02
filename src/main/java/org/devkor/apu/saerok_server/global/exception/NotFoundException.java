package org.devkor.apu.saerok_server.global.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    
    private final String resourceName;
    
    private final String fieldName;
    
    private final Object fieldValue;
    
    public NotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
