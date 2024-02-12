package com.enrich.authn.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Entity
@Table(name = "guest_activity", schema = "users")
@Data
public class GuestActivity {

	@Id
	@Column(name = "guest_id")
	@JsonProperty("guest_id")
	private String guestId;

	@Column(name = "customer_context_id", length = 50)
	@JsonProperty("customer_context_id")
	private String customerContextId;

	@Column(name = "otp_id", length = 60)
	@JsonProperty("otp_id")
	private String otpId;

	@Column(name = "otp", length = 6)
	@JsonProperty("otp")
	private String otp;
	
	@Column(name = "context_type", length = 20)
	@JsonProperty("context_type")
	private String contextType;

	@Column(name = "status", length = 20)
	@JsonProperty("status")
	private String status;

	@Column(name = "expiry_date")
	@JsonProperty("expiry_date")
	private Timestamp expiryDate;

}
