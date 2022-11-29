package com.oauth.server.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.oauth.server.dto.OAuthClientDetails;
import com.oauth.server.repository.OAuthClientDetailsRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Primary
public class MongoDBClientDetailsDAO  implements ClientDetailsService, ClientRegistrationService {
	
	@Autowired
	private OAuthClientDetailsRepository oauthClientDetailsRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Load {@link ClientDetails} by clientId provided.
     *
     * @param clientId client id.
     * @return client details.
     * @throws NoSuchClientException if clientId not found.
     */
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws NoSuchClientException {
        return Optional.ofNullable(oauthClientDetailsRepository.findById(clientId).get())
            .map(OAuthClientDetails::toClientDetails)
            .orElseThrow(() -> new NoSuchClientException("Client: " + clientId + " not found."));
    }

    /**
     * Add a new {@link ClientDetails} into Database.
     *
     * @param clientDetails client details to be added.
     * @throws ClientAlreadyExistsException if client details already exists.
     */
    @Override
    public void addClientDetails(ClientDetails clientDetails) throws ClientAlreadyExistsException {

        OAuthClientDetails oAuthClientDetails = oauthClientDetailsRepository.findById(clientDetails.getClientId()).get();

        if (oAuthClientDetails != null) {
            throw new ClientAlreadyExistsException("client already exists: " + clientDetails.getClientId());
        }

        addOrUpdateClientDetails(clientDetails);
    }

    /**
     * Update an existing {@link ClientDetails} in database.
     *
     * @param clientDetails client details.
     * @throws NoSuchClientException if client not exit.
     */
    @Override
    public void updateClientDetails(@NonNull ClientDetails clientDetails) throws NoSuchClientException {
        OAuthClientDetails oAuthClientDetails = oauthClientDetailsRepository.findById(clientDetails.getClientId()).get();

        if (oAuthClientDetails == null) {
            throw new NoSuchClientException("client not exists: " + clientDetails.getClientId());
        }

        addOrUpdateClientDetails(clientDetails);
    }

    /**
     * Add or update a client details in database.
     *
     * @param clientDetails client details.
     */
    public void addOrUpdateClientDetails(@NonNull ClientDetails clientDetails) {
        List<String> autoApproveList = clientDetails.getScope().stream()
            .filter(scope -> clientDetails.isAutoApprove(scope))
            .collect(Collectors.toList());

        OAuthClientDetails oAuthClientDetails = OAuthClientDetails
            .builder()
            .clientId(clientDetails.getClientId())
            .authorities(StringUtils.collectionToCommaDelimitedString(clientDetails.getAuthorities()))
            .authorizedGrantTypes(
                StringUtils.collectionToCommaDelimitedString(clientDetails.getAuthorizedGrantTypes()))
            .scopes(StringUtils.collectionToCommaDelimitedString(clientDetails.getScope()))
            .webServerRedirectUri(
                StringUtils.collectionToCommaDelimitedString(clientDetails.getRegisteredRedirectUri()))
            .accessTokenValidity(clientDetails.getAccessTokenValiditySeconds())
            .refreshTokenValidity(clientDetails.getRefreshTokenValiditySeconds())
            .autoapprove(StringUtils.collectionToCommaDelimitedString(autoApproveList))
            .build();

        oauthClientDetailsRepository.save(oAuthClientDetails);
    }

    /**
     * Update the client secret for a specific client id.
     *
     * @param clientId client id.
     * @param secret client secret.
     * @throws NoSuchClientException if client not exist.
     */
    @Override
    public void updateClientSecret(@NonNull String clientId, @NonNull String secret) throws NoSuchClientException {
        OAuthClientDetails oAuthClientDetails = oauthClientDetailsRepository.findById(clientId).get();

        if (oAuthClientDetails == null) {
            throw new NoSuchClientException("client not exists: " + clientId);
        }

        OAuthClientDetails updatedItem = oAuthClientDetails.toBuilder().clientSecret(passwordEncoder.encode(secret))
            .build();
        oauthClientDetailsRepository.save(updatedItem);
    }

    /**
     * Remove a specific client details by clientId.
     *
     * @param clientId client id.
     */
    @Override
    public void removeClientDetails(@NonNull String clientId) {
        OAuthClientDetails oAuthClientDetails = oauthClientDetailsRepository.findById(clientId).get();

        if (oAuthClientDetails == null) {
        	System.out.println("clientId already deleted: " + clientId);
        } else {
        	oauthClientDetailsRepository.deleteById(clientId);
        }
    }

    /**
     * List all the oauth clients in database by scanning the database.
     *
     * @return all client details.
     */
    @Override
    public List<ClientDetails> listClientDetails() {
        return oauthClientDetailsRepository.findAll()
            .stream()
            .map(OAuthClientDetails::toClientDetails)
            .collect(Collectors.toList());
    }
	
	
}
