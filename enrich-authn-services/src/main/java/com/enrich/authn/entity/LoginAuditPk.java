package com.enrich.authn.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Id;

import lombok.Data;

@Data
public class LoginAuditPk implements Serializable  {
	 private String userId;
	 private String sessionId;
	 
}
