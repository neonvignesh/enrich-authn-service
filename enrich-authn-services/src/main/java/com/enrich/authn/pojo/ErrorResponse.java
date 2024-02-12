package com.enrich.authn.pojo;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

public class ErrorResponse {
    private boolean success;
    private String successmessage;
    private String successMessageType;
    private List<Object> data;

    public ErrorResponse() {
        // Default constructor
    }

    public ErrorResponse(boolean success, String successmessage, String successMessageType, List<Object> data) {
        this.success = success;
        this.successmessage = successmessage;
        this.successMessageType = successMessageType;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSuccessmessage() {
        return successmessage;
    }

    public void setSuccessmessage(String successmessage) {
        this.successmessage = successmessage;
    }

    public String getSuccessMessageType() {
        return successMessageType;
    }

    public void setSuccessMessageType(String successMessageType) {
        this.successMessageType = successMessageType;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }
}
