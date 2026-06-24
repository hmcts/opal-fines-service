package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportRowDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportTotalsRowDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.OperationByEnforcementSummaryReport;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.OperationReportByEnforcementService;
import uk.gov.hmcts.opal.util.AgeUtil;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_enforcements.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OperationReportByEnforcementServiceTest")
@DisplayName("OperationReportByEnforcementServiceTest")
public class OperationReportByEnforcementServiceSummaryTest extends AbstractIntegrationTest {

    @Autowired
    private OperationReportByEnforcementService service;
    @Autowired
    private DefendantAccountRepository defendantAccountRepository;
    @Autowired
    private EnforcementRepository enforcementRepository;
    @Autowired
    private PaymentTermsRepository paymentTermsRepository;

    // ----------------------------------------
    // Helper
    // ----------------------------------------
    private ReportInstanceEntity reportWithFilters(String json) {
        ReportInstanceEntity instance = new ReportInstanceEntity();
        instance.setReportParameters(json);
        return instance;
    }

    private @NonNull Map<String, Long> getAccountNoToId() {
        return defendantAccountRepository.findAll().stream()
            .collect(Collectors.toMap(
                DefendantAccountEntity::getAccountNumber,
                DefendantAccountEntity::getDefendantAccountId
            ));
    }

    private static void verifyMetadata(OperationByEnforcementSummaryReport result,
        List<OperationByEnforcementSummaryReportRowDto> transactions) {
        ReportMetaData reportMetadata = result.getReportMetaData();
        long numberOfRecords = result.getNumberOfRecords();
        assertThat(numberOfRecords).isEqualTo(transactions.size());
        assertThat((long) reportMetadata.getPdpoPartyIds().size()).isGreaterThanOrEqualTo(numberOfRecords);
        Assertions.assertThat(reportMetadata.getPdpoPartyIds()).doesNotHaveDuplicates();
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7852")
    void generateReportData_filterSummaryReportType_returnSortedResultsOfSummaryReportType() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY"
            }
            """);
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();
        //Verify fields mapped
        Assertions.assertThat(transactions)
            .anySatisfy(dto -> {
                assertThat(dto.getHeader1()).isEqualTo("DETAIL");
                assertThat(dto.getCompany()).isEqualTo("N");
                assertThat(dto.getDefendantName()).isEqualTo("Graham, Anna");
                assertThat(dto.getAccountNo()).isEqualTo("177A");
                assertThat(dto.getDateOfBirth()).isEqualTo(LocalDate.of(1980, 2, 3));
                assertThat(dto.getNationalInsuranceNo()).isEqualTo("A11111A");
                assertThat(dto.getCollectionOrder()).isEqualTo("Y");
                assertThat(dto.getLastMovementDate()).isEqualTo(LocalDate.of(2024, 1, 2));
                assertThat(dto.getImposingCourt()).isEqualTo("AAA Test Court");
                assertThat(dto.getAmountImposed()).isEqualByComparingTo("700.58");
                assertThat(dto.getAmountPaid()).isEqualByComparingTo("200.00");
                assertThat(dto.getBalance()).isEqualByComparingTo("-500.58");
                assertThat(dto.getAddress1()).isEqualTo("Lumber House");
                assertThat(dto.getAddress2()).isEqualTo("77 Gordon Road");
                assertThat(dto.getAddress3()).isEqualTo("Maidstone, Kent");
                assertThat(dto.getPostcode()).isEqualTo("MA4 1AL");
                assertThat(dto.getVehicleReg()).isEqualTo("AB77CDE");
                assertThat(dto.getVehicleMake()).isEqualTo("Toyota Prius");
                assertThat(dto.getEmail1()).isEqualTo("email@one.com");
                assertThat(dto.getEmail2()).isEqualTo("email@two.com");
                assertThat(dto.getEmployeeRef()).isEqualTo("EMPREF77");
                assertThat(dto.getEmployerName()).isEqualTo("Tesco Ltd");
                assertThat(dto.getEmployerAddress1()).isEqualTo("123 Employer Road");
                assertThat(dto.getEmployerAddress2()).isEqualTo("Employer Lane");
                assertThat(dto.getEmployerAddress3()).isEqualTo("London Borough");
                assertThat(dto.getEmployerAddress4()).isEqualTo("London");
                assertThat(dto.getEmployerAddress5()).isEqualTo("England");
                assertThat(dto.getEmployerPostcode()).isEqualTo("EMP1 2AA");
                assertThat(dto.getEmployerTel()).isEqualTo("02079997777");
                assertThat(dto.getEmployerEmail()).isEqualTo("employer77@company.com");
                assertThat(dto.getEnforcementReason()).isEqualTo("Test enforcement");
                assertThat(dto.getLastEnforcementDate()).isEqualTo(LocalDate.of(2000, 1, 2));
                assertThat(dto.getUser()).isEqualTo("L080JG");
                assertThat(dto.getWarrantRef()).isEqualTo("001/25/00001");
                assertThat(dto.getJailDays()).isEqualTo(101);
                assertThat(dto.getParentOrGuardian()).isEqualTo("N");
                assertThat(dto.getProsecutorCaseReference()).isEqualTo("090A");
            });

        OperationByEnforcementSummaryReportTotalsRowDto totalsRow = result.getEnforcementReport().getTotals();
        assertThat(totalsRow.getAccountsReported()).isEqualTo(transactions.size());
        int totalAccounts = (int) defendantAccountRepository.count();
        assertThat(totalAccounts).isEqualTo(totalsRow.getAccountsReported());

        BigDecimal expectedTotalBalance = transactions.stream()
            .map(OperationByEnforcementSummaryReportRowDto::getBalance)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalsRow.getTotalBalance()).isEqualByComparingTo(expectedTotalBalance);

        BigDecimal expectedTotalImposed = transactions.stream()
            .map(OperationByEnforcementSummaryReportRowDto::getAmountImposed)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalsRow.getTotalImposed()).isEqualByComparingTo(expectedTotalImposed);

        BigDecimal expectedTotalPaid = transactions.stream()
            .map(OperationByEnforcementSummaryReportRowDto::getAmountPaid)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalsRow.getTotalPaid()).isEqualByComparingTo(expectedTotalPaid);

        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7845")
    void generateReportData_filterByBusinessUnitIds_returnSortedResultsOfCorrectBusinessUnitIds() {
        //Arrange
        List<DefendantAccountEntity> accountsInBusinessUnit =
            defendantAccountRepository.findAllByBusinessUnit_BusinessUnitId((short) 77);
        List<String> accountNumbers =
            accountsInBusinessUnit.stream().map(DefendantAccountEntity::getAccountNumber).toList();
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "businessUnitIds": [77]
            }
            """);
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .allMatch(accountNumbers::contains)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7851")
    void generateReportData_filterByEnforcementModeAll_returnAllSortedResults() {
        //Arrange
        String json = """
            {
              "reportType": "SUMMARY",
              "reportEnforcementMode": "ALL"
            }
            """;
        //Act
        OperationByEnforcementSummaryReport result = (OperationByEnforcementSummaryReport)
            service.generateReportData(reportWithFilters(json));
        //Assert
        long totalAccounts = defendantAccountRepository.count();
        assertThat(totalAccounts).isEqualTo(result.getNumberOfRecords());
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        assertThat(transactions).isNotNull();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7846")
    void generateReportData_filterByEnforcementModeNull_returnAllSortedResults() {
        //Arrange
        String json = """
            {
              "reportType": "SUMMARY",
              "reportEnforcementMode": null
            }
            """;
        //Act
        OperationByEnforcementSummaryReport result = (OperationByEnforcementSummaryReport)
            service.generateReportData(reportWithFilters(json));
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        long totalAccounts = defendantAccountRepository.count();
        assertThat(totalAccounts).isEqualTo(transactions.size());
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7835")
    void generateReportData_filterByEnforcementModeLastActionWithNoEnforcementAction_throwsError() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportEnforcementMode": "LAST_ACTION",
              "reportType": "SUMMARY"
            }
            """
        );
        assertThrows(IllegalArgumentException.class, () -> service.generateReportData(reportInstance));
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7844")
    void generateReportData_filterByEnforcementModeLastActionWithoutDates_returnLastActionSortedResults() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportEnforcementMode": "LAST_ACTION",
              "enforcementAction": "ABDC",
              "reportType": "SUMMARY"
            }
            """
        );
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();
        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(transactions).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
            EnforcementEntity latest = enforcementRepository
                .findTopByDefendantAccountIdAndResultIdOrderByPostedDateDescResultIdDesc(
                    accountId, ResultId.ABDC.value()
                );
            Assertions.assertThat(latest).isNotNull();
        });
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7841")
    void generateReportData_filterByEnforcementModeLastActionWithDates_returnLastActionSortedResultsBetweenDates() {
        //Arrange
        LocalDateTime start = LocalDate.now().minusDays(2).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "reportEnforcementMode": "LAST_ACTION",
              "enforcementAction": "ABDC",
              "lastActionDateFrom": "%s",
              "lastActionDateTo": "%s"
            }
            """.formatted(
            start.format(fmt),
            end.format(fmt)
        ));

        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();
        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(transactions).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
            EnforcementEntity latest = enforcementRepository
                .findTopByDefendantAccountIdAndResultIdOrderByPostedDateDescResultIdDesc(
                    accountId, ResultId.ABDC.value()
                );
            Assertions.assertThat(latest).isNotNull();
            Assertions.assertThat(latest.getPostedDate())
                .isAfterOrEqualTo(start)
                .isBefore(end.plusDays(1));
        });
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7850")
    void generateReportData_filterByEnforcementModeRegfWithDates_returnRegfSortedResults() {
        LocalDate from = LocalDate.of(2000, 1, 1);
        LocalDate to = LocalDate.of(2000, 2, 2);
        String json = """
            {
              "reportType": "SUMMARY",
              "reportEnforcementMode": "REGF",
              "regfDateFrom": "%s",
              "regfDateTo": "%s"
            }
            """.formatted(from, to);
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport)
                service.generateReportData(reportWithFilters(json));
        // Assert
        assertThat(result).isNotNull();
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();

        Map<String, Long> accountNoToId = getAccountNoToId();

        Assertions.assertThat(transactions).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
            List<EnforcementEntity> regfEnforcements =
                enforcementRepository.findByDefendantAccountIdAndResultId(accountId, "REGF");
            Assertions.assertThat(regfEnforcements)
                .as("Account %s should have REGF enforcement in range", dto.getAccountNo())
                .isNotEmpty()
                .anySatisfy(e ->
                    assertThat(e.getPostedDate().toLocalDate())
                        .isBetween(from, to)
                );
        });
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7831")
    void generateReportData_filterByEnforcementModeRegf_returnRegfSortedResults() {
        String json = """
            {
              "reportEnforcementMode": "REGF",
              "reportType": "SUMMARY"
            }
            """;
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportWithFilters(json));
        // Assert
        assertThat(result).isNotNull();
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();

        Map<String, Long> accountNoToId = getAccountNoToId();

        Assertions.assertThat(transactions).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
            List<EnforcementEntity> regfEnforcements =
                enforcementRepository.findByDefendantAccountIdAndResultId(accountId, "REGF");
            Assertions.assertThat(regfEnforcements)
                .as("Account %s should have REGF enforcement in range", dto.getAccountNo())
                .isNotEmpty();
        });
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7837")
    void generateReportData_filterByEnforcementDate_returnSortedResults() {
        //Arrange
        String json = """
            {
              "reportType": "SUMMARY",
              "enforcementDateFrom": "2000-01-01",
              "enforcementDateTo": "2000-02-02"
            }
            """;
        //Act
        OperationByEnforcementSummaryReport result = (OperationByEnforcementSummaryReport)
            service.generateReportData(reportWithFilters(json));
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();

        List<String> expectedAccountNumbers = enforcementRepository.findAll().stream()
            .filter(e -> {
                LocalDate date = e.getPostedDate().toLocalDate();
                return !date.isBefore(LocalDate.of(2000, 1, 1))
                    && !date.isAfter(LocalDate.of(2000, 2, 2));
            })
            .map(e -> e.getDefendantAccount().getAccountNumber())
            .distinct()
            .toList();

        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .containsExactlyInAnyOrderElementsOf(expectedAccountNumbers);

        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7847")
    void generateReportData_filterByNotUnderEnforcement_returnResultsNotUnderEnforcement() {
        //Arrange
        String json = """
            {
              "reportType": "SUMMARY",
              "reportEnforcementMode": "NOT_UNDER_ENFORCEMENT"
            }
            """;
        //Act
        OperationByEnforcementSummaryReport result = (OperationByEnforcementSummaryReport)
            service.generateReportData(reportWithFilters(json));
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();

        Map<String, DefendantAccountEntity> accounts = defendantAccountRepository.findAll().stream()
            .collect(Collectors.toMap(
                DefendantAccountEntity::getAccountNumber,
                Function.identity()
            ));

        Assertions.assertThat(transactions)
            .allSatisfy(row -> {
                DefendantAccountEntity defendantAccount =
                    accounts.get(row.getAccountNo());
                Assertions.assertThat(defendantAccount).isNotNull();
                Assertions.assertThat(defendantAccount.getLastEnforcement()).isNull();
            });
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7839")
    void generateReportData_filterByIncludeAdult_returnResultsOfAdults() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "includeAdult": true
            }
            """);
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getDateOfBirth)
            .filteredOn(Objects::nonNull)
            .allMatch(dob -> AgeUtil.calculateAge(dob) >= AgeUtil.ADULT_AGE)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7842")
    void generateReportData_filterByIncludeYouth_returnResultsOfYouth() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "includeYouth": true
            }
            """);
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getDateOfBirth)
            .filteredOn(Objects::nonNull)
            .allMatch(dob -> AgeUtil.calculateAge(dob) < AgeUtil.ADULT_AGE)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7848")
    void generateReportData_filterByIncludeCompany_returnResultsOfCompanies() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "includeCompany": true
            }
            """);
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getCompany)
            .allMatch("Y"::equals)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7849")
    void generateReportData_filterByParentOrGuardian_returnResultsWithParentOrGuardian() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "onlyAccountsWithParentGuardian": true
            }
            """);
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getParentOrGuardian)
            .allMatch("Y"::equals)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7833")
    void generateReportData_filterByCollectionOrderChoice_returnResults_with() {
        assertCollectionOrderChoiceReturnsResults("WITH", true);
    }

    @Test
    @JiraTestKey("PO-8225")
    void generateReportData_filterByCollectionOrderChoice_returnResults_without() {
        assertCollectionOrderChoiceReturnsResults("WITHOUT", false);
    }

    private void assertCollectionOrderChoiceReturnsResults(
        String collectionOrderChoice,
        boolean expectedValue
    ) {
        // Arrange
        List<String> expectedAccountNumbers = defendantAccountRepository.findAll().stream()
            .filter(acc -> Boolean.valueOf(expectedValue).equals(acc.getCollectionOrder()))
            .map(DefendantAccountEntity::getAccountNumber)
            .distinct()
            .toList();

        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);

        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "collectionOrderChoice": "%s"
            }
            """.formatted(collectionOrderChoice));
        // Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        // Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .containsExactlyInAnyOrderElementsOf(expectedAccountNumbers)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7832")
    void generateReportData_filterByAccountStatusLive_returnResults() {
        // Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);

        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "accountStatus": "LIVE"
            }
            """);
        // Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);

        // Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();

        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(transactions).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
            DefendantAccountEntity defendant =
                defendantAccountRepository.findByDefendantAccountId(accountId).orElseThrow();
            Assertions.assertThat(defendant.getCompletedDate()).isNull();
            Assertions.assertThat(defendant.getAccountBalance()).isGreaterThan(BigDecimal.ZERO);
        });
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7840")
    void generateReportData_filterByAccountStatusClosed_returnResults() {
        // Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);

        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "accountStatus": "CLOSED"
            }
            """);
        // Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);

        // Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getAccountNo)
            .isSorted();

        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(transactions).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
            DefendantAccountEntity defendant =
                defendantAccountRepository.findByDefendantAccountId(accountId).orElseThrow();
            assertThat(defendant.getCompletedDate() != null
                || BigDecimal.ZERO.compareTo(defendant.getAccountBalance()) == 0).isTrue();
        });
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7843")
    void generateReportData_filterByMinAndMaxBalance_returnSortedWithinMinAndMaxBalance() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "minBalance": 400.00,
              "maxBalance": 600.00
            }
            """);
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getBalance)
            .filteredOn(Objects::nonNull)
            .allSatisfy(balance ->
                assertThat(balance).isBetween(BigDecimal.valueOf(400), BigDecimal.valueOf(600))
            );
        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7838")
    void generateReportData_filterByFirstPaymentOrPayByInNext7Days_returnsForAccountWithPaymentInNext7Days() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "firstPaymentOrPayByInNext7Days": "true"
            }
            """);
        //Arrange
        PaymentTermsEntity paymentTermsForSeededData = paymentTermsRepository
            .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(77L)
            .stream().findFirst().orElseThrow();
        paymentTermsForSeededData.setEffectiveDate(LocalDate.now().plusDays(7));
        paymentTermsRepository.saveAndFlush(paymentTermsForSeededData);

        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(transactions).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
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

        verifyMetadata(result, transactions);
    }

    @JiraStory("PO-2286")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7836")
    void generateReportData_filterByNameRange_returnSortedWithinNameRange() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY",
              "lowerNameRange": "l",
              "upperNameRange": "l"
            }
            """);
        //Act
        OperationByEnforcementSummaryReport result =
            (OperationByEnforcementSummaryReport) service.generateReportData(reportInstance);
        //Assert
        List<OperationByEnforcementSummaryReportRowDto> transactions =
            result.getEnforcementReport().getReportSummaryRows();
        Assertions.assertThat(transactions)
            .extracting(OperationByEnforcementSummaryReportRowDto::getDefendantName)
            .allSatisfy(name ->
                assertThat(name.substring(0, 1).toLowerCase()).isEqualTo("l"));
        verifyMetadata(result, transactions);
    }
}
