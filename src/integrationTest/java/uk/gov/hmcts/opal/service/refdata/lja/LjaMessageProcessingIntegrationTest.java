package uk.gov.hmcts.opal.service.refdata.lja;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestConstructor;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.refdata.RefDataMessageProcessor;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@SpringBootTest(properties = "opal.ref-data.service-bus.consumer-enabled=true")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LjaMessageProcessingIntegrationTest extends AbstractIntegrationTest {

    private static final short TEST_ID_START = 8000;

    private final RefDataMessageProcessor processor;
    private final LocalJusticeAreaRepository repository;
    private final LocalJusticeAreaRefDataHandler handler;
    private final CacheManager cacheManager;

    @Test
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-10709")
    void processesValidLjaMessage() {
        short localJusticeAreaId = 8102;

        processor.processMessage(LjaMessageBuilder.messageWithAddresses(localJusticeAreaId));

        try {
            assertThat(repository.findById(localJusticeAreaId))
                .get()
                .satisfies(entity -> {
                    assertThat(entity.getLocalJusticeAreaId()).isEqualTo(localJusticeAreaId);
                    assertThat(entity.getName()).isEqualTo("Test LJA");
                    assertThat(entity.getLjaType()).isEqualTo(LocalJusticeAreaType.CRWCRT);
                    assertThat(entity.getAddressLine1()).isEqualTo("Court line 1");
                    assertThat(entity.getAddressLine2()).isEqualTo("Court line 2");
                    assertThat(entity.getAddressLine3()).isEqualTo("Court line 3");
                    assertThat(entity.getAddressLine4()).isEqualTo("Court line 4");
                    assertThat(entity.getAddressLine5()).isEqualTo("Court line 5");
                    assertThat(entity.getPostcode()).isEqualTo("GIR 0AA");
                });
        } finally {
            repository.deleteById(localJusticeAreaId);
        }
    }

    @ParameterizedTest(name = "{1} maps to {2}")
    @MethodSource("ljaTypeMappings")
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-10709")
    void mapsSupportedLjaTypes(short localJusticeAreaId, String ljaType, LocalJusticeAreaType expectedType) {
        processor.processMessage(LjaMessageBuilder.validMessage(localJusticeAreaId, ljaType));

        try {
            assertThat(repository.findById(localJusticeAreaId))
                .get()
                .extracting(LocalJusticeAreaEntity::getLjaType)
                .isEqualTo(expectedType);
        } finally {
            repository.deleteById(localJusticeAreaId);
        }
    }

    private static Stream<Arguments> ljaTypeMappings() {
        return Stream.of(
            Arguments.of(TEST_ID_START, "CRWCRT", LocalJusticeAreaType.CRWCRT),
            Arguments.of((short) (TEST_ID_START + 1), "SJCRT", LocalJusticeAreaType.SJCRT),
            Arguments.of((short) (TEST_ID_START + 2), "NICRT", LocalJusticeAreaType.NICRT),
            Arguments.of((short) (TEST_ID_START + 3), "SCSCRT", LocalJusticeAreaType.SCSCRT),
            Arguments.of((short) (TEST_ID_START + 4), "YCT", LocalJusticeAreaType.YCT),
            Arguments.of((short) (TEST_ID_START + 5), "SCDCRT", LocalJusticeAreaType.SCDCRT),
            Arguments.of((short) (TEST_ID_START + 6), "PSA", LocalJusticeAreaType.LJA)
        );
    }

    @Test
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-10709")
    void usesCourtAddressWhenItIsNotTheFirstAddress() {
        short localJusticeAreaId = 8100;

        processor.processMessage(LjaMessageBuilder.messageWithAddresses(localJusticeAreaId));

        try {
            assertThat(repository.findById(localJusticeAreaId))
                .get()
                .satisfies(entity -> {
                    assertThat(entity.getAddressLine1()).isEqualTo("Court line 1");
                    assertThat(entity.getAddressLine2()).isEqualTo("Court line 2");
                    assertThat(entity.getAddressLine3()).isEqualTo("Court line 3");
                    assertThat(entity.getAddressLine4()).isEqualTo("Court line 4");
                    assertThat(entity.getAddressLine5()).isEqualTo("Court line 5");
                    assertThat(entity.getPostcode()).isEqualTo("GIR 0AA");
                });
        } finally {
            repository.deleteById(localJusticeAreaId);
        }
    }

    @Test
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-10709")
    void doesNotUpdateEndDateWhenMessageIsProcessed() {
        short localJusticeAreaId = 8101;

        LocalJusticeAreaEntity existing = new LocalJusticeAreaEntity();
        existing.setLocalJusticeAreaId(localJusticeAreaId);
        existing.setName("Existing LJA");
        existing.setLjaType(LocalJusticeAreaType.CRWCRT);
        existing.setEndDate(LocalDateTime.of(2027, 3, 4, 0, 0));
        existing.setAddressLine1("Existing address");
        existing.setPostcode("GIR 0AA");
        repository.saveAndFlush(existing);

        try {
            processor.processMessage(LjaMessageBuilder.messageWithEndDate(localJusticeAreaId, "2028-03-04"));

            assertThat(repository.findById(localJusticeAreaId))
                .get()
                .extracting(LocalJusticeAreaEntity::getEndDate)
                .isEqualTo(LocalDateTime.of(2027, 3, 4, 0, 0));
        } finally {
            repository.deleteById(localJusticeAreaId);
        }
    }

    @Test
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-10709")
    void deleteEntityDeletesLjaUsingBusinessKeyWithLeadingZeros() {
        short localJusticeAreaId = 8103;
        repository.saveAndFlush(LjaMessageBuilder.entity(localJusticeAreaId));

        try {
            handler.deleteEntity("08103");

            assertThat(repository.findById(localJusticeAreaId)).isEmpty();
        } finally {
            repository.deleteById(localJusticeAreaId);
        }
    }

    @Test
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-10709")
    void clearsOpalLjaReferenceDataCacheWhenMessageIsProcessed() {
        var cache = cacheManager.getCache("opalLjaReferenceDataCache");
        assertThat(cache).isNotNull();
        cache.put("test-key", "cached-value");
        assertThat(cache.get("test-key")).isNotNull();

        processor.processMessage(LjaMessageBuilder.validMessage((short) 8104, "CRWCRT"));

        try {
            assertThat(cache.get("test-key")).isNull();
        } finally {
            repository.deleteById((short) 8104);
        }
    }

}
