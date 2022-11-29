/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.ClientKeyGenerator;
import org.springframework.security.oauth2.client.token.ClientTokenServices;
import org.springframework.security.oauth2.client.token.DefaultClientKeyGenerator;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import com.oauth.server.dto.OAuthPartnerToken;
import com.oauth.server.repository.OAuthPartnerTokenRepository;

/**
 * A DAO to access {@link OAuthPartnerToken} in DynamoDB.
 *
 * @author Lucun Cai
 */
@Service
@Primary
public class MongoDBPartnerTokenDAO implements ClientTokenServices {
	
	@Autowired
	private OAuthPartnerTokenRepository oauthPartnerTokenRepository;

    private ClientKeyGenerator keyGenerator = new DefaultClientKeyGenerator();

    /**
     * Get the {@link OAuth2AccessToken} of a protected resource for the {@link Authentication} provided.
     *
     * @param resource partner protected resource.
     * @param authentication user authentication.
     * @return oauth access token.
     */
    @Override
    public OAuth2AccessToken getAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String authenticationId = keyGenerator.extractKey(resource, authentication);
        List<OAuthPartnerToken> accessTokens = getOAuthPartnerTokensByAuthenticationId(authenticationId);

        return accessTokens.stream().findAny().map(OAuthPartnerToken::getToken).orElse(null);
    }

    /**
     * Save the {@link OAuth2AccessToken} of a partner protected resource for the {@link Authentication} provided.
     *
     * @param resource partner protected resource.
     * @param authentication user authentication.
     * @param accessToken oauth access token.
     */
    @Override
    public void saveAccessToken(OAuth2ProtectedResourceDetails resource,
                                Authentication authentication,
                                OAuth2AccessToken accessToken) {

        String userName = authentication != null ? authentication.getName() : null;

        OAuthPartnerToken oauthPartnerToken = OAuthPartnerToken.builder()
            .tokenId(accessToken.getValue())
            .token(accessToken)
            .authenticationId(keyGenerator.extractKey(resource, authentication))
            .userName(userName)
            .clientId(resource.getClientId())
            .build();

        oauthPartnerTokenRepository.save(oauthPartnerToken);
    }

    /**
     * Remove the all the access token of the partner protected resource for the {@link Authentication} provided.
     *
     * @param resource partner protected resource.
     * @param authentication user authentication.
     */
    @Override
    public void removeAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String authenticationId = keyGenerator.extractKey(resource, authentication);
        List<OAuthPartnerToken> partnerTokens = getOAuthPartnerTokensByAuthenticationId(authenticationId);
        oauthPartnerTokenRepository.deleteAll(partnerTokens);
    }

    private List<OAuthPartnerToken> getOAuthPartnerTokensByAuthenticationId(String authenticationId) {
        return oauthPartnerTokenRepository.findByAuthenticationId(authenticationId);
    }

}
