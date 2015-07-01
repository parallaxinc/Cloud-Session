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
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.ConfirmTokenService;
import com.parallax.server.common.cloudsession.service.MailService;
import java.util.Date;

/**
 *
 * @author Michel
 */
@Singleton
public class ConfirmTokenServiceImpl implements ConfirmTokenService {

    private MailService mailService;

    private ConfirmTokenDao confirmTokenDao;

    private UserDao userDao;

    @Inject
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
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
            return false;
        }
        if (confirmToken.getValidity().after(new Date())) {
            return false;
        }
        return true;
    }

    @Override
    public ConfirmtokenRecord createConfirmToken(String server, Long idUser) {
        try {
            UserRecord userRecord = userDao.getUser(idUser);
            if (userRecord.getConfirmed()) {
                return null;
            }
            confirmTokenDao.deleteConfirmTokenForUser(idUser);

            ConfirmtokenRecord confirmtokenRecord = confirmTokenDao.createConfirmToken(idUser);
            sendConfirmToken(server, userRecord, confirmtokenRecord.getToken());
            return confirmtokenRecord;
        } catch (UnknownUserIdException ex) {
            return null;
        }
    }

    private void sendConfirmToken(String server, UserRecord userRecord, String token) {
        System.out.println(server + ": " + userRecord.getEmail() + " -> " + token);
        mailService.sendConfirmTokenEmail(server, userRecord, token);
    }

}
