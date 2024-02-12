package com.enrich.authn.pojo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class CustomErrorResponse {
    private boolean success;
    private String systemMessage;
    private String systemMessageType;
    private java.util.List<Object> data;

    // Constructors, getters, and setters
    public CustomErrorResponse() {
        this.success = false;
        this.systemMessage = "401 unauthorized";
        this.systemMessageType = "401 unauthorized";
        this.data = new ArrayList<>();
    }

    public CustomErrorResponse(String systemMessage, String systemMessageType, List<Object> data) {
        this.success = false;
        this.systemMessage = systemMessage;
        this.systemMessageType = systemMessageType;
        this.data = data;
    }
    // Getter and Setter methods for all fields
}
