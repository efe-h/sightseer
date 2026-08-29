package com.sightseer.backend.exception;

public class RecommendationServiceUnavailableException
        extends RuntimeException {

    public RecommendationServiceUnavailableException() {
        super("The recommendation service is currently unavailable");
    }
}