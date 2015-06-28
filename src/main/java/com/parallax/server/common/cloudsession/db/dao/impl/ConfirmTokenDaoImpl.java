/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.ConfirmTokenDao;
import com.parallax.server.common.cloudsession.db.generated.Tables;
import com.parallax.server.common.cloudsession.db.generated.tables.records.ConfirmtokenRecord;
import com.parallax.server.common.cloudsession.service.TokenGeneratorService;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.configuration.Configuration;
import org.jooq.DSLContext;

/**
 *
 * @author Michel
 */
@Singleton
public class ConfirmTokenDaoImpl implements ConfirmTokenDao {

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

    @Override
    public ConfirmtokenRecord getConfirmToken(String token) {
        return create.selectFrom(Tables.CONFIRMTOKEN).where(Tables.CONFIRMTOKEN.TOKEN.equal(token)).fetchOne();
    }

    @Override
    public ConfirmtokenRecord createConfirmToken(Long idUser) {
        String token = tokenGeneratorService.generateToken();
        int validityHours = configuration.getInt("confirm-token-validity-hours", 12);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, validityHours);
        ConfirmtokenRecord confirmToken = create.insertInto(Tables.CONFIRMTOKEN, Tables.CONFIRMTOKEN.ID_USER, Tables.CONFIRMTOKEN.TOKEN, Tables.CONFIRMTOKEN.VALIDITY).values(idUser, token, new Timestamp(calendar.getTime().getTime())).returning().fetchOne();
        return confirmToken;
    }

    @Override
    public int deleteConfirmToken(String token) {
        return create.deleteFrom(Tables.CONFIRMTOKEN).where(Tables.CONFIRMTOKEN.TOKEN.equal(token)).execute();
    }

    @Override
    public int deleteConfirmToken(Long id) {
        return create.deleteFrom(Tables.CONFIRMTOKEN).where(Tables.CONFIRMTOKEN.ID.equal(id)).execute();
    }

    @Override
    public int deleteConfirmTokenForUser(Long idUser) {
        return create.deleteFrom(Tables.CONFIRMTOKEN).where(Tables.CONFIRMTOKEN.ID_USER.equal(idUser)).execute();
    }

    @Override
    public int cleanExpiredTokens() {
        return create.deleteFrom(Tables.CONFIRMTOKEN).where(Tables.CONFIRMTOKEN.VALIDITY.le(new Timestamp(new Date().getTime()))).execute();
    }

}
