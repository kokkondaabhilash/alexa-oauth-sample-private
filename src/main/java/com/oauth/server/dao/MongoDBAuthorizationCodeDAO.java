package com.oauth.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.stereotype.Service;

import com.oauth.server.dto.OAuthCode;
import com.oauth.server.repository.OAuthCodeRepository;

@Service
@Primary
public class MongoDBAuthorizationCodeDAO extends RandomValueAuthorizationCodeServices {
	
	@Autowired
	private OAuthCodeRepository oauthCodeRepository;

    /**
     * Store the authorization code for a authenticated user.
     *
     * @param code authorization code.
     * @param authentication authentication for the user.
     */
    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        OAuthCode oAuthCode = new OAuthCode(code, authentication);
        oauthCodeRepository.save(oAuthCode);
    }

    /**
     * Remove/Invalidate the authorization code.
     *
     * @param code authorization code.
     * @return user authentication.
     */
    @Override
    public OAuth2Authentication remove(String code) {
    	OAuthCode oauthCode = oauthCodeRepository.findById(code).orElse(null);
    	if (null != oauthCode) {
    		oauthCodeRepository.deleteById(code);
    		return oauthCode.getAuthentication();
    	}
        return null;
    }
}
