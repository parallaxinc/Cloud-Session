/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import com.parallax.server.common.cloudsession.service.UserService;
import javax.annotation.PostConstruct;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;

/**
 *
 * @author Michel
 */
public class UserServiceImpl implements UserService {

    private RandomNumberGenerator rng;

    private ResetTokenService resetTokenService;

    private UserDao userDao;

    @Inject
    public void setResetTokenSevice(ResetTokenService resetTokenService) {
        this.resetTokenService = resetTokenService;
    }

    @Inject
    public void setResetTokenService(ResetTokenService resetTokenService) {
        this.resetTokenService = resetTokenService;
    }

    @PostConstruct
    public void init() {
        rng = new SecureRandomNumberGenerator();
    }

    @Override
    public UserRecord resetPassword(String email, String token, String password, String repeatPassword) throws UnknownUserException {
        if (resetTokenService.isValidResetToken(token)) {
            UserRecord userRecord = userDao.getUserByEmail(email);
            if (changePassword(userRecord, password, repeatPassword)) {
                return userRecord;
            }
        }
        return null;
    }

    @Override
    public UserRecord changePassword(Long id, String oldPassword, String password, String repeatPassword) throws UnknownUserIdException {
        UserRecord userRecord = userDao.getUser(id);
        Sha256Hash oldPasswordHash = new Sha256Hash(oldPassword, userRecord.getSalt(), 1000);
        if (userRecord.getPassword().equals(oldPasswordHash.toHex())) {
            if (changePassword(userRecord, password, repeatPassword)) {
                return userRecord;
            }
        }
        return null;
    }

    private boolean changePassword(UserRecord userRecord, String password, String repeatPassword) {
        if (password.equals(repeatPassword)) {
            String salt = rng.nextBytes().toHex();
            Sha256Hash passwordHash = new Sha256Hash(password, salt, 1000);
            userRecord.setSalt(salt);
            userRecord.setPassword(passwordHash.toHex());
            userRecord.store();
            return true;
        } else {
            return false;
        }
    }

}
