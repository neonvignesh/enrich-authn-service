package com.enrich.authn.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enrich.authn.entity.GuestAccounts;

@Repository
public interface GuestAccountRepository extends JpaRepository<GuestAccounts, String> {
	
	GuestAccounts findByCustomerContextId(String mobileNumber);

}
