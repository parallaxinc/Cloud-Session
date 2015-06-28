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
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.ConfirmTokenService;
import java.util.Date;

/**
 *
 * @author Michel
 */
@Singleton
public class ConfirmTokenServiceImpl implements ConfirmTokenService {

    private ConfirmTokenDao confirmTokenDao;

    private UserDao userDao;

    @Inject
    public void setConfirmTokenDao(ConfirmTokenDao confirmTokenDao) {
        this.confirmTokenDao = confirmTokenDao;
    }

    @Override
    public ConfirmtokenRecord getConfirmToken(String token) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ConfirmtokenRecord getConfirmTokenForUser(String email) throws UnknownUserException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ConfirmtokenRecord getConfirmTokenForUser(Long idUser) throws UnknownUserIdException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isValidConfirmToken(String token) {
        ConfirmtokenRecord confirmToken = getConfirmToken(token);
        if (confirmToken == null) {
            return false;
        }
        if (confirmToken.getValidity().after(new Date())) {
            return false;
        }
        return true;
    }

    @Override
    public ConfirmtokenRecord createConfirmToken(Long idUser) {
        try {
            // confirm user exists
            userDao.getUser(idUser);
        } catch (UnknownUserIdException ex) {
            ex.printStackTrace();;
        }
        return confirmTokenDao.createConfirmToken(idUser);
    }

}
