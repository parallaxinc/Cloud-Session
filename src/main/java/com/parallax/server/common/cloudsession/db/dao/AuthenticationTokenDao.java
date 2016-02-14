/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao;

import com.parallax.server.common.cloudsession.db.generated.tables.records.AuthenticationtokenRecord;
import java.util.List;

/**
 *
 * @author Michel
 */
public interface AuthenticationTokenDao {

    AuthenticationtokenRecord getAuthenticationToken(String token);

    AuthenticationtokenRecord createAuthenticationToken(Long idUser, String server, String browser, String ipAddress);

    int deleteAuthenticationToken(String token);

    int deleteAuthenticationToken(Long id);

    //int deleteAuthenticationTokenForUser(Long idUser);
    int cleanExpiredTokens();

    List<AuthenticationtokenRecord> getAuthenticationTokenForUser(Long idUser);

    public List<AuthenticationtokenRecord> getValidAuthenticationTokens(String server, Long idUser, String browser, String ipAddress);

}
