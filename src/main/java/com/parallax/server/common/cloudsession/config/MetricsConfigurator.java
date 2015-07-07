/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.config;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.PickledGraphite;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import metrics_influxdb.InfluxdbHttp;
import metrics_influxdb.InfluxdbReporter;
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
        configureGraphite(metrics, configuration);
        configureInfluxDb(metrics, configuration);
    }

    private static void configureConsole(MetricRegistry metrics, Configuration configuration) {
        boolean enableConsole = configuration.getBoolean("metrics.console.enable", false);
        if (enableConsole) {
            int interval = configuration.getInt("metrics.console.interval", 30);
            ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            reporter.start(interval, TimeUnit.SECONDS);
        }
    }

    private static void configureLogger(MetricRegistry metrics, Configuration configuration) {
        boolean enableLogger = configuration.getBoolean("metrics.logger.enable", false);
        if (enableLogger) {
            int interval = configuration.getInt("metrics.logger.interval", 30);
            Slf4jReporter reporter = Slf4jReporter.forRegistry(metrics)
                    .outputTo(LoggerFactory.getLogger("com.parallax.server.common.cloudsession.metrics"))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            reporter.start(interval, TimeUnit.SECONDS);
        }
    }

    private static void configureGraphite(MetricRegistry metrics, Configuration configuration) {
        boolean enableGraphite = configuration.getBoolean("metrics.graphite.enable", false);
        if (enableGraphite) {
            int interval = configuration.getInt("metrics.graphite.interval", 30);
            final PickledGraphite pickledGraphite = new PickledGraphite(new InetSocketAddress(configuration.getString("metrics.graphite.host", "localhost"), 2004));
            final GraphiteReporter reporter = GraphiteReporter.forRegistry(metrics)
                    .prefixedWith(configuration.getString("metrics.graphite.prefix", "com.parallax.cloudsession"))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .build(pickledGraphite);
            reporter.start(interval, TimeUnit.SECONDS);
        }
    }

    private static void configureInfluxDb(MetricRegistry metrics, Configuration configuration) {
        boolean enableInfluxDb = configuration.getBoolean("metrics.influxdb.enable", false);
        if (enableInfluxDb) {
            try {
                int interval = configuration.getInt("metrics.influxdb.interval", 30);
                String host = configuration.getString("metrics.influxdb.host", "localhost");
                int port = configuration.getInt("metrics.influxdb.port", 8086);
                String database = configuration.getString("metrics.influxdb.database", "mydb");
                String user = configuration.getString("metrics.influxdb.user");
                String password = configuration.getString("metrics.influxdb.password");
                final InfluxdbHttp influxdb = new InfluxdbHttp(host, port, database, user, password); // http transport
                // = new InfluxDbUdp("127.0.0.1", 1234); // udp transport
                //influxdb.debugJson = true; // to print json on System.err
                //influxdb.jsonBuilder = new MyJsonBuildler(); // to use MyJsonBuilder to create json
                final InfluxdbReporter reporter = InfluxdbReporter
                        .forRegistry(metrics)
                        .prefixedWith(configuration.getString("metrics.influxdb.prefix", "com.parallax.cloudsession"))
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .filter(MetricFilter.ALL)
                        .skipIdleMetrics(true) // Only report metrics that have changed.
                        .build(influxdb);
                reporter.start(interval, TimeUnit.SECONDS);
            } catch (Exception ex) {
                Logger.getLogger(MetricsConfigurator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
