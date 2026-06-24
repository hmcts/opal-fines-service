package uk.gov.hmcts.opal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opal.user-service.fmea")
public record UserServiceFmeaProperties(
    boolean enabled,
    String headerName,
    String headerValue
) {

    public UserServiceFmeaProperties {
        headerName = headerName == null || headerName.isBlank() ? "X-FMEA" : headerName;
        headerValue = headerValue == null || headerValue.isBlank() ? "true" : headerValue;
    }
}
