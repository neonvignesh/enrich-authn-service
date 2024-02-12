package com.enrich.authn.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "user_token_actions", schema = "users")
@IdClass(UserTokenActionsPk.class)
public class UserTokenAction implements  Serializable {

	    /**
	 * 
	 */
	private static final long serialVersionUID = 2372819749610320923L;

		@Id
	    @Column(name = "input_token", nullable = false)
	    private String inputToken;

	    @Id
	    @Column(name = "user_id", nullable = false)
	    private String userId;

	    @Id
	    @Column(name = "access_token", nullable = false)
	    private String accessToken;

	    @Column(name = "expiry_date", nullable = false)
	    private Date expiryDate;

}
