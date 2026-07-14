package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.report.operation.DetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedOperationReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.service.report.operation.OperationDetailedReport;
import uk.gov.hmcts.opal.service.report.operation.OperationReportByPaymentService;
import uk.gov.hmcts.opal.util.AgeUtil;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_enforcements.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OperationReportByPaymentServiceTest")
@DisplayName("OperationReportByPaymentServiceDetailedTest")
public class OperationReportByPaymentServiceDetailedTest extends AbstractIntegrationTest {

    @Autowired
    private OperationReportByPaymentService service;
    @Autowired
    private DefendantAccountRepository defendantAccountRepository;
    @Autowired
    private PaymentTermsRepository paymentTermsRepository;
    @Autowired
    private DefendantTransactionRepository defendantTransactionRepository;

    // ----------------------------------------
    // Helper
    // ----------------------------------------

    private @NonNull Map<String, Long> getAccountNoToId() {
        return defendantAccountRepository.findAll().stream()
            .collect(Collectors.toMap(
                DefendantAccountEntity::getAccountNumber,
                DefendantAccountEntity::getDefendantAccountId
            ));
    }

    private static void verifyMetadata(OperationDetailedReport result) {
        ReportMetaData reportMetadata = result.getReportMetaData();
        long numberOfRecords = result.getNumberOfRecords();
        assertThat(numberOfRecords).isEqualTo(result.getDetailedReport().getAccountTransactionReports().size());
        assertThat((long) reportMetadata.getPdpoPartyIds().size()).isGreaterThanOrEqualTo(numberOfRecords);
        Assertions.assertThat(reportMetadata.getPdpoPartyIds()).doesNotHaveDuplicates();
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8822")
    void generateReportData_filterDetailedReportType_returnSortedResultsOfDetailedReportType() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "2026-06-01",
              "reportType": "DETAILED",
              "businessUnitIds": [77, 78]
            }
            """);
        //Act
        OperationDetailedReport result = (OperationDetailedReport) service.generateReportData(reportInstance);
        //Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();

        Assertions.assertThat(reports)
            .isSortedAccordingTo(
                Comparator.comparing(
                    report -> report.getAccountRow().getAccountNo()
                )
            );

        DetailedAccountReportDto report = reports.stream()
            .filter(r -> "177A".equals(r.getAccountRow().getAccountNo()))
            .findFirst()
            .orElseThrow();

        DetailedOperationReportAccountRowDto account = report.getAccountRow();
        assertAll("account row",
            () -> assertThat(account.getHeader1()).isEqualTo("ACCOUNT"),
            () -> assertThat(account.getCompany()).isEqualTo("N"),
            () -> assertThat(account.getDefendantName()).isEqualTo("Graham, Anna"),
            () -> assertThat(account.getAccountNo()).isEqualTo("177A"),
            () -> assertThat(account.getDateOfBirth()).isEqualTo(LocalDate.of(1980, 2, 3)),
            () -> assertThat(account.getAddress1()).isEqualTo("Lumber House"),
            () -> assertThat(account.getAddress2()).isEqualTo("77 Gordon Road"),
            () -> assertThat(account.getAddress3()).isEqualTo("Maidstone, Kent"),
            () -> assertThat(account.getPostcode()).isEqualTo("MA4 1AL"),
            () -> assertThat(account.getEmployeeRef()).isEqualTo("EMPREF77"),
            () -> assertThat(account.getEmployerName()).isEqualTo("Tesco Ltd"),
            () -> assertThat(account.getEmployerAddress1()).isEqualTo("123 Employer Road"),
            () -> assertThat(account.getEmployerAddress2()).isEqualTo("Employer Lane"),
            () -> assertThat(account.getEmployerAddress3()).isEqualTo("London Borough"),
            () -> assertThat(account.getEmployerAddress4()).isEqualTo("London"),
            () -> assertThat(account.getEmployerAddress5()).isEqualTo("England"),
            () -> assertThat(account.getEmployerPostcode()).isEqualTo("EMP1 2AA"),
            () -> assertThat(account.getEmployerTel()).isEqualTo("02079997777"),
            () -> assertThat(account.getEmployerEmail()).isEqualTo("employer77@company.com"),
            () -> assertThat(account.getCollectionOrder()).isEqualTo("Y"),
            () -> assertThat(account.getLastMovementDate()).isEqualTo(LocalDate.of(2024, 1, 2)),
            () -> assertThat(account.getDateOfHearing()).isEqualTo(LocalDate.of(2023, 11, 3)),
            () -> assertThat(account.getImposingCourt()).isEqualTo("AAA Test Court"),
            () -> assertThat(account.getPaymentTerms()).isEqualTo("12/10/2025"),
            () -> assertThat(account.getAmountImposed()).isEqualByComparingTo("700.58"),
            () -> assertThat(account.getBalance()).isEqualByComparingTo("-500.58"),
            () -> assertThat(account.getArrearsTotal()).isEqualByComparingTo("500.58"),
            () -> assertThat(account.getFineImpositions()).isEqualByComparingTo("120.00"),
            () -> assertThat(account.getCostImpositions()).isEqualByComparingTo("100.00"),
            () -> assertThat(account.getCompensationImpositions()).isEqualByComparingTo("50.00"),
            () -> assertThat(account.getCriminalCourtsChargeImpositions()).isEqualByComparingTo("301.80"),
            () -> assertThat(account.getVictimSurchargeImpositions()).isEqualByComparingTo("150.00"),
            () -> assertThat(account.getOtherImpositions()).isEqualByComparingTo("404.40"),
            () -> assertThat(account.getProsecutorCaseReference()).isEqualTo("090A"),
            () -> assertThat(account.getParentOrGuardian()).isEqualTo("N")
        );

        Assertions.assertThat(report.getTransactionRows()).contains(
            DetailedReportTransactionRowDto.builder()
                .accountNo("177A")
                .consolidatedAccountNo("ConsolidatedAcc")
                .transactionDate(LocalDate.of(2026, 5, 14))
                .transactionType(DefendantTransactionType.CONSOL.getLabel())
                .transactionUserId("enforcement.test")
                .transactionAmount(new BigDecimal("123.45"))
                .transactionDetails("Account consolidated | 77 | Amount credited to master account")
                .build(),
            DetailedReportTransactionRowDto.builder()
                .accountNo("177A")
                .consolidatedAccountNo(null)
                .transactionDate(LocalDate.of(2026, 5, 14))
                .transactionType(DefendantTransactionType.PAYMNT.getLabel())
                .transactionUserId("enforcement.test")
                .transactionAmount(new BigDecimal("50.00"))
                .transactionDetails("Payment received | Credit Transfer")
                .build()
        );

        Assertions.assertThat(report.getTransactionRows())
            .hasSize(21)
            .extracting(
                DetailedReportTransactionRowDto::getTransactionType,
                DetailedReportTransactionRowDto::getTransactionDetails
            )
            .containsExactlyInAnyOrder(
                tuple(
                    DefendantTransactionType.CONSOL.getLabel(),
                    "Account consolidated | 77 | Amount credited to master account"
                ),
                tuple(
                    DefendantTransactionType.PAYMNT.getLabel(),
                    "Payment received | Credit Transfer"
                ),
                tuple(
                    DefendantTransactionType.CANCHQ.getLabel(),
                    "Cheque cancelled | Cheque number: CHQ1003"
                ),
                tuple(
                    DefendantTransactionType.CHEQUE.getLabel(),
                    "Cheque issued | Cheque number: CHQ1004 | Dishonoured 2026-05-14T10:15:30"
                ),
                tuple(
                    DefendantTransactionType.CHEQUE.getLabel(),
                    "Cheque issued | Cheque number: Not yet written | Cancelled 2026-05-14T10:20:30"
                ),
                tuple(
                    DefendantTransactionType.DISHCQ.getLabel(),
                    "Cheque dishonoured | FCOMP 50.00 Created: 2023-11-03T16:05:10 | 1"
                ),
                tuple(
                    "FR_SUS",
                    "Transfer from suspense | SUS-1007"
                ),
                tuple(
                    DefendantTransactionType.MADJ.getLabel(),
                    "Manual adjustment | MADJ-1008"
                ),
                tuple(
                    DefendantTransactionType.PAYMNT.getLabel(),
                    "Payment received | Credit Transfer | Payment by credit transfer | PR1009"
                ),
                tuple(
                    DefendantTransactionType.REPSUS.getLabel(),
                    "Repayment from suspense | REP-1010"
                ),
                tuple(
                    DefendantTransactionType.REVPAY.getLabel(),
                    "Payment reversed | FCPC 50.00 Created: 2023-11-03T16:05:10 | 2"
                ),
                tuple(
                    DefendantTransactionType.RICHEQ.getLabel(),
                    "Cheque reissued | Cheque number: CHQ1012 | Dishonoured 2026-05-14T10:55:30"
                ),
                tuple(
                    DefendantTransactionType.RVWOFF.getLabel(),
                    "Write-off reversed Reinstated after review"
                ),
                tuple(
                    DefendantTransactionType.TFO.getLabel(),
                    "TFO out | Transferred to: Central Office Kingston-upon-Thames Mags Court"
                ),
                tuple(
                    "TFO_IN",
                    "TFO in | Received from: Kingston-upon-Thames Mags Court"
                ),
                tuple(
                    DefendantTransactionType.WRTOFF.getLabel(),
                    "Write-off | 2023-11-03T16:05:10 | FCOST 50.00 Created: 2023-11-03T16:05:10 | 3"
                        + " | Unknown whereabouts | Written off after judgment"
                ),
                tuple(
                    DefendantTransactionType.WRTOFF.getLabel(),
                    "Write-off | Transferred out - 77"
                ),
                tuple(
                    DefendantTransactionType.WRTOFF.getLabel(),
                    "Write-off | "
                ),
                tuple(
                    DefendantTransactionType.XFER.getLabel(),
                    "Suspense transfer | Cheque cancelled to suspense"
                ),
                tuple(
                    DefendantTransactionType.XFER.getLabel(),
                    "Suspense transfer | Cheque cancelled to Central Fund"
                ),
                tuple(
                    DefendantTransactionType.XFER.getLabel(),
                    "Suspense transfer | "
                )
            );
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8827")
    void generateReportData_filterByIncludeAdult_returnResultsOfAdults() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "1999-01-01",
              "includeAdult": true,
              "businessUnitIds": [77, 78]
            }
            """);
        //Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        //Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .isSorted();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getDateOfBirth())
            .filteredOn(Objects::nonNull)
            .allMatch(dob -> AgeUtil.calculateAge(dob) >= AgeUtil.ADULT_AGE)
            .isSorted();
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8832")
    void generateReportData_filterByIncludeYouth_returnResultsOfYouth() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "1999-01-01",
              "includeYouth": true,
              "businessUnitIds": [77, 78]
            }
            """);
        //Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        //Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .isSorted();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getDateOfBirth())
            .filteredOn(Objects::nonNull)
            .allMatch(dob -> AgeUtil.calculateAge(dob) < AgeUtil.ADULT_AGE)
            .isSorted();
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8836")
    void generateReportData_filterByIncludeCompany_returnResultsOfCompanies() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "1999-01-01",
              "includeCompany": true,
              "businessUnitIds": [77, 78]
            }
            """);
        //Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        //Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .isSorted();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getCompany())
            .allMatch("Y"::equals)
            .isSorted();
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8837")
    void generateReportData_filterByParentOrGuardian_returnResultsWithParentOrGuardian() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "1999-01-01",
              "onlyAccountsWithParentGuardian": true,
              "businessUnitIds": [77, 78]
            }
            """);
        //Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        //Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .isSorted();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getParentOrGuardian())
            .allMatch("Y"::equals)
            .isSorted();
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @CsvSource({
        "WITH, true",
        "WITHOUT, false"
    })
    @JiraTestKey(value = "PO-8820", name = "[1] collectionOrderChoice = \"WITH\", expectedValue = \"true\"")
    @JiraTestKey(value = "PO-8821", name = "[2] collectionOrderChoice = \"WITHOUT\", expectedValue = \"false\"")
    void generateReportData_filterByCollectionOrderChoice_returnResults(
        String collectionOrderChoice,
        boolean expectedValue
    ) {
        // Arrange
        List<String> expectedAccountNumbers = defendantAccountRepository.findAll().stream()
            .filter(acc -> Boolean.valueOf(expectedValue).equals(acc.getCollectionOrder()))
            .filter(acc -> acc.getBusinessUnit().getBusinessUnitId() == 77)
            .map(DefendantAccountEntity::getAccountNumber)
            .distinct()
            .toList();

        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);

        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "2026-06-01",
              "collectionOrderChoice": "%s",
              "businessUnitIds": [77]
            }
            """.formatted(collectionOrderChoice));
        // Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        // Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .containsExactlyInAnyOrderElementsOf(expectedAccountNumbers)
            .isSorted();
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8818")
    void generateReportData_filterByAccountStatusLive_returnResults() {
        // Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);

        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "1999-01-01",
              "accountStatus": "LIVE",
              "businessUnitIds": [77, 78]
            }
            """);
        // Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);

        // Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .isSorted();

        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(reports).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountRow().getAccountNo());
            DefendantAccountEntity defendant =
                defendantAccountRepository.findByDefendantAccountId(accountId).orElseThrow();
            Assertions.assertThat(defendant.getCompletedDate()).isNull();
            Assertions.assertThat(defendant.getAccountBalance()).isGreaterThan(BigDecimal.ZERO);
        });
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8828")
    void generateReportData_filterByAccountStatusClosed_returnResults() {
        // Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);

        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "1999-01-01",
              "accountStatus": "CLOSED",
              "businessUnitIds": [77, 78]
            }
            """);
        // Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);

        // Assert
        List<DetailedAccountReportDto> accounts =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(accounts)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .isSorted();

        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(accounts).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountRow().getAccountNo());
            DefendantAccountEntity defendant =
                defendantAccountRepository.findByDefendantAccountId(accountId).orElseThrow();
            assertThat(defendant.getCompletedDate() != null
                || BigDecimal.ZERO.compareTo(defendant.getAccountBalance()) == 0).isTrue();
        });
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8833")
    void generateReportData_filterByMinAndMaxBalance_returnSortedWithinMinAndMaxBalance() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "1999-01-01",
              "minBalance": 400.00,
              "maxBalance": 600.00,
              "businessUnitIds": [77, 78]
            }
            """);
        //Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        //Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getBalance())
            .filteredOn(Objects::nonNull)
            .allSatisfy(balance ->
                assertThat(balance).isBetween(BigDecimal.valueOf(400), BigDecimal.valueOf(600))
            );
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8825")
    void generateReportData_filterByFirstPaymentOrPayByInNext7Days_returnsForAccountWithPaymentInNext7Days() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "2026-06-01",
              "firstPaymentOrPayByInNext7Days": "true",
              "businessUnitIds": [77, 78]
            }
            """);
        //Arrange
        DefendantAccountEntity entity =
            defendantAccountRepository.findByDefendantAccountId(77L).orElseThrow();
        LocalDate inPast = LocalDate.now().plusDays(7);
        entity.setImposedHearingDate(inPast);
        entity.setCollectionOrderEffectiveDate(inPast);
        entity.setPaymentCardRequestedDate(inPast);
        defendantAccountRepository.saveAndFlush(entity);
        PaymentTermsEntity paymentTermsForSeededData = paymentTermsRepository
            .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(77L)
            .stream().findFirst().orElseThrow();
        paymentTermsForSeededData.setEffectiveDate(LocalDate.now().plusDays(7));
        paymentTermsRepository.saveAndFlush(paymentTermsForSeededData);

        //Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        //Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .contains(entity.getAccountNumber());
        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(reports).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountRow().getAccountNo());
            List<PaymentTermsEntity> paymentTerms = paymentTermsRepository
                .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(
                    accountId
                );
            Assertions.assertThat(paymentTerms).isNotEmpty();
            Assertions.assertThat(paymentTerms).anySatisfy(term ->
                Assertions.assertThat(term.getEffectiveDate())
                    .isBetween(LocalDate.now(), LocalDate.now().plusDays(7))
            );
        });
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8823")
    void generateReportData_filterByNameRange_returnSortedWithinNameRange() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "1999-01-01",
              "lowerNameRange": "l",
              "upperNameRange": "l",
              "businessUnitIds": [77, 78]
            }
            """);
        //Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        //Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getDefendantName())
            .allSatisfy(name ->
                assertThat(name.substring(0, 1).toLowerCase()).isEqualTo("l"));
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8829")
    void generateReportData_filterPaymentMadeSinceDate_returnSortedWithPaymentsMadeSinceDate() {
        // Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": true,
              "reportMode": "SINCE_DATE",
              "sinceDate": "2026-05-14",
              "businessUnitIds": [77, 78]
            }
            """);

        LocalDate sinceDate = LocalDate.parse("2026-05-14");

        // Act
        OperationDetailedReport result = (OperationDetailedReport) service.generateReportData(reportInstance);

        // Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();

        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .isSorted();

        Map<String, Long> accountNoToId = getAccountNoToId();

        Assertions.assertThat(reports).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountRow().getAccountNo());

            List<DefendantTransactionEntity> payments =
                defendantTransactionRepository.findByDefendantAccountId(accountId);

            Assertions.assertThat(payments)
                .anyMatch(transaction -> {
                    boolean recordTypeMatches =
                        AssociatedRecordType.DEFENDANT_ACCOUNTS.equals(
                            transaction.getAssociatedRecordType());

                    boolean transactionTypeMatches =
                        DefendantTransactionType.PAYMNT.equals(
                            transaction.getTransactionType())
                            || DefendantTransactionType.CHEQUE.equals(
                            transaction.getTransactionType());

                    boolean postedDateMatches =
                        !transaction.getPostedDate().isBefore(sinceDate);

                    return recordTypeMatches
                        && transactionTypeMatches
                        && postedDateMatches;
                });
        });
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8835")
    void generateReportData_filterPaymentNotMadeSinceDate_returnAccountsWithoutPaymentsMadeSinceDate() {
        // Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_DATE",
              "sinceDate": "2026-05-14",
              "businessUnitIds": [77, 78]
            }
            """);
        LocalDate sinceDate = LocalDate.parse("2026-05-14");
        // Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        // Assert
        List<DetailedAccountReportDto> reports =
            result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .isSorted();
        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(reports).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountRow().getAccountNo());
            List<DefendantTransactionEntity> payments =
                defendantTransactionRepository.findByDefendantAccountId(accountId);
            Assertions.assertThat(payments)
                .noneMatch(transaction ->
                    AssociatedRecordType.DEFENDANT_ACCOUNTS.equals(
                        transaction.getAssociatedRecordType())
                        && transaction.getStatus() != null
                        && (DefendantTransactionType.PAYMNT.equals(transaction.getTransactionType())
                        || DefendantTransactionType.CHEQUE.equals(transaction.getTransactionType()))
                        && !transaction.getPostedDate().isBefore(sinceDate)
                );
        });
        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8824")
    void generateReportData_filterNoPaymentSinceLastEnforcement_returnsAccountWithOnlyPaymentsBeforeLastEnforcement() {
        // Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_LAST_ENFORCEMENT",
              "sinceLastEnforcementAction": "ABDC",
              "businessUnitIds": [77, 78]
            }
            """);
        // Act
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);
        // Assert
        //Seeded data contains AccountNo noPaymentsAfterEnf with payment before last enforcement and no payments after
        List<DetailedAccountReportDto> reports = result.getDetailedReport().getAccountTransactionReports();
        reports.stream()
            .filter(r -> "noPaymentsAfterEnf".equals(r.getAccountRow().getAccountNo()))
            .findFirst()
            .orElseThrow();
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8834")
    void generateReportData_filterPaymentWithRegf_returnsExpectedResults_whenPaymentMadeIsTrue() {
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": true,
              "reportMode": "WITH_REGF",
              "businessUnitIds": [77, 78]
            }
            """);
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);

        List<DetailedAccountReportDto> reports = result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .contains("177A") //has payment after REGF
            .doesNotContain("noPaymentsAfterEnf") //payment before but not after REGF
            .doesNotContain("ConsolidatedAcc"); //no REGF enforcement
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8831")
    void generateReportData_filterPaymentWithRegf_returnsExpectedResults_whenPaymentMadeIsFalse() {
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "WITH_REGF",
              "businessUnitIds": [77, 78]
            }
            """);
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);

        List<DetailedAccountReportDto> reports = result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .doesNotContain("177A") //has payment after REGF
            .doesNotContain("ConsolidatedAcc") //no REGF enforcement
            .contains("noPaymentsAfterEnf"); //payment before but not after REGF
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8819")
    void generateReportData_combinesRegfFilterAndBaseFilters_returnsExpectedResults() {
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "WITH_REGF",
              "businessUnitIds": [77, 78],
              "lowerNameRange": "l",
              "upperNameRange": "l"
            }
            """);
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);

        List<DetailedAccountReportDto> reports = result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .doesNotContain("177A") //has payment after REGF
            .doesNotContain("ConsolidatedAcc") //no REGF enforcement
            .doesNotContain("noPaymentsAfterEnf"); //meets all payment criteria but not name range
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8838")
    void generateReportData_filterPaymentSinceLastEnforcement_returnsExpectedResults_whenPaymentMadeIsTrue() {
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": true,
              "reportMode": "SINCE_LAST_ENFORCEMENT",
              "sinceLastEnforcementAction": "ABDC",
              "businessUnitIds": [77, 78]
            }
            """);
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);

        List<DetailedAccountReportDto> reports = result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .contains("177A") //has payment after ABDC enforcement
            .doesNotContain("ConsolidatedAcc") //has no payments after ABDC enforcement
            .doesNotContain("noPaymentsAfterEnf"); //payment before but not after ABDC

        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8826")
    void generateReportData_combinesSinceLastEnforcementFilterAndBaseFilters_returnsExpectedResults() {
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": true,
              "reportMode": "SINCE_LAST_ENFORCEMENT",
              "sinceLastEnforcementAction": "ABDC",
              "businessUnitIds": [77, 78],
              "lowerNameRange": "l",
              "upperNameRange": "l"
            }
            """);
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);

        List<DetailedAccountReportDto> reports = result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .doesNotContain("177A") //meets all payment criteria but not name range
            .doesNotContain("ConsolidatedAcc") //has no payments after ABDC enforcement
            .doesNotContain("noPaymentsAfterEnf"); //payment before but not after ABDC

        verifyMetadata(result);
    }

    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-8830")
    void generateReportData_filterPaymentSinceLastEnforcement_returnsExpectedResults_whenPaymentMadeIsFalse() {
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "isPaymentMade": false,
              "reportMode": "SINCE_LAST_ENFORCEMENT",
              "sinceLastEnforcementAction": "ABDC",
              "businessUnitIds": [77, 78]
            }
            """);
        OperationDetailedReport result =
            (OperationDetailedReport) service.generateReportData(reportInstance);

        List<DetailedAccountReportDto> reports = result.getDetailedReport().getAccountTransactionReports();
        Assertions.assertThat(reports)
            .extracting(report -> report.getAccountRow().getAccountNo())
            .doesNotContain("177A") //has payment after ABDC enforcement
            .contains("ConsolidatedAcc") //has no payments after ABDC enforcement
            .contains("noPaymentsAfterEnf"); //payment before but not after ABDC

        verifyMetadata(result);
    }
}

