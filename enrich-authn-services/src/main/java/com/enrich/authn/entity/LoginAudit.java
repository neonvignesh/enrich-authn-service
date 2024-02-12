package com.enrich.authn.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_audit",schema = "users")
@Data
@IdClass(LoginAuditPk.class)
public class LoginAudit  {
	@Id
    @Column(name = "user_id", length = 13)
    private String userId;

    @Id
    @Column(name = "session_id", length = 40)
    private String sessionId;

    @Column(name = "partner_code", length = 15)
    private String partnerCode;

    @Column(name = "partner_channel", length = 15)
    private String partnerChannel;

    @Column(name = "user_access_type", length = 10)
    private String userAccessType;

    @Column(name = "ip_address", length = 32)
    private String ipAddress;

    @Column(name = "in_out", length = 6)
    private String inOut;

    @Column(name = "in_out_time", nullable = false)
    private LocalDateTime inOutTime;

    @Column(name = "mac_address", length = 32)
    private String macAddress;

    @Column(name = "remarks", length = 50)
    private String remarks;



}
