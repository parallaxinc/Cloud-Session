/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service;

import com.parallax.server.common.cloudsession.db.generated.tables.records.AuthenticationtokenRecord;
import com.parallax.server.common.cloudsession.exceptions.EmailNotConfirmedException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.exceptions.UserBlockedException;
import java.util.List;

/**
 *
 * @author Michel
 */
public interface AuthenticationTokenService {

    AuthenticationtokenRecord getAuthenticationToken(String token);

    boolean isValidAuthenticationToken(String token, String server, Long idUser, String browser, String ipAddress);

    AuthenticationtokenRecord createAuthenticationToken(String server, Long idUser, String browser, String ipAddress) throws UnknownUserIdException, UserBlockedException, EmailNotConfirmedException;

    public List<AuthenticationtokenRecord> getValidAuthenticationTokens(String server, Long idUser, String browser, String ipAddress);

    int cleanExpiredAutheticationTokens();
}
