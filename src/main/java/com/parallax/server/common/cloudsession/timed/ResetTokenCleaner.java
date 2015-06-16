/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.timed;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.parallax.server.common.cloudsession.db.dao.ResetTokenDao;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;

/**
 *
 * @author Michel
 */
@Singleton
@Transactional
public class ResetTokenCleaner implements Job {

    private ResetTokenDao resetTokenDao;

    private Scheduler scheduler;

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Inject
    public void setResetTokenDao(ResetTokenDao resetTokenDao) {
        this.resetTokenDao = resetTokenDao;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        resetTokenDao.cleanExpiredTokens();
    }

}
