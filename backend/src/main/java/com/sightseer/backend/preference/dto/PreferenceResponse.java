package com.sightseer.backend.preference.dto;

public record PreferenceResponse(
        Integer history,
        Integer art,
        Integer architecture,
        Integer nature,
        Integer science,
        Integer food,
        Integer entertainment,
        Integer shopping,
        Integer views,
        Integer family) {
}
