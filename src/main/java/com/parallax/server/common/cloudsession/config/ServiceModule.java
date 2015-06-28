/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.config;

import com.google.inject.AbstractModule;
import com.parallax.server.common.cloudsession.service.ConfirmTokenService;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import com.parallax.server.common.cloudsession.service.UserService;
import com.parallax.server.common.cloudsession.service.impl.ConfirmTokenServiceImpl;
import com.parallax.server.common.cloudsession.service.impl.ResetTokenServiceImpl;
import com.parallax.server.common.cloudsession.service.impl.UserServiceImpl;

/**
 *
 * @author Michel
 */
public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ResetTokenService.class).to(ResetTokenServiceImpl.class);
        bind(ConfirmTokenService.class).to(ConfirmTokenServiceImpl.class);
        bind(UserService.class).to(UserServiceImpl.class);
    }

}
