/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.config;

import com.google.inject.AbstractModule;
import com.parallax.server.common.cloudsession.db.dao.AuthenticationTokenDao;
import com.parallax.server.common.cloudsession.db.dao.BucketDao;
import com.parallax.server.common.cloudsession.db.dao.ConfirmTokenDao;
import com.parallax.server.common.cloudsession.db.dao.ResetTokenDao;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.dao.impl.AuthenticationTokenDaoImpl;
import com.parallax.server.common.cloudsession.db.dao.impl.BucketDaoImpl;
import com.parallax.server.common.cloudsession.db.dao.impl.ConfirmTokenDaoImpl;
import com.parallax.server.common.cloudsession.db.dao.impl.ResetTokenDaoImpl;
import com.parallax.server.common.cloudsession.db.dao.impl.UserDaoImpl;

/**
 *
 * @author Michel
 */
public class DaoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AuthenticationTokenDao.class).to(AuthenticationTokenDaoImpl.class);
        bind(ResetTokenDao.class).to(ResetTokenDaoImpl.class);
        bind(ConfirmTokenDao.class).to(ConfirmTokenDaoImpl.class);
        bind(UserDao.class).to(UserDaoImpl.class);
        bind(BucketDao.class).to(BucketDaoImpl.class);
    }

}
