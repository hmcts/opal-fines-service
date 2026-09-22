package uk.gov.hmcts.opal.service.refdata.framework;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.util.FeatureFlags;

@ExtendWith(MockitoExtension.class)
class RefDataMessageProcessorTest {

    private static final String CACHE_NAME = "ljaReferenceDataCache";
    private static final String REF_DATA_MESSAGE_SCHEMA = "ref-data/ref_data_schema.json";
    private static final Object ENTITY = new Object();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SchemaValidationService schemaValidationService;

    @Mock
    private RefDataHandlerRegistry handlerRegistry;

    @Mock
    private FeatureToggleApi featureToggleApi;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private RefDataUpdateMapper<JsonNode, Object> mapper;

    private RefDataMessageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new RefDataMessageProcessor(
            objectMapper,
            schemaValidationService,
            handlerRegistry,
            featureToggleApi,
            cacheManager
        );

        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING,
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING_ENABLED_PROPERTY,
            false
        )).thenReturn(true);
    }

    @Test
    void processMessage_clearsHandlerCachesAfterSuccessfulUpdate() {
        RefDataUpdateHandler<JsonNode, Object> handler = new ClearingTestRefDataUpdateHandler(mapper);
        when(handlerRegistry.find("LJA")).thenReturn(Optional.of(handler));
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);

        processor.processMessage(messagePayload());

        verify(schemaValidationService).validateOrError(any(JsonNode.class), eq(REF_DATA_MESSAGE_SCHEMA));
        verify(mapper).updateEntityFromDto(any(JsonNode.class), eq(ENTITY));
        verify(cache).clear();
    }

    @Test
    void processMessage_doesNotClearCachesWhenHandlerUsesDefaultCacheNames() {
        RefDataUpdateHandler<JsonNode, Object> handler = new TestRefDataUpdateHandler(mapper);
        when(handlerRegistry.find("LJA")).thenReturn(Optional.of(handler));

        processor.processMessage(messagePayload());

        verify(cacheManager, never()).getCache(any());
    }

    private String messagePayload() {
        return """
            {
              "header": {
                "message_id": "437dacf6-511c-4e93-95f3-23e82b12e735",
                "message_type": "ReferenceData",
                "data_product": "LJA",
                "operation": "PUBLISH",
                "source_system": "Semarchy",
                "created_date_time": "2026-09-02T08:28:56.935738+00:00",
                "release_package_id": 202,
                "record_count": 1
              },
              "payload": {
                "records": [
                  {
                    "lja_code": "1",
                    "lja_name": "Test LJA",
                    "lja_type": "LJA",
                    "start_date": "2027-03-01",
                    "addresses": [
                      {
                        "address_type": "Test Address",
                        "address_line_1": "1 Test Street",
                        "post_code": "NE1 2BB"
                      },
                      {
                        "address_type": "Secondary Address",
                        "address_line_1": "2 Test Street",
                        "post_code": "NE1 2BB"
                      }
                    ],
                    "contact_information": []
                  }
                ]
              }
            }
            """;
    }

    private static class TestRefDataUpdateHandler implements RefDataUpdateHandler<JsonNode, Object> {

        private final RefDataUpdateMapper<JsonNode, Object> mapper;

        private TestRefDataUpdateHandler(RefDataUpdateMapper<JsonNode, Object> mapper) {
            this.mapper = mapper;
        }

        @Override
        public String refDataType() {
            return "LJA";
        }

        @Override
        public Class<JsonNode> payloadType() {
            return JsonNode.class;
        }

        @Override
        public void validateDto(JsonNode dto) {
        }

        @Override
        public Optional<Object> findEntity(JsonNode dto) {
            return Optional.of(ENTITY);
        }

        @Override
        public Object createEntity(JsonNode dto) {
            return ENTITY;
        }

        @Override
        public Object saveEntity(Object entity) {
            return entity;
        }

        @Override
        public RefDataUpdateMapper<JsonNode, Object> mapper() {
            return mapper;
        }
    }

    private static class ClearingTestRefDataUpdateHandler extends TestRefDataUpdateHandler {

        private ClearingTestRefDataUpdateHandler(RefDataUpdateMapper<JsonNode, Object> mapper) {
            super(mapper);
        }

        @Override
        public List<String> cachesToClear() {
            return List.of(CACHE_NAME);
        }
    }
}
