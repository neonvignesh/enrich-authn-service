package com.enrich.authn.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "user_credentials", schema = "users")
public class UserCredentials {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "pan_no")
    private String panNo;

    // Constructors, getters, setters, and other methods...

}

