package com.enrich.authn.entity;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "partners",schema = "users")
@IdClass(PartnerPk.class)
@Data
public class Partner {
	    @Id
	    @Column(name = "partner_code")
	    private String partnerCode;

		@Id
	    @Column(name = "partner_channel")
	    private String partnerChannel;

	    @Column(name = "partner_uuid")
	    private java.util.UUID partnerUuid;

	    @Column(name = "partner_name")
	    private String partnerName;

	    @Column(name = "partner_description")
	    private String partnerDescription;

	    @Column(name = "address_line_1")
	    private String addressLine1;

	    @Column(name = "address_line_2")
	    private String addressLine2;

	    @Column(name = "address_line_3")
	    private String addressLine3;

	    @Column(name = "city")
	    private String city;

	    @Column(name = "state")
	    private String state;

	    @Column(name = "country")
	    private String country;

	    @Column(name = "pin_code")
	    private String pinCode;

}