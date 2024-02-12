package com.enrich.authn.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enrich.authn.entity.LoginAudit;
import com.enrich.authn.entity.UserTokenAction;
import com.enrich.authn.entity.UserTokenActionsPk;

@Repository
public interface UserTokenActionRepository extends JpaRepository<UserTokenAction, UserTokenActionsPk> { 
	
	

}
