package com.threedsoft.test;

import static net.logstash.logback.marker.Markers.append;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Allows to expose actuator metrics
 *
 * @author lotarvad
 */
@Service
@Slf4j
class MetricAndHealthExporterService {
    @Autowired
    private MetricsEndpoint metricsEndpoint;

    @Autowired
    private HealthEndpoint healthEndpoint;

/*    *//**
     * Exposes all metrics each 10 minutes after an initial delay of a minute
     *//*
    @Scheduled(initialDelay = 60000, fixedDelay = 600000)
    void exportMetrics() {
        this.metricsEndpoint..invoke().forEach(this::log);
    }

    *//**
     * Pushes heart beats every 10 seconds
     *//*
    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    void pushHeartbeat() {
        Health health = this.healthEndpoint.invoke();
        LOGGER.info(append("Heartbeat", health.getStatus()), "Heartbeat details {}", health.getDetails());
    }*/

    private void log(String metricName, Object metricValue) {
        log.info(append("metric", metricName), "Reporting metric {}={}", metricName, metricValue);
    }
}