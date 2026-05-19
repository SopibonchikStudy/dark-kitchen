// TODO: Implement ResourceNotFoundException.java
// exception/ResourceNotFoundException.java
package edu.rutmiit.demo.orderservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s with id=%s not found", resourceName, resourceId));
    }
}