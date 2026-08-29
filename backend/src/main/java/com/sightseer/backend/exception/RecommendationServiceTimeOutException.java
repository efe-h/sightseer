package com.sightseer.backend.exception;

public class RecommendationServiceTimeOutException
        extends RuntimeException {

    public RecommendationServiceTimeOutException() {
        super("The recommendation service took too long to respond");
    }
}