/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.db.utils.DataSourceSetup;
import com.parallax.server.common.cloudsession.db.utils.NeedsDataSource;
import com.parallax.server.common.cloudsession.service.AuthenticationTokenService;
import com.parallax.server.common.cloudsession.service.DatabaseMaintenanceService;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
public class DatabaseMaintenanceServiceImpl implements DatabaseMaintenanceService, NeedsDataSource, Runnable {

    private final Logger log = LoggerFactory.getLogger(DatabaseMaintenanceServiceImpl.class);

    private ScheduledExecutorService service;

//    private UserDao userDao;
    private AuthenticationTokenService authenticationTokenService;

    private DataSource dataSource;

    public DatabaseMaintenanceServiceImpl() {
        DataSourceSetup.registerDataSourceUsers(this);
    }

//    @Inject
//    public void setUserDao(UserDao userDao) {
//        this.userDao = userDao;
//        setup();
//    }
    @Inject
    public void setAuthenticationTokenService(AuthenticationTokenService authenticationTokenService) {
        this.authenticationTokenService = authenticationTokenService;
        setup();
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setup() {
        this.service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                //thread.setDaemon(false);
                return thread;
            }
        });
        this.service.scheduleAtFixedRate(this, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        //  log.info("Keep db active: {}", userDao.count());
        try {
            log.info("Clean authentication tokens and keep db active: removed {}", authenticationTokenService.cleanExpiredAutheticationTokens());
        } catch (Throwable t) {
            log.error("ALERT: Problem cleaning authentication tokens and keeping db active", t);
        }
        if (this.dataSource != null) {
            try {
                Connection connection = dataSource.getConnection();
                connection.prepareStatement("SELECT 1").executeQuery();
            } catch (Throwable t) {
                log.error("ALERT: Problem keeping db active", t);
            }
        }
    }

}
