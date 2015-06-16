/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service;

import com.parallax.server.common.cloudsession.db.generated.tables.records.ResettokenRecord;

/**
 *
 * @author Michel
 */
public interface ResetTokenService {

    ResettokenRecord getResetToken(String token);

    boolean isValidResetToken(String token);

    ResettokenRecord createResetToken(Long idUser);

}
