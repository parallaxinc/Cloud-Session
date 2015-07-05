/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao.impl;

import com.codahale.metrics.annotation.Counted;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.ResetTokenDao;
import com.parallax.server.common.cloudsession.db.generated.Tables;
import com.parallax.server.common.cloudsession.db.generated.tables.records.ResettokenRecord;
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
public class ResetTokenDaoImpl implements ResetTokenDao {

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
    public ResettokenRecord getResetToken(String token) {
        return create.selectFrom(Tables.RESETTOKEN).where(Tables.RESETTOKEN.TOKEN.equal(token)).fetchOne();
    }

    @Counted(monotonic = true, name = "createResetToken")
    @Override
    public ResettokenRecord createResetToken(Long idUser) {
        String token = tokenGeneratorService.generateToken();
        int validityHours = configuration.getInt("reset-token-validity-hours", 12);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, validityHours);
        ResettokenRecord resetToken = create.insertInto(Tables.RESETTOKEN, Tables.RESETTOKEN.ID_USER, Tables.RESETTOKEN.TOKEN, Tables.RESETTOKEN.VALIDITY).values(idUser, token, new Timestamp(calendar.getTime().getTime())).returning().fetchOne();
        return resetToken;
    }

    @Counted(monotonic = true, name = "deleteConfirmTokenForToken")
    @Override
    public int deleteResetToken(String token) {
        return create.deleteFrom(Tables.RESETTOKEN).where(Tables.RESETTOKEN.TOKEN.equal(token)).execute();
    }

    @Counted(monotonic = true, name = "deleteConfirmTokenForId")
    @Override
    public int deleteResetToken(Long id) {
        return create.deleteFrom(Tables.RESETTOKEN).where(Tables.RESETTOKEN.ID.equal(id)).execute();
    }

    @Counted(monotonic = true, name = "deleteConfirmTokenForUser")
    @Override
    public int deleteResetTokenForUser(Long idUser) {
        return create.deleteFrom(Tables.RESETTOKEN).where(Tables.RESETTOKEN.ID_USER.equal(idUser)).execute();
    }

    @Counted(monotonic = true, name = "cleanExpiredTokens")
    @Override
    public int cleanExpiredTokens() {
        return create.deleteFrom(Tables.RESETTOKEN).where(Tables.RESETTOKEN.VALIDITY.le(new Timestamp(new Date().getTime()))).execute();
    }

}
