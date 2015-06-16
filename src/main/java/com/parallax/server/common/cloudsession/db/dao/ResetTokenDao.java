/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao;

import com.parallax.server.common.cloudsession.db.generated.tables.records.ResettokenRecord;

/**
 *
 * @author Michel
 */
public interface ResetTokenDao {

    ResettokenRecord getResetToken(String token);

    ResettokenRecord createResetToken(Long idUser);

    int deleteResetToken(String token);

    int deleteResetToken(Long id);

    int deleteResetTokenForUser(Long idUser);

    int cleanExpiredTokens();

}
