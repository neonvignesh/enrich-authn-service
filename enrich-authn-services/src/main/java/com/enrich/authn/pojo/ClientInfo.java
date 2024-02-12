package com.enrich.authn.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ClientInfo {
    @JsonProperty("pan_number")
    private String panNumber;

    // Other properties and getters/setters

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }
}
