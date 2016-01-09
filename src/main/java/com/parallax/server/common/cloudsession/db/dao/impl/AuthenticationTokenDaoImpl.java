/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao.impl;

import com.codahale.metrics.annotation.Counted;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.AuthenticationTokenDao;
import com.parallax.server.common.cloudsession.db.generated.Tables;
import com.parallax.server.common.cloudsession.db.generated.tables.records.AuthenticationtokenRecord;
import com.parallax.server.common.cloudsession.service.TokenGeneratorService;
import java.sql.Timestamp;
import java.util.Arrays;
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
public class AuthenticationTokenDaoImpl implements AuthenticationTokenDao {

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

    @Counted(monotonic = true, name = "getAuthenticationToken")
    @Override
    public AuthenticationtokenRecord getAuthenticationToken(String token) {
        return create.selectFrom(Tables.AUTHENTICATIONTOKEN).where(Tables.AUTHENTICATIONTOKEN.TOKEN.equal(token)).fetchOne();
    }

    @Counted(monotonic = true, name = "createAuthenticationToken")
    @Override
    public AuthenticationtokenRecord createAuthenticationToken(Long idUser, String server, String browser, String ipAddress) {
        String token = tokenGeneratorService.generateToken();
        int validityHours = configuration.getInt("authentication-token-validity-minutes", 12);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, validityHours);
        AuthenticationtokenRecord authenticationtoken = create.insertInto(Tables.AUTHENTICATIONTOKEN, Tables.AUTHENTICATIONTOKEN.ID_USER, Tables.AUTHENTICATIONTOKEN.SERVER, Tables.AUTHENTICATIONTOKEN.TOKEN, Tables.AUTHENTICATIONTOKEN.VALIDITY, Tables.AUTHENTICATIONTOKEN.BROWSER, Tables.AUTHENTICATIONTOKEN.IPADDRESS).values(idUser, server, token, new Timestamp(calendar.getTime().getTime()), browser, ipAddress).returning().fetchOne();
        return authenticationtoken;
    }

    @Counted(monotonic = true, name = "deleteAuthenticationToken")
    @Override
    public int deleteAuthenticationToken(String token) {
        return create.deleteFrom(Tables.AUTHENTICATIONTOKEN).where(Tables.AUTHENTICATIONTOKEN.TOKEN.equal(token)).execute();
    }

    @Counted(monotonic = true, name = "deleteAuthenticationToken")
    @Override
    public int deleteAuthenticationToken(Long id) {
        return create.deleteFrom(Tables.AUTHENTICATIONTOKEN).where(Tables.AUTHENTICATIONTOKEN.ID.equal(id)).execute();
    }

    @Counted(monotonic = true, name = "cleanExpiredTokens")
    @Override
    public int cleanExpiredTokens() {
        return create.deleteFrom(Tables.AUTHENTICATIONTOKEN).where(Tables.AUTHENTICATIONTOKEN.VALIDITY.le(new Timestamp(new Date().getTime()))).execute();
    }

    @Counted(monotonic = true, name = "getAuthenticationTokenForUser")
    @Override
    public List<AuthenticationtokenRecord> getAuthenticationTokenForUser(Long idUser) {
        return (List<AuthenticationtokenRecord>) Arrays.asList(create.selectFrom(Tables.AUTHENTICATIONTOKEN).where(Tables.AUTHENTICATIONTOKEN.ID_USER.equal(idUser)).fetchArray());
    }

}
