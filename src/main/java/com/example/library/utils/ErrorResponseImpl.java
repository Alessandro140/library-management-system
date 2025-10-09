package com.example.library.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.web.ErrorResponse;

/**
 * The response body of the exception.
 */
public record ErrorResponseImpl(@NonNull HttpStatus statusCode, @NonNull String message) implements ErrorResponse {
    /**
     * Returns the status code of the response.
     *
     * @return the status code of the response
     */
    @Override
    @NonNull
    public HttpStatusCode getStatusCode() {
        return this.statusCode;
    }

    /**
     * Returns the body of the response.
     *
     * @return the body of the response
     */
    @Override
    @NonNull
    public ProblemDetail getBody() {
        return ProblemDetail.forStatusAndDetail(this.statusCode, this.message);
    }
}