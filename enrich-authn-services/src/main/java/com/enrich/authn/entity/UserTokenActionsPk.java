package com.enrich.authn.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserTokenActionsPk implements Serializable  {

	 private String inputToken;
	 private String userId;
	 private String accessToken;
}
