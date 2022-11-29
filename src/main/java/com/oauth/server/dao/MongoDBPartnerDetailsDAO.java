/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.oauth.server.dto.OAuthPartner;
import com.oauth.server.repository.OAuthPartnerRepository;

import lombok.NonNull;

/**
 * A DAO to access {@link OAuthPartner} in DynamoDB.
 *
 * @author Lucun Cai
 */
@Service
@Primary
public class MongoDBPartnerDetailsDAO {
	
	@Autowired
	private OAuthPartnerRepository oauthPartnerRepository;

    /**
     * Returns an OAuthPartner object whose keys match those of the prototype key object given, or null if no such item exists.
     *
     * @param partnerId partnerId.
     * @return {@link OAuthPartner} or null if not found.
     */
    public OAuthPartner loadPartnerByPartnerId(@NonNull String partnerId) {
        return oauthPartnerRepository.findById(partnerId).get();
    }

    /**
     * Scans through an Amazon DynamoDB table and returns the matching results as an unmodifiable list of instantiated objects.
     *
     * @return a list of {@link OAuthPartner}.
     */
    public List<OAuthPartner> listPartners() {
        return oauthPartnerRepository.findAll();
    }

    /**
     * Save the {@link OAuthPartner} provided.
     *
     * @param partner {@link OAuthPartner}
     */
    public void savePartner(OAuthPartner partner) {
        oauthPartnerRepository.save(partner);
    }

    /**
     * Delete the {@link OAuthPartner} by partnerId.
     *
     * @param partnerId
     */
    public void deletePartnerByPartnerId(@NonNull String partnerId) {
    	oauthPartnerRepository.deleteById(partnerId);
    }
}
