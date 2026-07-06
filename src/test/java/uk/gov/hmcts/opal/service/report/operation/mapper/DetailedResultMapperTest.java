package uk.gov.hmcts.opal.service.report.operation.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.mockito.ArgumentMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.report.operation.DetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedOperationReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;
import uk.gov.hmcts.opal.service.report.operation.OperationDetailedReport;

class DetailedResultMapperTest {

    private static final LocalDate PAYMENT_TERMS_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate ENFORCEMENT_DATE = LocalDate.of(2024, 1, 2);
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2024, 1, 3);
    private static final LocalDate NOTE_DATE = LocalDate.of(2024, 1, 4);
    private static final LocalDate TRANSACTION_TWO_DATE = LocalDate.of(2024, 1, 5);

    private DetailedResultMapper mapper;
    private DetailedRowDtoCoreMapper rowMapper;
    private DetailedTransactionRowMapper transactionRowMapper;
    private DefendantTransactionRepository transactionRepository;
    private ImpositionRepository impositionRepository;
    private PaymentTermsRepository paymentTermsRepository;
    private EnforcementRepository enforcementRepository;
    private NoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        mapper = new DetailedResultMapper() {
        };
        rowMapper = mock(DetailedRowDtoCoreMapper.class);
        transactionRowMapper = mock(DetailedTransactionRowMapper.class);
        transactionRepository = mock(DefendantTransactionRepository.class);
        impositionRepository = mock(ImpositionRepository.class);
        paymentTermsRepository = mock(PaymentTermsRepository.class);
        enforcementRepository = mock(EnforcementRepository.class);
        noteRepository = mock(NoteRepository.class);
        mapper.setRowMapper(
            rowMapper,
            transactionRowMapper,
            transactionRepository,
            impositionRepository,
            paymentTermsRepository,
            enforcementRepository,
            noteRepository
        );
    }

    @Test
    void map_shouldBuildReportWithAllTransactionRowSourcesAndMetadata() {
        DefendantAccountEntity account1 = mock(DefendantAccountEntity.class);
        DefendantAccountEntity account2 = mock(DefendantAccountEntity.class);
        when(account1.getDefendantAccountId()).thenReturn(101L);
        when(account2.getDefendantAccountId()).thenReturn(202L);

        DetailedOperationReportAccountRowDto accountRow1 =
            mock(DetailedOperationReportAccountRowDto.class);
        DetailedOperationReportAccountRowDto accountRow2 =
            mock(DetailedOperationReportAccountRowDto.class);

        when(rowMapper.map(eq(account1), any(ReportMetadataContext.class))).thenReturn(accountRow1);
        when(rowMapper.map(eq(account2), any(ReportMetadataContext.class))).thenReturn(accountRow2);

        PaymentTermsEntity paymentTerms1 = mock(PaymentTermsEntity.class);
        EnforcementEntity enforcement1 = mock(EnforcementEntity.class);
        DefendantTransactionEntity transaction1 = mock(DefendantTransactionEntity.class);
        DefendantTransactionEntity transaction2 = mock(DefendantTransactionEntity.class);
        NoteEntity note1 = mock(NoteEntity.class);
        ImpositionEntity imposition1 = mock(ImpositionEntity.class);
        when(imposition1.getImpositionId()).thenReturn(11L);

        when(paymentTermsRepository
            .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(101L))
            .thenReturn(List.of(paymentTerms1));
        when(paymentTermsRepository
            .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(202L))
            .thenReturn(List.of());

        when(enforcementRepository.findHistoryRowsByDefendantAccountId(101L)).thenReturn(List.of(enforcement1));
        when(enforcementRepository.findHistoryRowsByDefendantAccountId(202L)).thenReturn(List.of());

        when(transactionRepository.findByDefendantAccountId(101L)).thenReturn(List.of(transaction1, transaction2));
        when(transactionRepository.findByDefendantAccountId(202L)).thenReturn(List.of());

        when(transaction1.getAssociatedRecordType()).thenReturn(AssociatedRecordType.IMPOSITIONS);
        when(transaction1.getAssociatedRecordId()).thenReturn("11");
        when(transaction2.getAssociatedRecordType()).thenReturn(AssociatedRecordType.DEFENDANT_ACCOUNTS);
        when(transaction2.getAssociatedRecordId()).thenReturn("22");
        when(impositionRepository.findAllById(List.of(11L))).thenReturn(List.of(imposition1));

        when(noteRepository.findAll(ArgumentMatchers.<Specification<NoteEntity>>any()))
            .thenReturn(List.of(note1))
            .thenReturn(List.of());

        DetailedReportTransactionRowDto paymentTermsRow =
            DetailedReportTransactionRowDto.builder()
                .transactionDate(PAYMENT_TERMS_DATE)
                .transactionType("TTPAY")
                .build();
        DetailedReportTransactionRowDto enforcementRow =
            DetailedReportTransactionRowDto.builder()
                .transactionDate(ENFORCEMENT_DATE)
                .transactionType("ENFT")
                .build();
        DetailedReportTransactionRowDto transactionRow1 =
            DetailedReportTransactionRowDto.builder()
                .transactionDate(TRANSACTION_DATE)
                .transactionType("TRANSACTION-1")
                .build();
        DetailedReportTransactionRowDto noteRow =
            DetailedReportTransactionRowDto.builder()
                .transactionDate(NOTE_DATE)
                .transactionType("NOTE")
                .build();
        DetailedReportTransactionRowDto transactionRow2 =
            DetailedReportTransactionRowDto.builder()
                .transactionDate(TRANSACTION_TWO_DATE)
                .transactionType("TRANSACTION-2")
                .build();

        when(transactionRowMapper
            .mapFromPaymentTerms(eq(paymentTerms1), eq(account1), any(ReportMetadataContext.class)))
            .thenReturn(paymentTermsRow);
        when(transactionRowMapper.mapFromEnforcement(eq(enforcement1), eq(account1), any(ReportMetadataContext.class)))
            .thenReturn(enforcementRow);
        when(transactionRowMapper.mapFromTransaction(
            eq(transaction1), eq(account1), eq(imposition1), any(ReportMetadataContext.class)))
            .thenReturn(transactionRow1);
        when(transactionRowMapper.mapFromTransaction(
            eq(transaction2), eq(account1), isNull(), any(ReportMetadataContext.class)))
            .thenReturn(transactionRow2);
        when(transactionRowMapper.mapFromNote(eq(note1), eq(account1), any(ReportMetadataContext.class)))
            .thenReturn(noteRow);

        OperationDetailedReport result = mapper.map(List.of(account1, account2));

        assertThat(result).isNotNull();
        assertThat(result.getDetailedReport()).isNotNull();
        assertThat(result.getReportMetaData()).isNotNull();
        assertThat(result.getReportMetaData().getPdpoPartyIds()).isEmpty();

        DetailedReportDto reportDto = result.getDetailedReport();
        assertThat(reportDto.getAccountTransactionReports()).hasSize(2);

        DetailedAccountReportDto mappedAccount1 = reportDto.getAccountTransactionReports().get(0);
        DetailedAccountReportDto mappedAccount2 = reportDto.getAccountTransactionReports().get(1);

        assertThat(mappedAccount1.getAccountRow()).isSameAs(accountRow1);
        assertThat(mappedAccount1.getTransactionRows()).containsExactly(
            paymentTermsRow,
            enforcementRow,
            transactionRow1,
            noteRow,
            transactionRow2
        );

        assertThat(mappedAccount2.getAccountRow()).isSameAs(accountRow2);
        assertThat(mappedAccount2.getTransactionRows()).isEmpty();

        verify(rowMapper).map(eq(account1), any(ReportMetadataContext.class));
        verify(rowMapper).map(eq(account2), any(ReportMetadataContext.class));
        verify(paymentTermsRepository)
            .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(101L);
        verify(paymentTermsRepository)
            .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(202L);
        verify(enforcementRepository).findHistoryRowsByDefendantAccountId(101L);
        verify(enforcementRepository).findHistoryRowsByDefendantAccountId(202L);
        verify(transactionRepository).findByDefendantAccountId(101L);
        verify(transactionRepository).findByDefendantAccountId(202L);
        verify(impositionRepository).findAllById(List.of(11L));
        verify(noteRepository, times(2))
            .findAll(ArgumentMatchers.<Specification<NoteEntity>>any());
    }

    @Test
    void map_shouldReturnEmptyAccountListWhenNoAccountsProvided() {
        OperationDetailedReport result = mapper.map(List.of());

        assertThat(result).isNotNull();
        assertThat(result.getDetailedReport()).isNotNull();
        assertThat(result.getDetailedReport().getAccountTransactionReports()).isEmpty();
        assertThat(result.getReportMetaData()).isNotNull();
        assertThat(result.getReportMetaData().getPdpoPartyIds()).isEmpty();
    }
}
