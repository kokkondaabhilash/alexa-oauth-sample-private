package com.oauth.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.oauth.server.dto.OAuthCode;

public interface OAuthCodeRepository extends MongoRepository<OAuthCode, String> {
}
