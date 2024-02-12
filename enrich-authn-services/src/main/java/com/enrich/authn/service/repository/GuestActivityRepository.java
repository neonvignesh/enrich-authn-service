package com.enrich.authn.service.repository;

import java.sql.Timestamp;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.enrich.authn.entity.GuestActivity;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface GuestActivityRepository extends JpaRepository<GuestActivity, String> {

	@Transactional
	@Modifying
	@Query("UPDATE GuestActivity ga SET ga.otpId = :otpId, ga.otp = :otp, ga.status = :status, ga.expiryDate = :expiryDate WHERE ga.guestId = :guestId AND ga.customerContextId = :customerContextId")
	int updateGuestActivity(@Param("guestId") String guestId, @Param("customerContextId") String customerContextId,
			@Param("otp") String otp, @Param("otpId") String otpId, @Param("status") String status,
			@Param("expiryDate") Timestamp expiryDate);

	GuestActivity findByGuestId(String guestId);

}
