package com.oauth.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.oauth.server.dto.OAuthClientDetails;

public interface OAuthClientDetailsRepository extends MongoRepository<OAuthClientDetails, String> {
}
