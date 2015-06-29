/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao;

import com.parallax.server.common.cloudsession.db.generated.tables.records.ConfirmtokenRecord;

/**
 *
 * @author Michel
 */
public interface ConfirmTokenDao {

    ConfirmtokenRecord getConfirmToken(String token);

    ConfirmtokenRecord createConfirmToken(Long idUser);

    int deleteConfirmToken(String token);

    int deleteConfirmToken(Long id);

    int deleteConfirmTokenForUser(Long idUser);

    int cleanExpiredTokens();

    ConfirmtokenRecord getConfirmTokenForUser(Long idUser);

}
