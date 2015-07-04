/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service;

import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensExceptions;
import com.parallax.server.common.cloudsession.exceptions.UnknownBucketTypeException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;

/**
 *
 * @author Michel
 */
public interface BucketService {

    void consumeTokens(Long idUser, String type, int tokenCount) throws UnknownBucketTypeException, UnknownUserIdException, InsufficientBucketTokensExceptions;
}
