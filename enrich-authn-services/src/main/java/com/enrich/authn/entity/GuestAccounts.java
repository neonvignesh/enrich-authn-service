package com.enrich.authn.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Entity
@Table(name = "guest_accounts", schema = "users")
@Data
public class GuestAccounts {

	@Id
	@Column(name = "guest_id")
	@JsonProperty("guest_id")
	private String guestId;

	@Column(name = "customer_context_id", length = 50)
	@JsonProperty("customer_context_id")
	private String customerContextId;

	@Column(name = "context_type", length = 20)
	@JsonProperty("context_type")
	private String contextType;

	@Column(name = "expiration_date")
	@JsonProperty("expiration_date")
	private Timestamp expirationDate;

}
