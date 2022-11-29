package com.oauth.server.dao;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import com.oauth.server.dto.OAuthAccessToken;
import com.oauth.server.dto.OAuthRefreshToken;
import com.oauth.server.repository.OAuthAccessTokenRepository;
import com.oauth.server.repository.OAuthoRefreshTokenRepository;

@Service
@Primary
public class MongoDBTokenDAO implements TokenStore {

    private final AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    @Autowired
    private OAuthAccessTokenRepository oauthAccessTokenRepository;
    @Autowired
    private OAuthoRefreshTokenRepository oauthoRefreshTokenRepository;

    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        String authenticationId = authenticationKeyGenerator.extractKey(authentication);
        List<OAuthAccessToken> accessTokens = oauthAccessTokenRepository.findByAuthenticationId(authenticationId);
        return accessTokens.stream().findAny().map(OAuthAccessToken::getToken).orElse(null);
    }

    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        String refreshToken = null;
        if (token.getRefreshToken() != null) {
            refreshToken = token.getRefreshToken().getValue();
        }

        OAuthAccessToken accessToken = OAuthAccessToken.builder()
            .tokenId(extractTokenKey(token.getValue()))
            .token(token)
            .authenticationId(authenticationKeyGenerator.extractKey(authentication))
            .authentication(authentication)
            .clientId(authentication.getOAuth2Request().getClientId())
            .refreshToken(extractTokenKey(refreshToken))
            .userName(StringUtils.isNotBlank(authentication.getName()) ? authentication.getName() : "#")
            .build();

        oauthAccessTokenRepository.save(accessToken);
    }

    public OAuth2AccessToken readAccessToken(String tokenValue) {
        String tokenId = extractTokenKey(tokenValue);
        return Optional.ofNullable(oauthAccessTokenRepository.findById(tokenId).get())
            .map(OAuthAccessToken::getToken)
            .orElse(null);
    }

    public void removeAccessToken(OAuth2AccessToken token) {
        removeAccessToken(token.getValue());
    }

    public void removeAccessToken(String tokenValue) {
        String tokenId = extractTokenKey(tokenValue);
        oauthAccessTokenRepository.deleteById(tokenId);
    }

    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    public OAuth2Authentication readAuthentication(String token) {
        String tokenId = extractTokenKey(token);
        return Optional.ofNullable(oauthAccessTokenRepository.findById(tokenId).get())
            .map(OAuthAccessToken::getAuthentication)
            .orElse(null);
    }

    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {

        OAuthRefreshToken itemToSave = OAuthRefreshToken.builder()
            .tokenId(extractTokenKey(refreshToken.getValue()))
            .token(refreshToken)
            .authentication(authentication)
            .build();

        oauthoRefreshTokenRepository.save(itemToSave);
    }

    public OAuth2RefreshToken readRefreshToken(String token) {
        String tokenId = extractTokenKey(token);

        return Optional.ofNullable(oauthoRefreshTokenRepository.findById(tokenId).get())
            .map(OAuthRefreshToken::getToken)
            .orElse(null);
    }

    public void removeRefreshToken(OAuth2RefreshToken token) {
        removeRefreshToken(token.getValue());
    }

    public void removeRefreshToken(String token) {
        String tokenId = extractTokenKey(token);
        oauthoRefreshTokenRepository.deleteById(tokenId);
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return readAuthenticationForRefreshToken(token.getValue());
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(String value) {
        String tokenId = extractTokenKey(value);
        return Optional.ofNullable(oauthoRefreshTokenRepository.findById(tokenId).get())
            .map(OAuthRefreshToken::getAuthentication)
            .orElse(null);
    }

    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        removeAccessTokenUsingRefreshToken(refreshToken.getValue());
    }

    public void removeAccessTokenUsingRefreshToken(String refreshToken) {
		/*
		 * String refreshTokenId = extractTokenKey(refreshToken);
		 * DynamoDBQueryExpression query = new
		 * DynamoDBQueryExpression<OAuthAccessToken>()
		 * .withIndexName("refreshToken-index") .withConsistentRead(Boolean.FALSE)
		 * .withHashKeyValues(OAuthAccessToken.builder() .refreshToken(refreshTokenId)
		 * .build());
		 * dynamoDBMapper.batchDelete(accessTokens);
		 */
		 oauthAccessTokenRepository.deleteByRefreshToken(refreshToken);

    }

    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
//        DynamoDBQueryExpression query = new DynamoDBQueryExpression<OAuthAccessToken>()
//            .withIndexName("clientId-userName-index")
//            .withConsistentRead(Boolean.FALSE)
//            .withHashKeyValues(OAuthAccessToken.builder()
//                .clientId(clientId)
//                .build());

        List<OAuthAccessToken> accessTokens = oauthAccessTokenRepository.findByClientId(clientId);
        return accessTokens.stream().map(OAuthAccessToken::getToken).collect(Collectors.toList());
    }

    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
//        DynamoDBQueryExpression query = new DynamoDBQueryExpression<OAuthAccessToken>()
//            .withIndexName("clientId-userName-index")
//            .withConsistentRead(Boolean.FALSE)
//            .withHashKeyValues(OAuthAccessToken.builder()
//                .clientId(clientId)
//                .userName(userName)
//                .build());

    	List<OAuthAccessToken> accessTokens = oauthAccessTokenRepository.findByClientIdAndUserName(clientId, userName);
        return accessTokens.stream().map(OAuthAccessToken::getToken).collect(Collectors.toList());
    }

    protected String extractTokenKey(String value) {
        if (value == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }

        try {
            byte[] bytes = digest.digest(value.getBytes("UTF-8"));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
        }
    }

}
