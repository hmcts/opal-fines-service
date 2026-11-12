package uk.gov.hmcts.opal.service.refdata.lja;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import tools.jackson.databind.JsonNode;
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

    private static final String REF_DATA_SCHEMA = "jsonSchemas/ref-data/ref_data_schema.json";
    private static final short TEST_ID_START = 8000;

    private final RefDataMessageProcessor processor;
    private final LocalJusticeAreaRepository repository;
    private final LocalJusticeAreaRefDataHandler handler;

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

    @Test
    @JiraStory("PO-10709")
    @JiraEpic("PO-9031")
    @JiraTestKey("PO-10709")
    void savesEveryLjaTypeDefinedInTheOpenApiSchema() {
        List<String> ljaTypes = ljaTypesFromSchema();

        try {
            for (int index = 0; index < ljaTypes.size(); index++) {
                String ljaType = ljaTypes.get(index);
                short localJusticeAreaId = (short) (TEST_ID_START + index);
                processor.processMessage(LjaMessageBuilder.validMessage(localJusticeAreaId, ljaType));

                assertThat(repository.findById(localJusticeAreaId))
                    .get()
                    .extracting(LocalJusticeAreaEntity::getLjaType)
                    .isEqualTo(expectedLocalJusticeAreaType(ljaType));
            }
        } finally {
            for (int index = 0; index < ljaTypes.size(); index++) {
                repository.deleteById((short) (TEST_ID_START + index));
            }
        }
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
    void deleteEntityRejectsInvalidBusinessKey() {
        assertThatThrownBy(() -> handler.deleteEntity("not-a-short"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid business key received: not-a-short");

        assertThatThrownBy(() -> handler.deleteEntity("32768"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid business key received: 32768");
    }

    private LocalJusticeAreaType expectedLocalJusticeAreaType(String ljaType) {
        return "PSA".equals(ljaType) ? LocalJusticeAreaType.LJA : LocalJusticeAreaType.valueOf(ljaType);
    }

    private List<String> ljaTypesFromSchema() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(REF_DATA_SCHEMA)) {
            if (inputStream == null) {
                throw new IllegalStateException("Unable to find " + REF_DATA_SCHEMA);
            }

            JsonNode enumValues = objectMapper.readTree(inputStream)
                .path("$defs")
                .path("ljaRecord")
                .path("properties")
                .path("lja_type")
                .path("enum");
            List<String> ljaTypes = new ArrayList<>();
            enumValues.forEach(value -> ljaTypes.add(value.asString()));
            return ljaTypes;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read LJA types from " + REF_DATA_SCHEMA, exception);
        }
    }
}
