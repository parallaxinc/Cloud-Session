/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.service.DatabaseMaintenanceService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
public class DatabaseMaintenanceServiceImpl implements DatabaseMaintenanceService, Runnable {

    private Logger log = LoggerFactory.getLogger(DatabaseMaintenanceServiceImpl.class);

    private ScheduledExecutorService service;

    private UserDao userDao;

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
        setup();
    }

    public void setup() {
        this.service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        this.service.scheduleAtFixedRate(this, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        log.info("Keep db active: {}", userDao.count());
    }

}
