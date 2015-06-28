/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service;

import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;

/**
 *
 * @author Michel
 */
public interface UserService {

    UserRecord resetPassword(String email, String token, String password, String repeatPassword) throws UnknownUserException;

    UserRecord changePassword(Long id, String oldPassword, String password, String repeatPassword) throws UnknownUserIdException;

}
