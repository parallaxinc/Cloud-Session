/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.config;

import com.google.inject.AbstractModule;
import com.parallax.server.common.cloudsession.db.dao.ResetTokenDao;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.dao.impl.ResetTokenDaoImpl;
import com.parallax.server.common.cloudsession.db.dao.impl.UserDaoImpl;

/**
 *
 * @author Michel
 */
public class DaoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ResetTokenDao.class).to(ResetTokenDaoImpl.class);
        bind(UserDao.class).to(UserDaoImpl.class);
    }

}
