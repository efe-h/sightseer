package com.sightseer.backend.exception;

public class InvalidTokenClaimsException extends RuntimeException {
    public InvalidTokenClaimsException(String message) {
        super(message);
    }

}
