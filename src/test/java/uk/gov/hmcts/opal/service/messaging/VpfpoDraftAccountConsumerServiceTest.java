package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountMapping;
import uk.gov.hmcts.opal.entity.draft.DraftAccountSource;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.DraftAccountService;

@ExtendWith(MockitoExtension.class)
class VpfpoDraftAccountConsumerServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DraftAccountService draftAccountService;

    @Mock
    private LocalJusticeAreaRepository localJusticeAreaRepository;

    private VpfpoDraftAccountConsumerService consumer;

    @BeforeEach
    void setUp() {
        consumer = new VpfpoDraftAccountConsumerService(objectMapper, draftAccountService, localJusticeAreaRepository);
    }

    @Test
    void consume_mapsBusinessUnitAndEnforcingCourt() {
        String payload = """
            {
              "draft_account_id": 987,
              "created_at": "2026-11-12T10:00:00Z",
              "validated_at": "2026-11-12T11:00:00Z",
              "business_unit_id": 1013,
              "account_type": "Fixed Penalty",
              "account_status": "Submitted",
              "status_message": "Extracted from VPFPO",
              "timeline_data": {"entries": []},
              "source": "VPFPO",
              "source_reference": 123456,
              "account": {
                "enforcement_court_id": 1,
                "prosecutor_case_reference": "TICKET-123",
                "defendant": {"surname": "Example"}
              },
              "account_snapshot": {"defendant_name": "Example"}
            }
            """;
        when(localJusticeAreaRepository.findDraftAccountMappingByLjaCode((short) 1013))
            .thenReturn(Optional.of(new DraftAccountMapping((short) 78, 260000000048L)));

        consumer.consume(payload);

        ArgumentCaptor<AddDraftAccountRequestDto> captor = ArgumentCaptor.forClass(AddDraftAccountRequestDto.class);
        verify(draftAccountService).submitDraftAccount(captor.capture());
        AddDraftAccountRequestDto request = captor.getValue();
        assertThat(request.getBusinessUnitId()).isEqualTo((short) 78);
        assertThat(objectMapper.readTree(request.getAccount()).get("enforcement_court_id").longValue())
            .isEqualTo(260000000048L);
        assertThat(objectMapper.readTree(request.getAccount()).get("prosecutor_case_reference").asString())
            .isEqualTo("TICKET-123");
        assertThat(objectMapper.readTree(request.getAccount()).get("defendant").get("surname").asString())
            .isEqualTo("Example");
        assertThat(objectMapper.readTree(request.getAccountSnapshot()).get("defendant_name").asString())
            .isEqualTo("Example");
        assertThat(request.getSource()).isEqualTo(DraftAccountSource.VPFPO);
        assertThat(request.getSourceReference()).isEqualTo(123456L);
        assertThat(request.getAccountType()).isEqualTo(DraftAccountType.FIXED_PENALTY);
        assertThat(request.getDraftAccountId()).isEqualTo(987L);
        assertThat(request.getCreatedDate()).isEqualTo(OffsetDateTime.parse("2026-11-12T10:00:00Z"));
        assertThat(request.getValidatedDate()).isEqualTo(OffsetDateTime.parse("2026-11-12T11:00:00Z"));
        assertThat(request.getAccountStatus()).isEqualTo(DraftAccountStatus.SUBMITTED);
        assertThat(request.getStatusMessage()).isEqualTo("Extracted from VPFPO");
        JsonNode timeline = objectMapper.valueToTree(request.getTimelineData());
        assertThat(timeline).isEqualTo(objectMapper.readTree("{\"entries\":[]}"));
    }

    @Test
    void consume_addsMissingEnforcingCourt() {
        String payload = """
            {"business_unit_id":1013,"account_type":"Fixed Penalty","account":{"offences":[]}}
            """;
        when(localJusticeAreaRepository.findDraftAccountMappingByLjaCode((short) 1013))
            .thenReturn(Optional.of(new DraftAccountMapping((short) 78, 260000000048L)));

        consumer.consume(payload);

        ArgumentCaptor<AddDraftAccountRequestDto> captor = ArgumentCaptor.forClass(AddDraftAccountRequestDto.class);
        verify(draftAccountService).submitDraftAccount(captor.capture());
        assertThat(captor.getValue().getAccountSnapshot()).isNull();
        assertThat(objectMapper.readTree(captor.getValue().getAccount()).get("enforcement_court_id").longValue())
            .isEqualTo(260000000048L);
    }

    @Test
    void consume_discardsMissingMapping() {
        when(localJusticeAreaRepository.findDraftAccountMappingByLjaCode((short) 1013)).thenReturn(Optional.empty());

        consumer.consume("""
            {"business_unit_id":1013,"account_type":"Fixed Penalty","account":{}}
            """);

        verifyNoInteractions(draftAccountService);
    }

    @Test
    void consume_propagatesDatabaseFailure() {
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("Database unavailable");
        when(localJusticeAreaRepository.findDraftAccountMappingByLjaCode((short) 1013)).thenThrow(failure);

        assertThatThrownBy(() -> consumer.consume("""
            {"business_unit_id":1013,"account_type":"Fixed Penalty","account":{}}
            """))
            .isSameAs(failure);
        verifyNoInteractions(draftAccountService);
    }
}
