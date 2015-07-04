/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service;

import com.parallax.server.common.cloudsession.db.generated.tables.records.ConfirmtokenRecord;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensException;

/**
 *
 * @author Michel
 */
public interface ConfirmTokenService {

    ConfirmtokenRecord getConfirmToken(String token);

    boolean isValidConfirmToken(String token);

    ConfirmtokenRecord createConfirmToken(String server, Long idUser) throws InsufficientBucketTokensException;
}
