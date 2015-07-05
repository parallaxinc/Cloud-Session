/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.config;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
public class MetricsConfigurator {

    public static void configure(MetricRegistry metrics, Configuration configuration) {
        configureConsole(metrics, configuration);
        configureLogger(metrics, configuration);
    }

    private static void configureConsole(MetricRegistry metrics, Configuration configuration) {
        boolean enableConsole = configuration.getBoolean("metrics.console.enable", false);
        if (enableConsole) {
            int interval = configuration.getInt("metrics.console.interval", 5);
            ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            reporter.start(interval, TimeUnit.MINUTES);
        }
    }

    private static void configureLogger(MetricRegistry metrics, Configuration configuration) {
        boolean enableLogger = configuration.getBoolean("metrics.logger.enable", false);
        if (enableLogger) {
            int interval = configuration.getInt("metrics.logger.interval", 5);
            Slf4jReporter reporter = Slf4jReporter.forRegistry(metrics)
                    .outputTo(LoggerFactory.getLogger("com.parallax.server.common.cloudsession.metrics"))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            reporter.start(interval, TimeUnit.MINUTES);
        }
    }

}
