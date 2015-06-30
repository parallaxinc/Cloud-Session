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
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import java.util.Date;

/**
 *
 * @author Michel
 */
@Singleton
public class ResetTokenServiceImpl implements ResetTokenService {

    private ResetTokenDao resetTokenDao;

    private UserDao userDao;

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
    public boolean isValidResetToken(String token) {
        ResettokenRecord resetToken = getResetToken(token);
        if (resetToken == null) {
            return false;
        }
        if (resetToken.getValidity().after(new Date())) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param idUser
     * @return
     * @throws UnknownUserIdException
     */
    @Override
    public ResettokenRecord createResetToken(Long idUser) throws UnknownUserIdException {
        // confirm user exists
        userDao.getUser(idUser);
        return resetTokenDao.createResetToken(idUser);
    }

    @Override
    public ResettokenRecord createResetToken(String email) throws UnknownUserException {
        UserRecord user = userDao.getLocalUserByEmail(email);
        return resetTokenDao.createResetToken(user.getId());
    }

}
