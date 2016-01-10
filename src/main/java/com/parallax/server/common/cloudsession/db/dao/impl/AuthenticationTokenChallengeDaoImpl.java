/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao.impl;

import com.codahale.metrics.annotation.Counted;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.AuthenticationTokenChallengeDao;
import com.parallax.server.common.cloudsession.db.generated.Tables;
import com.parallax.server.common.cloudsession.db.generated.tables.records.AuthenticationtokenRecord;
import com.parallax.server.common.cloudsession.db.generated.tables.records.AuthenticationtokenchallengeRecord;
import com.parallax.server.common.cloudsession.service.TokenGeneratorService;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.jooq.DSLContext;

/**
 *
 * @author Michel
 */
@Singleton
public class AuthenticationTokenChallengeDaoImpl implements AuthenticationTokenChallengeDao {

    private DSLContext create;

    private TokenGeneratorService tokenGeneratorService;

    private Configuration configuration;

    @Inject
    public void setDSLContext(DSLContext dsl) {
        this.create = dsl;
    }

    @Inject
    public void setTokenGeneratorService(TokenGeneratorService tokenGeneratorService) {
        this.tokenGeneratorService = tokenGeneratorService;
    }

    @Inject
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Counted(monotonic = true, name = "getAuthenticationTokenChallenge")
    @Override
    public AuthenticationtokenchallengeRecord getAuthenticationTokenChallenge(String challenge) {
        return create.selectFrom(Tables.AUTHENTICATIONTOKENCHALLENGE).where(Tables.AUTHENTICATIONTOKENCHALLENGE.CHALLENGE.equal(challenge)).fetchOne();
    }

    @Counted(monotonic = true, name = "createChallenge")
    @Override
    public AuthenticationtokenchallengeRecord createChallenge(AuthenticationtokenRecord authenticationtoken) {
        String challenge = tokenGeneratorService.generateToken();
        String hash = Hashing.sha256().hashString(authenticationtoken.getToken() + challenge, Charsets.UTF_8).toString();

        int validityMinutes = configuration.getInt("authentication-token-challenge-validity-minutes", 10);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, validityMinutes);
        AuthenticationtokenchallengeRecord authenticationTokenChallenge = create.insertInto(Tables.AUTHENTICATIONTOKENCHALLENGE, Tables.AUTHENTICATIONTOKENCHALLENGE.ID_AUTHENTICATIONTOKEN, Tables.AUTHENTICATIONTOKENCHALLENGE.CHALLENGE, Tables.AUTHENTICATIONTOKENCHALLENGE.HASH, Tables.AUTHENTICATIONTOKENCHALLENGE.VALIDITY).values(authenticationtoken.getId(), challenge, hash, new Timestamp(calendar.getTime().getTime())).returning().fetchOne();
        return authenticationTokenChallenge;
    }

    @Counted(monotonic = true, name = "deleteAuthenticationTokenChallenge")
    @Override
    public int deleteAuthenticationTokenChallenge(String challenge) {
        return create.deleteFrom(Tables.AUTHENTICATIONTOKENCHALLENGE).where(Tables.AUTHENTICATIONTOKENCHALLENGE.CHALLENGE.equal(challenge)).execute();
    }

    @Counted(monotonic = true, name = "deleteAuthenticationTokenChallenge")
    @Override
    public int deleteAuthenticationTokenChallenge(Long id) {
        return create.deleteFrom(Tables.AUTHENTICATIONTOKENCHALLENGE).where(Tables.AUTHENTICATIONTOKENCHALLENGE.ID.equal(id)).execute();
    }

    @Counted(monotonic = true, name = "cleanExpiredChallenges")
    @Override
    public int cleanExpiredChallenges() {
        return create.deleteFrom(Tables.AUTHENTICATIONTOKENCHALLENGE).where(Tables.AUTHENTICATIONTOKENCHALLENGE.VALIDITY.le(new Timestamp(new Date().getTime()))).execute();
    }

    @Counted(monotonic = true, name = "getAuthenticationTokenChallengesForUser")
    @Override
    public List<AuthenticationtokenchallengeRecord> getAuthenticationTokenChallengesForUser(Long idUser) {
        //   create.select(Tables.AUTHENTICATIONTOKENCHALLENGE.ID, Tables.AUTHENTICATIONTOKENCHALLENGE.ID_AUTHENTICATIONTOKEN, Tables.AUTHENTICATIONTOKENCHALLENGE.CHALLENGE, Tables.AUTHENTICATIONTOKENCHALLENGE.HASH, Tables.AUTHENTICATIONTOKENCHALLENGE.VALIDITY).
        return (List<AuthenticationtokenchallengeRecord>) create.selectFrom(Tables.AUTHENTICATIONTOKENCHALLENGE).where(Tables.AUTHENTICATIONTOKENCHALLENGE.ID_AUTHENTICATIONTOKEN.in(create.select(Tables.AUTHENTICATIONTOKEN.ID).where(Tables.AUTHENTICATIONTOKEN.ID_USER.eq(idUser)).fetch(Tables.AUTHENTICATIONTOKEN.ID, Long.class)));
        //    return (List<AuthenticationtokenRecord>) Arrays.asList(create.selectFrom(Tables.AUTHENTICATIONTOKENCHALLENGE)..where(Tables.AUTHENTICATIONTOKEN.ID_USER.equal(idUser)).fetchArray());
    }

}
