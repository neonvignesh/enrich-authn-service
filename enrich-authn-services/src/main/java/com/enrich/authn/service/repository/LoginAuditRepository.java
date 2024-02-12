package com.enrich.authn.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enrich.authn.entity.LoginAuditPk;
import com.enrich.authn.entity.LoginAudit;


@Repository
public interface LoginAuditRepository extends JpaRepository<LoginAudit, LoginAuditPk> {
	
}
