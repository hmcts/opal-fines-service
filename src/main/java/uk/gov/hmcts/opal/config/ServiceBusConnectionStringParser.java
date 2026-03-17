package uk.gov.hmcts.opal.config;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

final class ServiceBusConnectionStringParser {

    private ServiceBusConnectionStringParser() {
        // Utility class
    }

    static ConnectionDetails parse(String connectionString) {
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalArgumentException("Connection string must not be blank");
        }

        Map<String, String> parts = Arrays.stream(connectionString.split(";"))
            .map(String::trim)
            .filter(part -> !part.isEmpty())
            .map(part -> part.split("=", 2))
            .collect(Collectors.toMap(
                entry -> entry[0],
                entry -> entry.length > 1 ? entry[1] : ""
            ));

        String endpoint = parts.get("Endpoint");
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("Connection string missing endpoint segment");
        }

        URI endpointUri;
        try {
            endpointUri = URI.create(endpoint);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Endpoint segment invalid uri", ex);
        }
        String host = endpointUri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Endpoint segment missing host");
        }

        String keyName = parts.get("SharedAccessKeyName");
        if (keyName == null || keyName.isBlank()) {
            throw new IllegalArgumentException("Connection string missing SharedAccessKeyName");
        }

        String key = parts.get("SharedAccessKey");
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Connection string missing SharedAccessKey");
        }

        return new ConnectionDetails(host, keyName, key);
    }

    record ConnectionDetails(String fullyQualifiedNamespace,
                             String sharedAccessKeyName,
                             String sharedAccessKey) {
    }
}
