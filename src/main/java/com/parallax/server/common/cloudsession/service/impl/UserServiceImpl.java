/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.ConfirmtokenRecord;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.exceptions.EmailNotConfirmedException;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensException;
import com.parallax.server.common.cloudsession.exceptions.NonUniqueEmailException;
import com.parallax.server.common.cloudsession.exceptions.PasswordVerifyException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.exceptions.UserBlockedException;
import com.parallax.server.common.cloudsession.service.BucketService;
import com.parallax.server.common.cloudsession.service.ConfirmTokenService;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import com.parallax.server.common.cloudsession.service.UserService;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final RandomNumberGenerator rng;

    private ResetTokenService resetTokenService;

    private ConfirmTokenService confirmTokenService;

    private BucketService bucketService;

    private UserDao userDao;

    @Inject
    public void setResetTokenSevice(ResetTokenService resetTokenService) {
        this.resetTokenService = resetTokenService;
    }

    @Inject
    public void setConfirmTokenService(ConfirmTokenService confirmTokenService) {
        this.confirmTokenService = confirmTokenService;
    }

    @Inject
    public void setBucketService(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserServiceImpl() {
        rng = new SecureRandomNumberGenerator();
    }

    public UserServiceImpl(RandomNumberGenerator rng) {
        this.rng = rng;
    }

    @Override
    public UserRecord resetPassword(String email, String token, String password, String repeatPassword) throws PasswordVerifyException, UnknownUserException {
        if (resetTokenService.isValidResetToken(token)) {
            UserRecord userRecord = userDao.getLocalUserByEmail(email);
            if (changePassword(userRecord, password, repeatPassword)) {
                return userRecord;
            }
        }
        return null;
    }

    @Override
    public UserRecord changePassword(Long id, String oldPassword, String password, String repeatPassword) throws PasswordVerifyException, UnknownUserIdException {
        UserRecord userRecord = userDao.getUser(id);
        Sha256Hash oldPasswordHash = new Sha256Hash(oldPassword, userRecord.getSalt(), 1000);
        if (userRecord.getPassword().equals(oldPasswordHash.toHex())) {
            if (changePassword(userRecord, password, repeatPassword)) {
                LOG.info("Password successfully changed: {}", userRecord.getEmail());
                return userRecord;
            }
        }
        LOG.info("Password change failed: {}", userRecord.getEmail());
        return null;
    }

    private boolean changePassword(UserRecord userRecord, String password, String repeatPassword) throws PasswordVerifyException {
        if (!password.equals(repeatPassword)) {
            throw new PasswordVerifyException();
        }
        String salt = rng.nextBytes().toHex();
        Sha256Hash passwordHash = new Sha256Hash(password, salt, 1000);
        userRecord.setSalt(salt);
        userRecord.setPassword(passwordHash.toHex());
        userRecord.store();
        return true;
    }

    @Override
    public UserRecord confirmEmail(String email, String token) throws UnknownUserException {
        UserRecord userRecord = userDao.getLocalUserByEmail(email);
        ConfirmtokenRecord confirmtokenRecord = confirmTokenService.getConfirmToken(token);
        if (confirmtokenRecord != null) {
            if (confirmtokenRecord.getIdUser().equals(userRecord.getId())) {
                userRecord.setConfirmed(true);
                userRecord.store();
                LOG.info("Email confirm successfull: {}", email);
                return userRecord;
            }
        }
        LOG.info("Email confirm failed: {}", email);
        return null;
    }

    @Override
    public UserRecord register(String server, String email, String password, String passwordConfirm, String language) throws PasswordVerifyException, NonUniqueEmailException {
        if (!password.equals(passwordConfirm)) {
            throw new PasswordVerifyException();
        }
        String salt = rng.nextBytes().toHex();
        Sha256Hash passwordHash = new Sha256Hash(password, salt, 1000);
        UserRecord userRecord = userDao.createLocalUser(email, passwordHash.toHex(), salt, language);

        try {
            confirmTokenService.createConfirmToken(server, userRecord.getId());
        } catch (UnknownUserIdException uuie) {
            return null;
        } catch (InsufficientBucketTokensException ibte) {
            return null;
        }
        LOG.info("User registered: {}", email);
        return userRecord;
    }

    @Override
    public UserRecord authenticateLocal(String email, String password) throws UnknownUserException, InsufficientBucketTokensException, EmailNotConfirmedException, UserBlockedException {
        UserRecord userRecord = userDao.getLocalUserByEmail(email);

        // check if failed attemps are exceeded, but do not take a token (as much successfull attempts as wanted are allowed)
        bucketService.hasSufficientTokensInternal(userRecord.getId(), "failed-password", 1);

        Sha256Hash passwordHash = new Sha256Hash(password, userRecord.getSalt(), 1000);
        if (userRecord.getPassword().equals(passwordHash.toHex())) {
            if (userRecord.getBlocked()) {
                throw new UserBlockedException();
            }
            if (!userRecord.getConfirmed()) {
                throw new EmailNotConfirmedException();
            }
            return userRecord;
        }

        LOG.info("Authentication failed due to wrong password: {}", email);

        // Consume token on password failure
        bucketService.consumeTokensInternal(userRecord.getId(), "failed-password", 1);
        return null;
    }

    @Override
    public UserRecord getLocalUser(String email) throws UnknownUserException {
        return userDao.getLocalUserByEmail(email);
    }

}
