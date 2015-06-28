/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao;

import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;

/**
 *
 * @author Michel
 */
public interface UserDao {

    UserRecord getUser(Long id) throws UnknownUserIdException;

    UserRecord getUserByEmail(String email) throws UnknownUserException;

}
