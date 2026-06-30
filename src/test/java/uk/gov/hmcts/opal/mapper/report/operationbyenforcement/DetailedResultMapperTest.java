package uk.gov.hmcts.opal.mapper.report.operationbyenforcement;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.OperationByEnforcementDetailedReport;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper.DetailedResultMapper;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper.DetailedRowDtoCoreMapper;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper.DetailedTransactionRowMapper;

class DetailedResultMapperTest {

    private DetailedResultMapper mapper;
    private DetailedRowDtoCoreMapper rowMapper;
    private DetailedTransactionRowMapper transactionRowMapper;
    private DefendantTransactionRepository transactionRepository;
    private ImpositionRepository impositionRepository;

    @BeforeEach
    void setUp() {
        mapper = new DetailedResultMapper() {
        };
        rowMapper = mock(DetailedRowDtoCoreMapper.class);
        transactionRowMapper = mock(DetailedTransactionRowMapper.class);
        transactionRepository = mock(DefendantTransactionRepository.class);
        impositionRepository = mock(ImpositionRepository.class);
        mapper.setRowMapper(rowMapper, transactionRowMapper, transactionRepository, impositionRepository);
    }

    @Test
    void map_shouldBuildReportWithAccountTransactionRowsAndMetadata() {
        DefendantAccountEntity account1 = mock(DefendantAccountEntity.class);
        DefendantAccountEntity account2 = mock(DefendantAccountEntity.class);
        when(account1.getDefendantAccountId()).thenReturn(101L);
        when(account2.getDefendantAccountId()).thenReturn(202L);

        OperationByEnforcementDetailedReportAccountRowDto accountRow1 =
            mock(OperationByEnforcementDetailedReportAccountRowDto.class);
        OperationByEnforcementDetailedReportAccountRowDto accountRow2 =
            mock(OperationByEnforcementDetailedReportAccountRowDto.class);

        when(rowMapper.map(eq(account1), any(ReportMetadataContext.class))).thenReturn(accountRow1);
        when(rowMapper.map(eq(account2), any(ReportMetadataContext.class))).thenReturn(accountRow2);

        DefendantTransactionEntity transaction1 = mock(DefendantTransactionEntity.class);
        DefendantTransactionEntity transaction2 = mock(DefendantTransactionEntity.class);
        DefendantTransactionEntity transaction3 = mock(DefendantTransactionEntity.class);

        ImpositionEntity imposition1 = mock(ImpositionEntity.class);
        when(imposition1.getImpositionId()).thenReturn(11L);
        ImpositionEntity imposition2 = mock(ImpositionEntity.class);
        when(imposition2.getImpositionId()).thenReturn(12L);

        when(transactionRepository.findByDefendantAccountId(101L)).thenReturn(List.of(transaction1, transaction2));
        when(transactionRepository.findByDefendantAccountId(202L)).thenReturn(List.of(transaction3));

        when(transaction1.getAssociatedRecordType()).thenReturn(AssociatedRecordType.IMPOSITIONS);
        when(transaction1.getAssociatedRecordId()).thenReturn("11");

        when(transaction3.getAssociatedRecordType()).thenReturn(AssociatedRecordType.IMPOSITIONS);
        when(transaction3.getAssociatedRecordId()).thenReturn("12");

        when(impositionRepository.findAllById(any())).thenReturn(List.of(imposition1, imposition2));


        OperationByEnforcementDetailedReportTransactionRowDto transactionRow1 =
            mock(OperationByEnforcementDetailedReportTransactionRowDto.class);
        OperationByEnforcementDetailedReportTransactionRowDto transactionRow2 =
            mock(OperationByEnforcementDetailedReportTransactionRowDto.class);
        OperationByEnforcementDetailedReportTransactionRowDto transactionRow3 =
            mock(OperationByEnforcementDetailedReportTransactionRowDto.class);

        when(transactionRowMapper
            .map(eq(transaction1), eq(account1), eq(imposition1), any(ReportMetadataContext.class)))
            .thenReturn(transactionRow1);
        when(transactionRowMapper.map(eq(transaction2), eq(account1), isNull(), any(ReportMetadataContext.class)))
            .thenReturn(transactionRow2);
        when(transactionRowMapper.map(eq(transaction3), eq(account2), eq(imposition2),any(ReportMetadataContext.class)))
            .thenReturn(transactionRow3);

        OperationByEnforcementDetailedReport result = mapper.map(List.of(account1, account2));

        assertThat(result).isNotNull();
        assertThat(result.getEnforcementReport()).isNotNull();
        assertThat(result.getReportMetaData()).isNotNull();

        OperationByEnforcementDetailedReportDto reportDto = result.getEnforcementReport();
        Assertions.assertThat(reportDto.getAccountTransactionReports()).hasSize(2);

        OperationByEnforcementDetailedAccountReportDto mappedAccount1 =
            reportDto.getAccountTransactionReports().get(0);
        OperationByEnforcementDetailedAccountReportDto mappedAccount2 =
            reportDto.getAccountTransactionReports().get(1);

        assertThat(mappedAccount1.getAccountRow()).isSameAs(accountRow1);
        Assertions.assertThat(mappedAccount1.getTransactionRows()).containsExactly(transactionRow1, transactionRow2);

        assertThat(mappedAccount2.getAccountRow()).isSameAs(accountRow2);
        Assertions.assertThat(mappedAccount2.getTransactionRows()).containsExactly(transactionRow3);

        verify(rowMapper).map(eq(account1), any(ReportMetadataContext.class));
        verify(rowMapper).map(eq(account2), any(ReportMetadataContext.class));
    }

    @Test
    void map_shouldReturnEmptyAccountListWhenNoAccountsProvided() {
        OperationByEnforcementDetailedReport result = mapper.map(List.of());

        assertNotNull(result);
        OperationByEnforcementDetailedReportDto reportDto = result.getEnforcementReport();
        assertNotNull(reportDto);
        assertEquals(List.of(), reportDto.getAccountTransactionReports());
        assertNotNull(result.getReportMetaData());
        assertEquals(List.of(), result.getReportMetaData().getPdpoPartyIds());

    }
}
