/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.ConfirmTokenDao;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.ConfirmtokenRecord;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.BucketService;
import com.parallax.server.common.cloudsession.service.ConfirmTokenService;
import com.parallax.server.common.cloudsession.service.MailService;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
@Singleton
public class ConfirmTokenServiceImpl implements ConfirmTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmTokenServiceImpl.class);

    private MailService mailService;

    private BucketService bucketService;

    private ConfirmTokenDao confirmTokenDao;

    private UserDao userDao;

    @Inject
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Inject
    public void setBucketService(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @Inject
    public void setConfirmTokenDao(ConfirmTokenDao confirmTokenDao) {
        this.confirmTokenDao = confirmTokenDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public ConfirmtokenRecord getConfirmToken(String token) {
        ConfirmtokenRecord confirmtoken = confirmTokenDao.getConfirmToken(token);
        if (confirmtoken != null) {
            confirmtoken.delete();
        }
        return confirmtoken;
    }

    @Override
    public boolean isValidConfirmToken(String token) {
        ConfirmtokenRecord confirmToken = getConfirmToken(token);
        if (confirmToken == null) {
            LOG.info("Unknown token: {}", token);
            return false;
        }
        if (confirmToken.getValidity().before(new Date())) {
            LOG.info("Token not valid anymore: {}", confirmToken.getValidity());
            return false;
        }
        return true;
    }

    @Override
    public ConfirmtokenRecord createConfirmToken(String server, Long idUser) throws InsufficientBucketTokensException, UnknownUserIdException {
        LOG.debug("Create new confirm token: {}", idUser);
        bucketService.consumeTokensInternal(idUser, "email-confirm", 1);

        UserRecord userRecord = userDao.getUser(idUser);
        if (userRecord.getConfirmed()) {
            LOG.info("Already confirmed: {}", idUser);
            return null;
        }
        confirmTokenDao.deleteConfirmTokenForUser(idUser);

        ConfirmtokenRecord confirmtokenRecord = confirmTokenDao.createConfirmToken(idUser);
        sendConfirmToken(server, userRecord, confirmtokenRecord.getToken());
        return confirmtokenRecord;
    }

    private void sendConfirmToken(String server, UserRecord userRecord, String token) {
        mailService.sendConfirmTokenEmail(server, userRecord, token);
    }

}
