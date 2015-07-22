/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.ResetTokenDao;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.ResettokenRecord;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.BucketService;
import com.parallax.server.common.cloudsession.service.MailService;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
@Singleton
public class ResetTokenServiceImpl implements ResetTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(ResetTokenServiceImpl.class);

    private MailService mailService;

    private BucketService bucketService;

    private ResetTokenDao resetTokenDao;

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
    public void setResetTokenDao(ResetTokenDao resetTokenDao) {
        this.resetTokenDao = resetTokenDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public ResettokenRecord getResetToken(String token) {
        return resetTokenDao.getResetToken(token);
    }

    @Override
    public boolean consumeResetToken(String token) {
        ResettokenRecord resetToken = resetTokenDao.getResetToken(token);
        if (resetToken != null) {
            resetToken.delete();
            return true;
        }
        return false;
    }

    @Override
    public boolean isValidResetToken(String token) {
        ResettokenRecord resetToken = getResetToken(token);
        if (resetToken == null) {
            LOG.info("Unknown token: {}", token);
            return false;
        }
        if (resetToken.getValidity().before(new Date())) {
            LOG.info("Token not valid anymore: {}", resetToken.getValidity());
            return false;
        }
        return true;
    }

    @Override
    public ResettokenRecord createResetToken(String server, Long idUser) throws UnknownUserIdException, InsufficientBucketTokensException {
        LOG.debug("Create new reset token: {}", idUser);
        UserRecord user = userDao.getUser(idUser);

        bucketService.consumeTokensInternal(idUser, "password-reset", 1);

        resetTokenDao.deleteResetTokenForUser(idUser);

        ResettokenRecord resettokenRecord = resetTokenDao.createResetToken(idUser);
        sendResetToken(server, user, resettokenRecord.getToken());
        return resettokenRecord;
    }

    @Override
    public ResettokenRecord createResetToken(String server, String email) throws UnknownUserException, InsufficientBucketTokensException {
        LOG.debug("Create new reset token: {}", email);
        UserRecord user = userDao.getLocalUserByEmail(email);
        bucketService.consumeTokensInternal(user.getId(), "password-reset", 1);

        resetTokenDao.deleteResetTokenForUser(user.getId());

        ResettokenRecord resettokenRecord = resetTokenDao.createResetToken(user.getId());
        sendResetToken(server, user, resettokenRecord.getToken());
        return resettokenRecord;
    }

    private void sendResetToken(String server, UserRecord userRecord, String token) {
        mailService.sendResetTokenEmail(server, userRecord, token);
    }

}
