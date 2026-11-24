package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.ObjectMapperConfiguration;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.config.PdpoAsyncJmsConfig;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.messaging.PdpoLogMessage;

class PdplMessageBatcherTest {

    private static final int TRANSPORT_LIMIT = 262144;
    private static final int HEADER_BUDGET = 65536;
    private final ObjectMapper mapper = new ObjectMapperConfiguration().snakeCaseObjectMapper();

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1})
    void respectsPayloadBoundaryIncludingEnvelopeAndHeaders(int spareBytes) throws Exception {
        PersonalDataProcessingLogDetails original = details(participants(8));
        int limit = wireSize(original) + HEADER_BUDGET + spareBytes;

        List<PersonalDataProcessingLogDetails> batches =
            new PdplMessageBatcher(mapper, limit, HEADER_BUDGET, "PDPO").split(original);

        assertThat(batches.size()).isEqualTo(spareBytes < 0 ? 2 : 1);
        assertBatches(original, batches, limit);
    }

    @Test
    void largePayloadPreservesEveryParticipantExactlyOnceAndAllMetadata() throws Exception {
        PersonalDataProcessingLogDetails original = details(participants(10000));
        assertThat(wireSize(original)).isGreaterThan(TRANSPORT_LIMIT);

        List<PersonalDataProcessingLogDetails> batches =
            new PdplMessageBatcher(mapper, TRANSPORT_LIMIT, HEADER_BUDGET, "PDPO").split(original);

        assertThat(batches).hasSizeGreaterThan(1);
        assertBatches(original, batches, TRANSPORT_LIMIT);
        assertThat(original.getIndividuals()).hasSize(10000);
    }

    @Test
    void countsUtf8BytesRatherThanCharacters() throws Exception {
        List<ParticipantIdentifier> individuals = IntStream.range(0, 8)
            .mapToObj(i -> participant("漢😀".repeat(12) + i, PdplIdentifierType.DRAFT_ACCOUNT)).toList();
        PersonalDataProcessingLogDetails original = details(individuals);
        int characterCount = mapper.writeValueAsString(new PdpoLogMessage("PDPO", original)).length();
        assertThat(wireSize(original)).isGreaterThan(characterCount);
        int limit = characterCount + HEADER_BUDGET;

        List<PersonalDataProcessingLogDetails> batches =
            new PdplMessageBatcher(mapper, limit, HEADER_BUDGET, "PDPO").split(original);

        assertThat(batches).hasSizeGreaterThan(1);
        assertBatches(original, batches, limit);
    }

    @Test
    void rejectsUnsplittableParticipant() {
        PersonalDataProcessingLogDetails original = details(List.of(
            participant("x".repeat(10000), PdplIdentifierType.DRAFT_ACCOUNT)));

        assertThatThrownBy(() -> new PdplMessageBatcher(mapper, 1024, 128, "PDPO").split(original))
            .isInstanceOf(PdplLoggingException.class).hasMessageContaining("transport limit");
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "1024, -1", "1024, 1024", "1024, 2048"})
    void rejectsInvalidTransportBudget(int limit, int overhead) {
        assertThatThrownBy(() -> new PdplMessageBatcher(mapper, limit, overhead, "PDPO"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private void assertBatches(PersonalDataProcessingLogDetails original,
        List<PersonalDataProcessingLogDetails> batches, int limit) throws Exception {

        assertThat(batches.stream().flatMap(batch -> batch.getIndividuals().stream()).toList())
            .containsExactlyElementsOf(original.getIndividuals());
        for (PersonalDataProcessingLogDetails batch : batches) {
            assertThat(batch).usingRecursiveComparison().ignoringFields("individuals").isEqualTo(original);
            assertThat(wireSize(batch) + HEADER_BUDGET).isLessThanOrEqualTo(limit);
        }
    }

    private int wireSize(PersonalDataProcessingLogDetails details) throws Exception {
        // Exercise the actual integration-library JMS converter, rather than reproducing the batching calculation.
        Session session = mock(Session.class);
        when(session.createTextMessage(anyString())).thenReturn(mock(TextMessage.class));
        new PdpoAsyncJmsConfig().pdpoMessageConverter(mapper)
            .toMessage(new PdpoLogMessage("PDPO", details), session);
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(session).createTextMessage(payload.capture());
        return payload.getValue().getBytes(StandardCharsets.UTF_8).length;
    }

    private List<ParticipantIdentifier> participants(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> participant(Long.toString(99000000000000L + i), PdplIdentifierType.DRAFT_ACCOUNT)).toList();
    }

    private PersonalDataProcessingLogDetails details(List<ParticipantIdentifier> individuals) {
        return PersonalDataProcessingLogDetails.builder()
            .createdBy(participant("500000000", PdplIdentifierType.OPAL_USER_ID))
            .businessIdentifier("Get Draft Account - Defendant")
            .createdAt(OffsetDateTime.parse("2026-11-17T10:00:00Z"))
            .ipAddress("192.0.2.1")
            .category(PersonalDataProcessingCategory.CONSULTATION)
            .recipient(participant("recipient", PdplIdentifierType.EXTERNAL_SYSTEM))
            .individuals(individuals)
            .build();
    }

    private ParticipantIdentifier participant(String id, PdplIdentifierType type) {
        return ParticipantIdentifier.builder().identifier(id).type(type).build();
    }
}
