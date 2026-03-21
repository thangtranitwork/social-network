package com.stu.socialnetworkapi.indicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Implement custom health check logic here
        // For example, check if a specific third-party service is reachable
        boolean isServiceUp = checkExternalService();

        if (isServiceUp) {
            return Health.up().withDetail("Custom Service", "Available and responsive").build();
        } else {
            return Health.down().withDetail("Custom Service", "Unreachable").build();
        }
    }

    private boolean checkExternalService() {
        // Mock external service check
        return true;
    }
}
