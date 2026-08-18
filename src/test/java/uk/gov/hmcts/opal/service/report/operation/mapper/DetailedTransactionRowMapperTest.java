package uk.gov.hmcts.opal.service.report.operation.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;
import uk.gov.hmcts.opal.service.opal.history.defendant.DefendantTransactionDetailsService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;

@ExtendWith(MockitoExtension.class)
class DetailedTransactionRowMapperTest {

    @Mock
    private DefendantAccountRepositoryService defendantAccountService;

    @Mock
    private DefendantTransactionDetailsService defendantTransactionDetailsService;

    private DetailedTransactionRowMapper mapper;

    private DefendantAccountEntity account;

    @Mock
    ImpositionEntity imposition;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(
            DetailedTransactionRowMapper.class);
        ReflectionTestUtils.setField(mapper, "defendantAccountService", defendantAccountService);
        ReflectionTestUtils.setField(mapper, "defendantTransactionDetailsService", defendantTransactionDetailsService);
        account = new DefendantAccountEntity();
        account.setAccountNumber("ACCOUNT");
    }

    @Test
    void mapFromTransaction_allFieldsIncludingConsolidatedAccountNo() {
        DefendantTransactionEntity transaction = new DefendantTransactionEntity();
        transaction.setPostedDate(LocalDate.of(2024, 1, 15));
        transaction.setTransactionType(DefendantTransactionType.CONSOL);
        transaction.setPostedByUsername("test.user");
        transaction.setTransactionAmount(new BigDecimal("125.50"));
        transaction.setAssociatedRecordType(AssociatedRecordType.DEFENDANT_ACCOUNTS);
        transaction.setAssociatedRecordId("12");
        DefendantAccountEntity consolidated = DefendantAccountEntity.builder().accountNumber("CONSOLIDATED").build();
        when(defendantAccountService.findById(12)).thenReturn(consolidated);
        when(defendantTransactionDetailsService.generateTransactionDetails(transaction, account, imposition))
            .thenReturn("txn details");

        DetailedReportTransactionRowDto result =
            mapper.mapFromTransaction(transaction, account, imposition, new ReportMetadataContext());

        assertThat(result).isNotNull();
        assertThat(result.getAccountNo()).isEqualTo("ACCOUNT");
        assertThat(result.getTransactionDate())
            .isEqualTo(transaction.getPostedDate());
        assertThat(result.getTransactionType())
            .isEqualTo(transaction.getTransactionType().getLabel());
        assertThat(result.getTransactionUserId())
            .isEqualTo("test.user");
        assertThat(result.getTransactionAmount())
            .isEqualByComparingTo("125.50");
        assertThat(result.getConsolidatedAccountNo())
            .isEqualTo("CONSOLIDATED");
        assertThat(result.getTransactionDetails())
            .isEqualTo("txn details");
    }

    @ParameterizedTest
    @EnumSource(value = DefendantTransactionType.class, names = {"CONSOL"}, mode = Mode.EXCLUDE)
    void mapFromTransaction_transactionTypeNotConsol_returnNull(DefendantTransactionType transactionType) {
        DefendantTransactionEntity entity = new DefendantTransactionEntity();
        entity.setTransactionType(transactionType);
        entity.setAssociatedRecordType(AssociatedRecordType.DEFENDANT_ACCOUNTS);
        entity.setAssociatedRecordId("12");

        DetailedReportTransactionRowDto result =
            mapper.mapFromTransaction(entity, account, imposition, new ReportMetadataContext());

        assertThat(result.getConsolidatedAccountNo()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = AssociatedRecordType.class, names = {"DEFENDANT_ACCOUNTS"}, mode = Mode.EXCLUDE)
    void mapFromTransaction_associatedRecordTypeNotDefendantAccounts_returnNull() {
        DefendantTransactionEntity entity = new DefendantTransactionEntity();
        entity.setTransactionType(DefendantTransactionType.CONSOL);
        entity.setAssociatedRecordType(AssociatedRecordType.IMPOSITIONS);
        entity.setAssociatedRecordId("12");

        DetailedReportTransactionRowDto result =
            mapper.mapFromTransaction(entity, account, imposition, new ReportMetadataContext());

        assertThat(result.getConsolidatedAccountNo()).isNull();
    }

    @Test
    void mapFromPaymentTerms_byDate_shouldMapCoreFieldsAndDetails() {
        PaymentTermsEntity paymentTerms = new PaymentTermsEntity();
        paymentTerms.setPostedDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        paymentTerms.setPostedByUsername("payment.user");
        paymentTerms.setTermsTypeCode(TermsTypeCode.BY_DATE);
        paymentTerms.setEffectiveDate(LocalDate.of(2024, 2, 20));
        paymentTerms.setJailDays(7);
        paymentTerms.setReasonForExtension("extended by court");

        DetailedReportTransactionRowDto result =
            mapper.mapFromPaymentTerms(paymentTerms, account, new ReportMetadataContext());

        assertThat(result).isNotNull();
        assertThat(result.getAccountNo()).isEqualTo("ACCOUNT");
        assertThat(result.getTransactionDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(result.getTransactionType()).isEqualTo(DetailedTransactionRowMapper.PAYMENT_TERMS_TYPE);
        assertThat(result.getTransactionUserId()).isEqualTo("payment.user");
        assertThat(result.getTransactionAmount()).isNull();
        assertThat(result.getConsolidatedAccountNo()).isNull();
        assertThat(result.getTransactionDetails())
            .isEqualTo("In full | 2024-02-20 | 7 days in default | extended by court");
    }

    @Test
    void mapFromPaymentTerms_instalments_shouldMapInstallmentDetails() {
        PaymentTermsEntity paymentTerms = new PaymentTermsEntity();
        paymentTerms.setPostedDate(LocalDateTime.of(2024, 3, 1, 8, 45));
        paymentTerms.setPostedByUsername("instalment.user");
        paymentTerms.setTermsTypeCode(TermsTypeCode.INSTALMENTS);
        paymentTerms.setEffectiveDate(LocalDate.of(2024, 4, 10));
        paymentTerms.setInstalmentLumpSum(new BigDecimal("150.00"));
        paymentTerms.setInstalmentAmount(new BigDecimal("25.00"));
        paymentTerms.setInstalmentPeriod(InstalmentPeriod.MONTH);

        DetailedReportTransactionRowDto result =
            mapper.mapFromPaymentTerms(paymentTerms, account, new ReportMetadataContext());

        assertThat(result).isNotNull();
        assertThat(result.getAccountNo()).isEqualTo("ACCOUNT");
        assertThat(result.getTransactionDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(result.getTransactionType()).isEqualTo(DetailedTransactionRowMapper.PAYMENT_TERMS_TYPE);
        assertThat(result.getTransactionUserId()).isEqualTo("instalment.user");
        assertThat(result.getTransactionAmount()).isNull();
        assertThat(result.getConsolidatedAccountNo()).isNull();
        assertThat(result.getTransactionDetails())
            .isEqualTo("Lump sum: 150.00 | Installments: 25.00 monthly from 2024-04-10");
    }

    @Test
    void mapFromEnforcement_shouldMapAllFieldsAndDetails() {
        EnforcementEntity enforcement = new EnforcementEntity();
        enforcement.setPostedDate(LocalDateTime.of(2024, 5, 6, 7, 8));
        enforcement.setPostedByUsername("enforcement.user");
        enforcement.setResultId("RESULT-1");
        enforcement.setJailDays(10);
        enforcement.setWarrantReference("WR-123");
        enforcement.setEarliestReleaseDate(LocalDateTime.of(2024, 5, 7, 8, 9));
        enforcement.setHearingDate(LocalDateTime.of(2024, 5, 8, 9, 10));
        CourtEntity court = new CourtEntity();
        court.setName("Magistrates Court");
        enforcement.setHearingCourt(court);
        enforcement.setCaseReference("CASE-9");
        enforcement.setReason("reason text");

        DetailedReportTransactionRowDto result =
            mapper.mapFromEnforcement(enforcement, account, new ReportMetadataContext());

        assertThat(result).isNotNull();
        assertThat(result.getAccountNo()).isEqualTo("ACCOUNT");
        assertThat(result.getTransactionDate()).isEqualTo(LocalDate.of(2024, 5, 6));
        assertThat(result.getTransactionType()).isEqualTo(DetailedTransactionRowMapper.ENFORCEMENT_TYPE);
        assertThat(result.getTransactionUserId()).isEqualTo("enforcement.user");
        assertThat(result.getTransactionAmount()).isNull();
        assertThat(result.getConsolidatedAccountNo()).isNull();
        assertThat(result.getTransactionDetails()).isEqualTo(
            "RESULT-1 | 10 days in default | Warrant number: WR-123"
                + " | Earliest date of release: 2024-05-07T08:09"
                + " | Hearing: 2024-05-08T09:10 - Magistrates Court - Case: CASE-9"
                + " | reason text");
    }

    @Test
    void mapFromEnforcement_nullResultId_shouldStillMapOtherDetails() {
        EnforcementEntity enforcement = new EnforcementEntity();
        enforcement.setPostedDate(LocalDateTime.of(2024, 7, 8, 9, 10));
        enforcement.setPostedByUsername("enforcement.user");
        enforcement.setResultId("RESULT_ID");
        enforcement.setReason("reason only");

        DetailedReportTransactionRowDto result =
            mapper.mapFromEnforcement(enforcement, account, new ReportMetadataContext());

        assertThat(result).isNotNull();
        assertThat(result.getTransactionDetails()).isEqualTo("RESULT_ID | reason only");
    }

    @Test
    void mapFromNote_shouldMapNoteFieldsAndConsolidatedAccountNo() {
        DefendantAccountEntity consolidatedAccount = DefendantAccountEntity.builder()
            .accountNumber("CONSOLIDATED-12")
            .build();
        when(defendantAccountService.findById(12)).thenReturn(consolidatedAccount);

        NoteEntity note = new NoteEntity();
        note.setPostedDate(LocalDateTime.of(2024, 6, 2, 13, 14));
        note.setPostedByUsername("note.user");
        note.setNoteText("note text");
        note.setAssociatedRecordType(AssociatedRecordType.DEFENDANT_ACCOUNTS);
        note.setAssociatedRecordId("12");

        ReportMetadataContext context = new ReportMetadataContext();
        DetailedReportTransactionRowDto result = mapper.mapFromNote(note, account, context);

        assertThat(result).isNotNull();
        assertThat(result.getAccountNo()).isEqualTo("ACCOUNT");
        assertThat(result.getTransactionDate()).isEqualTo(LocalDate.of(2024, 6, 2));
        assertThat(result.getTransactionType()).isEqualTo(DetailedTransactionRowMapper.NOTE_TYPE);
        assertThat(result.getTransactionUserId()).isEqualTo("note.user");
        assertThat(result.getTransactionAmount()).isNull();
        assertThat(result.getConsolidatedAccountNo()).isEqualTo("CONSOLIDATED-12");
        assertThat(result.getTransactionDetails()).isEqualTo("note text");
        assertThat(context.getParticipants())
            .containsExactly(new ParticipantIdentifier("12", PdplIdentifierType.CONSOLIDATED_ACCOUNT));
    }

}
