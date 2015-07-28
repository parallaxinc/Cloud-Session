/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.config;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.palominolabs.metrics.guice.MetricsInstrumentationModule;
import com.parallax.server.common.cloudsession.service.TokenGeneratorService;
import com.parallax.server.common.cloudsession.service.impl.UUIDTokenGeneratorServiceImpl;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContextEvent;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;

/**
 *
 * @author Michel
 */
public class SetupConfig extends GuiceServletContextListener {

    private Configuration configuration;
    private MetricRegistry metrics;

    @Override
    protected Injector getInjector() {
        readConfiguration();

        metrics = new MetricRegistry();
        MetricsConfigurator.configure(metrics, configuration);

        return Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                install(new MetricsInstrumentationModule(metrics));

                bind(MetricRegistry.class).toInstance(metrics);

                bind(Configuration.class).toInstance(configuration);
                bind(TokenGeneratorService.class).to(UUIDTokenGeneratorServiceImpl.class);

                install(new PersistenceModule(configuration));
                install(new DaoModule());
                install(new ServiceModule());
                install(new RestModule());
            }

        }
        //        new PersistenceModule(configuration)
        //new DaoModule()
        //new ServletsModule()
        );
    }

    private void readConfiguration() {
        try {
            System.out.println("Looking for cloudsession.properties in: " + System.getProperty("user.home"));
            DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder(getClass().getResource("/config.xml"));
            configuration = configurationBuilder.getConfiguration();
        } catch (ConfigurationException ce) {
            ce.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        // Stop reporters
        MetricsConfigurator.stopReporters();

        // This manually deregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks wrto this class
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                //  LOG.log(Level.INFO, String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException sqlE) {
                //   LOG.log(Level.SEVERE, String.format("Error deregistering driver %s", driver), e);
            }

        }
    }

}
