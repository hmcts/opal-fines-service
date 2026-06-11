package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartyAccountType;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Transactional
@DisplayName("Cash Till Report Generation Integration Tests")
class CashTillReportGenerationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CashTillReportService service;

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    @Autowired
    private TillRepository tillRepository;

    @Autowired
    private PaymentInRepository paymentInRepository;

    @Autowired
    private DefendantAccountRepository defendantAccountRepository;

    @Autowired
    private MiscellaneousAccountRepository miscellaneousAccountRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Test
    @JiraStory("PO-2636")
    @JiraEpic("PO-2248")
    void generateReportData_returnsAllTillPaymentsSortedAndMapped() {
        BusinessUnitEntity businessUnit = businessUnitRepository.findAll().stream().findFirst().orElseThrow();
        TillEntity till = tillRepository.saveAndFlush(TillEntity.builder()
            .businessUnit(businessUnit)
            .tillNumber((short) 17)
            .ownedBy("Jamie")
            .createdDate(LocalDateTime.of(2026, 5, 1, 8, 0))
            .build());
        DefendantAccountEntity defendantAccount = defendantAccountRepository.findAll().stream().findFirst()
            .orElseThrow();
        PartyEntity party = partyRepository.saveAndFlush(PartyEntity.builder()
            .accountType(PartyAccountType.DEFENDANT)
            .surname("Misc")
            .forenames("Holder")
            .build());
        MiscellaneousAccountEntity miscellaneousAccount = miscellaneousAccountRepository.saveAndFlush(
            MiscellaneousAccountEntity.builder()
                .businessUnit(businessUnit)
                .accountNumber("MISC-7001")
                .party(party)
                .build());
        paymentInRepository.saveAndFlush(PaymentInEntity.builder()
            .tillEntity(till)
            .paymentAmount(new BigDecimal("8.40"))
            .paymentDate(LocalDateTime.of(2026, 5, 2, 9, 5))
            .paymentMethod("CQ")
            .destinationType("S")
            .associatedRecordType(AssociatedRecordType.MISCELLANEOUS_ACCOUNTS.getLabel())
            .associatedRecordId(String.valueOf(miscellaneousAccount.getMiscellaneousAccountId()))
            .receipt(false)
            .autoPayment(true)
            .allocated(false)
            .build());
        paymentInRepository.saveAndFlush(PaymentInEntity.builder()
            .tillEntity(till)
            .paymentAmount(new BigDecimal("12.30"))
            .paymentDate(LocalDateTime.of(2026, 5, 3, 10, 15))
            .paymentMethod("NC")
            .destinationType("F")
            .associatedRecordType(AssociatedRecordType.DEFENDANT_ACCOUNTS.getLabel())
            .associatedRecordId(String.valueOf(defendantAccount.getDefendantAccountId()))
            .receipt(true)
            .autoPayment(false)
            .allocated(true)
            .build());

        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReportParameters("""
            {"till_id":%d,"allocated_report":true}
            """.formatted(till.getTillId()));

        CashTillReportData reportData = service.generateReportData(reportInstance);

        assertThat(reportData.getAllocatedReport()).isTrue();
        assertThat(reportData.getRows()).hasSize(2);
        assertThat(reportData.getRows()).extracting(
            CashTillReportRow::getCashTillNumber,
            CashTillReportRow::getCashier,
            CashTillReportRow::getDestinationType,
            CashTillReportRow::getDetails,
            CashTillReportRow::getPaymentMethod,
            CashTillReportRow::getAmount,
            CashTillReportRow::getReceipt,
            CashTillReportRow::getAllocated
        ).containsExactly(
            Tuple.tuple("17", "Jamie", CashTillDestinationType.FA, defendantAccount.getAccountNumber(),
                CashTillPaymentMethod.NC, new BigDecimal("12.30"), true, true),
            Tuple.tuple("17", "Jamie", CashTillDestinationType.SA, "MISC-7001",
                CashTillPaymentMethod.CQ, new BigDecimal("8.40"), false, false));
        assertThat(reportData.getRows()).extracting(CashTillReportRow::getPaymentDateTime).containsExactly(
            LocalDateTime.of(2026, 5, 3, 10, 15),
            LocalDateTime.of(2026, 5, 2, 9, 5));
        assertThat(reportData.getReportMetaData().getPdpoPartyIds()).singleElement().satisfies(identifier -> {
            assertThat(identifier.getIdentifier()).isEqualTo(String.valueOf(defendantAccount.getDefendantAccountId()));
            assertThat(identifier.getType()).isEqualTo(PdplIdentifierType.DEFENDANT_ACCOUNT);
        });
    }
}
