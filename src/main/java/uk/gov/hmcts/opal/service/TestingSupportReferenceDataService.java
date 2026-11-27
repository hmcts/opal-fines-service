package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.config.TestingSupportReferenceDataPublisherConfig.REF_DATA_SESSION_ID;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.refdata.RefDataHandlerRegistry;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
public class TestingSupportReferenceDataService {

    private static final String REF_DATA_SCHEMA = "ref-data/ref_data_schema.json";

    private final RefDataHandlerRegistry handlerRegistry;
    private final ObjectMapper objectMapper;
    private final JsonSchemaValidationService jsonSchemaValidationService;
    private final ServiceBusSenderClient testingSupportReferenceDataSender;
    private final CacheManager cacheManager;

    @Transactional
    public void deleteReferenceData(String refDataType, String businessKey) {
        RefDataUpdateHandler<?, ?> handler = handlerRegistry.find(refDataType)
            .orElseThrow(() -> new InvalidReferenceValidationException(
                "Unsupported reference data type: " + refDataType));

        try {
            handler.deleteEntity(businessKey);
        } catch (IllegalArgumentException exception) {
            throw new InvalidReferenceValidationException(exception.getMessage(), exception);
        }
        handler.cachesToClear().stream().distinct().forEach(this::clearCacheAfterCommit);
    }

    private void clearCacheAfterCommit(String cacheName) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    clearCache(cacheName);
                }
            });
        } else {
            clearCache(cacheName);
        }
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Unable to clear ref-data cache because no cache is registered with name: {}", cacheName);
            return;
        }
        cache.clear();
        log.info("Cleared ref-data cache: {}", cacheName);
    }

    public void publishReferenceData(Map<String, Object> requestBody) {
        JsonNode payload = objectMapper.valueToTree(requestBody);
        String messageBody = payload.toString();
        jsonSchemaValidationService.validateOrError(messageBody, REF_DATA_SCHEMA);

        ServiceBusMessage message = new ServiceBusMessage(messageBody);
        message.setSessionId(REF_DATA_SESSION_ID);
        testingSupportReferenceDataSender.sendMessage(message);
    }
}
