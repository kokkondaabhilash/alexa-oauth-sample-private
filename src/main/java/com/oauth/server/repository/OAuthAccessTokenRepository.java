package com.oauth.server.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.oauth.server.dto.OAuthAccessToken;

public interface OAuthAccessTokenRepository extends MongoRepository<OAuthAccessToken, String> {
	List<OAuthAccessToken> findByAuthenticationId(String authenticationId);
	List<OAuthAccessToken> findByUserName(String username);
	void deleteByRefreshToken(String refreshToken);
	List<OAuthAccessToken> findByClientId(String clientId);
	List<OAuthAccessToken> findByClientIdAndUserName(String clientId, String userName);
}
