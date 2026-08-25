package com.sightseer.backend.exception;

public class PreferenceNotFoundException extends RuntimeException {
    public PreferenceNotFoundException() {
        super("Preferences not found");
    }

}
