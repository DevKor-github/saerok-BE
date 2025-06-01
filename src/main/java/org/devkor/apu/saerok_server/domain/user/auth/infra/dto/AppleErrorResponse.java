package org.devkor.apu.saerok_server.domain.user.auth.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppleErrorResponse {

    @JsonProperty("error")
    private String error;
}
