/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.service.TokenGeneratorService;
import java.util.UUID;

/**
 *
 * @author Michel
 */
@Singleton
public class UUIDTokenGeneratorServiceImpl implements TokenGeneratorService {

    @Override
    public String generateToken() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }

}
