/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao.impl;

import com.codahale.metrics.annotation.Counted;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.Tables;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.exceptions.NonUniqueEmailException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

/**
 *
 * @author Michel
 */
@Singleton
public class UserDaoImpl implements UserDao {

    private static final String LOCAL_USER = "local";

    private DSLContext create;

    @Inject
    public void setDSLContext(DSLContext dsl) {
        this.create = dsl;
    }

    @Counted(monotonic = true, name = "getUser")
    @Override
    public UserRecord getUser(Long id) throws UnknownUserIdException {
        UserRecord userRecord = create.selectFrom(Tables.USER).where(Tables.USER.ID.equal(id)).fetchOne();
        if (userRecord == null) {
            throw new UnknownUserIdException(id);
        }
        return userRecord;
    }

    @Counted(monotonic = true, name = "getLocalUserByEmail")
    @Override
    public UserRecord getLocalUserByEmail(String email) throws UnknownUserException {
        UserRecord userRecord = create.selectFrom(Tables.USER).where(Tables.USER.EMAIL.equal(email)).and(Tables.USER.AUTHSOURCE.equal(LOCAL_USER)).fetchOne();
        if (userRecord == null) {
            throw new UnknownUserException(email);
        }
        return userRecord;
    }

    @Counted(monotonic = true, name = "createLocalUser")
    @Override
    public UserRecord createLocalUser(String email, String password, String salt, String locale, String screenname) throws NonUniqueEmailException {
        try {
            return create.insertInto(Tables.USER).columns(Tables.USER.EMAIL, Tables.USER.PASSWORD, Tables.USER.SALT, Tables.USER.AUTHSOURCE, Tables.USER.LOCALE, Tables.USER.SCREENNAME)
                    .values(email, password, salt, LOCAL_USER, locale, screenname).returning().fetchOne();
        } catch (DataAccessException dae) {
            throw new NonUniqueEmailException(email);
        }
    }

}
