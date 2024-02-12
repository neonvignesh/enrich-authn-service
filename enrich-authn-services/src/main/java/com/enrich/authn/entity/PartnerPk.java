package com.enrich.authn.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class PartnerPk implements Serializable{

    private String partnerCode;
    private String partnerChannel;

}
