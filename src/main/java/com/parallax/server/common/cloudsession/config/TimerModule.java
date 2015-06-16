/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.parallax.server.common.cloudsession.timed.ResetTokenCleaner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author Michel
 */
public class TimerModule extends AbstractModule {

    private Scheduler scheduler;

    public TimerModule() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
        } catch (SchedulerException ex) {
            Logger.getLogger(TimerModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void configure() {
        bind(Scheduler.class);
        bind(ResetTokenCleaner.class).asEagerSingleton();
        // scheduleJob(ResetTokenCleaner.class).withCronExpression("0 0 * * * ?");
    }

    @Provides
    public Scheduler scheduler() {
        return scheduler;
    }

}
