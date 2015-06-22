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
import org.jooq.DSLContext;

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

}
