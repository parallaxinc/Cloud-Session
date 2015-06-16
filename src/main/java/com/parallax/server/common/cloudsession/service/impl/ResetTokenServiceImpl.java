/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.ResetTokenDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.ResettokenRecord;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import java.util.Date;

/**
 *
 * @author Michel
 */
@Singleton
public class ResetTokenServiceImpl implements ResetTokenService {

    private ResetTokenDao resetTokenDao;

    @Inject
    public void setResetTokenDao(ResetTokenDao resetTokenDao) {
        this.resetTokenDao = resetTokenDao;
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

    @Override
    public ResettokenRecord createResetToken(Long idUser) {
        return resetTokenDao.createResetToken(idUser);
    }

}
