package uk.gov.hmcts.opal.mapper.report.operationbyenforcement;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.service.opal.history.defendant.DefendantTransactionDetailsService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper.DetailedTransactionRowMapper;

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
    void map_allFieldsIncludingConsolidatedAccountNo() {
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

        OperationByEnforcementDetailedReportTransactionRowDto result =
            mapper.map(transaction, account, imposition, new ReportMetadataContext());

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
    void map_transactionTypeNotConsol_returnNull(DefendantTransactionType transactionType) {
        DefendantTransactionEntity entity = new DefendantTransactionEntity();
        entity.setTransactionType(transactionType);
        entity.setAssociatedRecordType(AssociatedRecordType.DEFENDANT_ACCOUNTS);
        entity.setAssociatedRecordId("12");

        OperationByEnforcementDetailedReportTransactionRowDto result =
            mapper.map(entity, account, imposition, new ReportMetadataContext());

        assertThat(result.getConsolidatedAccountNo()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = AssociatedRecordType.class, names = {"DEFENDANT_ACCOUNTS"}, mode = Mode.EXCLUDE)
    void map_associatedRecordTypeNotDefendantAccounts_returnNull() {
        DefendantTransactionEntity entity = new DefendantTransactionEntity();
        entity.setTransactionType(DefendantTransactionType.CONSOL);
        entity.setAssociatedRecordType(AssociatedRecordType.IMPOSITIONS);
        entity.setAssociatedRecordId("12");

        OperationByEnforcementDetailedReportTransactionRowDto result =
            mapper.map(entity, account, imposition, new ReportMetadataContext());

        assertThat(result.getConsolidatedAccountNo()).isNull();
    }

}