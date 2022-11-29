package com.oauth.server.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.oauth.server.dto.OAuthPartnerToken;

public interface OAuthPartnerTokenRepository extends MongoRepository<OAuthPartnerToken, String> {
	List<OAuthPartnerToken> findByAuthenticationId(String authenticationId);
}
