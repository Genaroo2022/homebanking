package com.homebanking.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ErrorResponse(
        @JsonProperty("error")
        String error,

        @JsonProperty("message")
        String message,

        @JsonProperty("timestamp")
        String timestamp
) {

    public static ErrorResponse of(String error, String message) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .timestamp(java.time.Instant.now().toString())
                .build();
    }
}

