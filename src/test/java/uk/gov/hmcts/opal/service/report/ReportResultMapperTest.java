package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;

class ReportResultMapperTest {

    @Mock
    private ReportRowDtoMapper rowMapper;

    @InjectMocks
    private ReportResultMapper mapper = new ReportResultMapper() {
    };

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper.setRowMapper(rowMapper);
    }

    @Test
    void shouldMapAccountsToRowsAndPopulateMetadata() {
        // Arrange
        DefendantAccountEntity acc1 = new DefendantAccountEntity();
        acc1.setAccountNumber("A1");
        DefendantAccountEntity acc2 = new DefendantAccountEntity();
        acc2.setAccountNumber("A2");
        EnforcementReportRowDto dto1 = new EnforcementReportRowDto();
        EnforcementReportRowDto dto2 = new EnforcementReportRowDto();

        doAnswer(invocation -> {
            DefendantAccountEntity acc = invocation.getArgument(0);
            ReportMetadataContext ctx = invocation.getArgument(1);
            ctx.addParticipant(acc.getAccountNumber(), PdplIdentifierType.DEFENDANT_ACCOUNT);
            return dto1;
        }).when(rowMapper).map(eq(acc1), any());

        doAnswer(invocation -> {
            DefendantAccountEntity acc = invocation.getArgument(0);
            ReportMetadataContext ctx = invocation.getArgument(1);
            ctx.addParticipant(acc.getAccountNumber(), PdplIdentifierType.DEFENDANT_ACCOUNT);
            return dto2;
        }).when(rowMapper).map(eq(acc2), any());
        List<DefendantAccountEntity> accounts = List.of(acc1, acc2);
        // Act
        OperationReportByEnforcementTransaction result = mapper.map(accounts);
        // Assert
        assertThat(result).isNotNull();
        Assertions.assertThat(result.getTransactionList())
            .containsExactly(dto1, dto2);
        Assertions.assertThat(result.getReportMetaData().getPdpoPartyIds())
            .extracting(ParticipantIdentifier::getIdentifier)
            .containsExactlyInAnyOrder("A1", "A2");
        verify(rowMapper).map(eq(acc1), any());
        verify(rowMapper).map(eq(acc2), any());
    }

    @Test
    void shouldHandleEmptyAccountList() {
        // Act
        OperationReportByEnforcementTransaction result = mapper.map(List.of());
        // Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getTransactionList()).isEmpty();
        Assertions.assertThat(result.getReportMetaData()).isNotNull();
        Assertions.assertThat(result.getReportMetaData().getPdpoPartyIds()).isEmpty();
    }

    @Test
    void shouldHandleNullParticipantsFromContextGracefully() {
        // Arrange
        DefendantAccountEntity acc = new DefendantAccountEntity();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(rowMapper.map(eq(acc), any())).thenReturn(dto);
        // Act
        OperationReportByEnforcementTransaction result =
            mapper.map(List.of(acc));
        // Assert
        Assertions.assertThat(result.getTransactionList()).containsExactly(dto);
        Assertions.assertThat(result.getReportMetaData().getPdpoPartyIds()).isEmpty();
    }
}
