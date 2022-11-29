package com.oauth.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.oauth.server.dto.OAuthPartner;

public interface OAuthPartnerRepository extends MongoRepository<OAuthPartner, String> {
}
