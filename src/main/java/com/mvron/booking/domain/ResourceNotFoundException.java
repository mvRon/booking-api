package com.mvron.booking.domain;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(long resourceId) {
        super("resource not found: " + resourceId);
    }
    
}
