package com.example.library.utils;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.ErrorResponse;

/**
 * CrudApiException is a custom exception that extends Exception. It is used to handle exceptions in the
 * application whose messages are safe to be known by the consumers of the API.
 */
@Getter
public class RepositoryException extends Exception {
    private final int statusCode;

    /**
     * Creates a new exception that represents an intentional and controlled interruption of the execution flow.
     *
     * @param message    - the exception message
     * @param statusCode - the status code of the exception
     */
    public RepositoryException(@NonNull String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode.value();
    }

    /**
     * Converts the exception to a {@link ResponseEntity} with the status code and message.
     *
     * @return the {@link ResponseEntity} with the status code and message
     */
    public @NonNull ResponseEntity<ErrorResponse> toResponseEntity() {
        return ResponseEntity.status(this.getStatusCode()).body(this.toErrorResponse());
    }

    /**
     * Converts the exception to an {@link ErrorResponse} with the status code and message.
     *
     * @return the {@link ErrorResponse} with the status code and message
     */
    public @NonNull ErrorResponse toErrorResponse() {
        return new ErrorResponseImpl(HttpStatus.valueOf(this.getStatusCode()), this.getMessage());
    }

    /**
     * Exception thrown when a resource is not found.
     */
    public static class NotFound extends RepositoryException {
        /**
         * Creates a new NotFound exception with the given message.
         *
         * @param message - the exception message
         */
        public NotFound(@NonNull String message) {
            super(message, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Exception thrown when a resource is not found.
     */
    public static class Conflict extends RepositoryException {
        /**
         * Creates a new Conflict exception with the given message.
         *
         * @param message - the exception message
         */
        public Conflict(@NonNull String message) {
            super(message, HttpStatus.CONFLICT);
        }
    }

    /**
     * Exception thrown when a resource is not found.
     */
    public static class BadRequest extends RepositoryException {
        /**
         * Creates a new BadRequest exception with the given message.
         *
         * @param message - the exception message
         */
        public BadRequest(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Exception thrown when a resource is not found.
     */
    public static final class InternalServerError extends RepositoryException {
        /**
         * Creates a new InternalServerError exception with the given message.
         *
         * @param message - the exception message
         */
        public InternalServerError(String message) {
            super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


