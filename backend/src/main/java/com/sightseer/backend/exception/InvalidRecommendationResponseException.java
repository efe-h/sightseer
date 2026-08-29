package com.sightseer.backend.exception;

public class InvalidRecommendationResponseException
        extends RuntimeException {

    public InvalidRecommendationResponseException() {
        super("The recommendation service returned an invalid response");
    }
}