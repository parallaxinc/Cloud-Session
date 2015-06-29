/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service;

import com.parallax.server.common.cloudsession.db.generated.tables.records.ConfirmtokenRecord;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;

/**
 *
 * @author Michel
 */
public interface ConfirmTokenService {

    ConfirmtokenRecord getConfirmToken(String token);

    ConfirmtokenRecord getConfirmTokenForUser(String email) throws UnknownUserException;

    ConfirmtokenRecord getConfirmTokenForUser(Long idUser) throws UnknownUserIdException;

    boolean isValidConfirmToken(String token);

    ConfirmtokenRecord createConfirmToken(String server, Long idUser);
}
