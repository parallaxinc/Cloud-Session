/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.parallax.server.common.cloudsession.db.dao.AuthenticationTokenDao;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.AuthenticationtokenRecord;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.exceptions.EmailNotConfirmedException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.exceptions.UserBlockedException;
import com.parallax.server.common.cloudsession.service.AuthenticationTokenService;
import java.util.Date;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
@Singleton
@Transactional
public class AuthenticationTokenServiceImpl implements AuthenticationTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationTokenServiceImpl.class);

    private AuthenticationTokenDao authenticationTokenDao;

    private UserDao userDao;

    private Configuration configuration;

    @Inject
    public void setAuthenticationTokenDao(AuthenticationTokenDao authenticationTokenDao) {
        this.authenticationTokenDao = authenticationTokenDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Inject
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public AuthenticationtokenRecord getAuthenticationToken(String token) {
        return authenticationTokenDao.getAuthenticationToken(token);
    }

    @Override
    public boolean isValidConfirmToken(String token, String server, String browser, String ipAddress) {
        AuthenticationtokenRecord authenticationToken = authenticationTokenDao.getAuthenticationToken(token);
        if (authenticationToken == null) {
            return false;
        }
        if (!authenticationToken.getServer().equals(server)) {
            return false;
        }
        if (!authenticationToken.getBrowser().equals(browser)) {
            return false;
        }
        if (!authenticationToken.getIpaddress().equals(ipAddress)) {
            return false;
        }
        return authenticationToken.getValidity().after(new Date());
    }

    @Override
    public AuthenticationtokenRecord createAuthenticationToken(String server, Long idUser, String browser, String ipAddress) throws UnknownUserIdException, UserBlockedException, EmailNotConfirmedException {
        UserRecord user = userDao.getUser(idUser);
        if (user.getBlocked()) {
            throw new UserBlockedException();
        }
        if (!user.getConfirmed()) {
            throw new EmailNotConfirmedException();
        }
        return authenticationTokenDao.createAuthenticationToken(idUser, server, browser, ipAddress);
    }

}
