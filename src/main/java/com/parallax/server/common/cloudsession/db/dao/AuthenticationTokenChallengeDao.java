/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao;

import com.parallax.server.common.cloudsession.db.generated.tables.records.AuthenticationtokenRecord;
import com.parallax.server.common.cloudsession.db.generated.tables.records.AuthenticationtokenchallengeRecord;
import java.util.List;

/**
 *
 * @author Michel
 */
public interface AuthenticationTokenChallengeDao {

    AuthenticationtokenchallengeRecord getAuthenticationTokenChallenge(String challenge);

    AuthenticationtokenchallengeRecord createChallenge(AuthenticationtokenRecord authenticationtoken);

    int deleteAuthenticationTokenChallenge(String challenge);

    int deleteAuthenticationTokenChallenge(Long id);

    //int deleteAuthenticationTokenForUser(Long idUser);
    int cleanExpiredChallenges();

    List<AuthenticationtokenchallengeRecord> getAuthenticationTokenChallengesForUser(Long idUser);

}
