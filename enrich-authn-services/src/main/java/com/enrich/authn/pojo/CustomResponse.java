package com.enrich.authn.pojo;

import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CustomResponse {
    private boolean success;
    private String systemMessage;
    private String systemMessageType;
    private Map<String, String> data;

    public CustomResponse(boolean success, String systemMessage, String systemMessageType, Map<String, String> data) {
        this.success = success;
        this.systemMessage = systemMessage;
        this.systemMessageType = systemMessageType;
        this.data = data;
    }

    // Getters and setters
}
