package com.enrich.authn.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import com.enrich.authn.entity.Partner;
import com.enrich.authn.entity.PartnerPk;

@Repository

public interface PartnerRepository extends JpaRepository<Partner, PartnerPk> {
   Optional< Partner> findByPartnerCodeAndPartnerChannel(String partnerCode, String partnerChannel);
}