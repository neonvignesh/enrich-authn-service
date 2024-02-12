package com.enrich.authn.pojo;


import lombok.Data;

@Data
public class LoginAuthnResponse {
    private boolean success;
    private String systemMessage;
    private String systemMessageType;
    private ResponseData data;

    @Data
    public static class ResponseData {
        private String message;
        private String x_authorization;
        private String user_id;
    }
}