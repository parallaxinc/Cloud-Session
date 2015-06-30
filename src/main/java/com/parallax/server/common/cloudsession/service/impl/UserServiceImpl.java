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
import com.parallax.server.common.cloudsession.exceptions.NonUniqueEmailException;
import com.parallax.server.common.cloudsession.exceptions.PasswordVerifyException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.ConfirmTokenService;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import com.parallax.server.common.cloudsession.service.UserService;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;

/**
 *
 * @author Michel
 */
public class UserServiceImpl implements UserService {

    private final RandomNumberGenerator rng;

    private ResetTokenService resetTokenService;

    private ConfirmTokenService confirmTokenService;

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
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserServiceImpl() {
        rng = new SecureRandomNumberGenerator();
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
                return userRecord;
            }
        }
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
                return userRecord;
            }
        }
        return null;
    }

    @Override
    public UserRecord register(String server, String email, String password, String passwordConfirm) throws PasswordVerifyException, NonUniqueEmailException {
        if (!password.equals(passwordConfirm)) {
            throw new PasswordVerifyException();
        }
        String salt = rng.nextBytes().toHex();
        Sha256Hash passwordHash = new Sha256Hash(password, salt, 1000);
        UserRecord userRecord = userDao.createLocalUser(email, passwordHash.toHex(), salt);

        confirmTokenService.createConfirmToken(server, userRecord.getId());

        return userRecord;
    }

    @Override
    public UserRecord authenticateLocal(String email, String password) throws UnknownUserException {
        UserRecord userRecord = userDao.getLocalUserByEmail(email);
        Sha256Hash passwordHash = new Sha256Hash(password, userRecord.getSalt(), 1000);
        if (userRecord.getPassword().equals(passwordHash.toHex())) {
            return userRecord;
        }
        return null;
    }

    @Override
    public UserRecord getLocalUser(String email) throws UnknownUserException {
        return userDao.getLocalUserByEmail(email);
    }

}
