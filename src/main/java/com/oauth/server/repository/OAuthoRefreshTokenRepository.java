package com.oauth.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.oauth.server.dto.OAuthRefreshToken;

public interface OAuthoRefreshTokenRepository extends MongoRepository<OAuthRefreshToken, String> {
}
