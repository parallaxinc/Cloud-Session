/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.Tables;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.exceptions.NonUniqueEmailException;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

/**
 *
 * @author Michel
 */
@Singleton
public class UserDaoImpl implements UserDao {

    private DSLContext create;

    @Inject
    public void setDSLContext(DSLContext dsl) {
        this.create = dsl;
    }

    @Override
    public UserRecord getUser(Long id) {
        return create.selectFrom(Tables.USER).where(Tables.USER.ID.equal(id)).fetchOne();
    }

    @Override
    public UserRecord getUserByEmail(String email) {
        return create.selectFrom(Tables.USER).where(Tables.USER.EMAIL.equal(email)).fetchOne();
    }

    @Override
    public UserRecord createUser(String email, String password, String salt) throws NonUniqueEmailException {
        try {
            return create.insertInto(Tables.USER).columns(Tables.USER.EMAIL, Tables.USER.PASSWORD, Tables.USER.SALT).values(email, password, salt).returning().fetchOne();
        } catch (DataAccessException dae) {
            throw new NonUniqueEmailException(email);
        }
    }

}
