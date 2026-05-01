package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.util.AgeUtil;

@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_CLASS)
@Slf4j(topic = "opal.OperationReportByEnforcementServiceTest")
@DisplayName("OperationReportByEnforcementServiceTest")
public class OperationReportByEnforcementServiceTest extends AbstractIntegrationTest {

    @Autowired
    private OperationReportByEnforcementService service;
    @Autowired
    private DefendantAccountRepository defendantAccountRepository;
    @Autowired
    private EnforcementRepository enforcementRepository;

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

    private static void verifyMetadata(OperationReportByEnforcementTransaction result,
        List<EnforcementReportRowDto> transactions) {
        ReportMetaData reportMetadata = result.getReportMetaData();
        int numberOfRecords = result.getNumberOfRecords();
        assertThat(numberOfRecords).isEqualTo(transactions.size());
        assertThat(reportMetadata.getPdpoPartyIds().size()).isGreaterThanOrEqualTo(numberOfRecords);
        Assertions.assertThat(reportMetadata.getPdpoPartyIds()).doesNotHaveDuplicates();
    }

    @Test
    void generateReportData_filterDetailedReportType_returnSortedResultsOfDetailedReportType() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "DETAILED"
            }
            """);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        verifyMetadata(result, transactions);
    }


    @Test
    void generateReportData_filterSummaryReportType_returnSortedResultsOfSummaryReportType() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportType": "SUMMARY"
            }
            """);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        //Currently there is no Summary option so this will be the same as Detailed
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByBusinessUnitIds_returnSortedResultsOfCorrectBusinessUnitIds() {
        //Arrange
        List<DefendantAccountEntity> accountsInBusinessUnit =
            defendantAccountRepository.findAllByBusinessUnit_BusinessUnitId((short) 77);
        List<String> accountNumbers =
            accountsInBusinessUnit.stream().map(DefendantAccountEntity::getAccountNumber).toList();
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "businessUnitIds": [77]
            }
            """);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .allMatch(accountNumbers::contains)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByEnforcementModeAll_returnAllSortedResults() {
        //Arrange
        String json = """
            {
              "reportEnforcementMode": "ALL"
            }
            """;
        //Act
        OperationReportByEnforcementTransaction result = (OperationReportByEnforcementTransaction)
            service.generateReportData(reportWithFilters(json));
        //Assert
        List<EnforcementReportRowDto> accounts = result.getTransactionList();
        assertThat(accounts).isNotNull();
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByEnforcementModeLastAction_returnLastActionSortedResults() {
        //Arrange
        LocalDateTime start = LocalDate.now().minusDays(2).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "reportEnforcementMode": "LAST_ACTION",
              "lastActionDateFrom": "%s",
              "lastActionDateTo": "%s"
            }
            """.formatted(
            start.format(fmt),
            end.format(fmt)
        ));

        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        Map<String, Long> accountNoToId = getAccountNoToId();
        transactions.forEach(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
            boolean exists = enforcementRepository
                .findTopByDefendantAccountIdOrderByPostedDateDescEnforcementIdDesc(accountId)
                .isPresent();

            if (!exists) {
                System.out.println("Missing enforcement for account: " + dto.getAccountNo());
            }
        });

        Assertions.assertThat(transactions).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
            Optional<EnforcementEntity> latest =
                enforcementRepository.findTopByDefendantAccountIdOrderByPostedDateDescEnforcementIdDesc(accountId);
            Assertions.assertThat(latest).isPresent();
            latest.ifPresent(e -> {
                Assertions.assertThat(e.getPostedDate())
                    .isAfterOrEqualTo(start)
                    .isBeforeOrEqualTo(end);
            });
        });
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByEnforcementModeRegf_returnRegfSortedResults() {
        // Arrange
        LocalDate from = LocalDate.of(2000, 1, 1);
        LocalDate to = LocalDate.of(2000, 2, 2);
        String json = """
        {
          "reportEnforcementMode": "REGF",
          "regfDateFrom": "%s",
          "regfDateTo": "%s"
        }
        """.formatted(from, to);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction)
                service.generateReportData(reportWithFilters(json));
        // Assert
        assertThat(result).isNotNull();
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
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

    @Test
    void generateReportData_filterByEnforcementDate_returnSortedResults() {
        //Arrange
        String json = """
            {
              "enforcementDateFrom": "2000-01-01",
              "enforcementDateTo": "2000-02-02"
            }
            """;
        //Act
        OperationReportByEnforcementTransaction result = (OperationReportByEnforcementTransaction)
            service.generateReportData(reportWithFilters(json));
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByNotUnderEnforcement_returnResultsNotUnderEnforcement() {
        //Arrange
        String json = """
            {
              "enforcementMode": "NOT_UNDER_ENFORCEMENT"
            }
            """;
        //Act
        OperationReportByEnforcementTransaction result = (OperationReportByEnforcementTransaction)
            service.generateReportData(reportWithFilters(json));
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByIncludeAdult_returnResultsOfAdults() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "includeAdult": true
            }
            """);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getDateOfBirth)
            .filteredOn(Objects::nonNull)
            .allMatch(dob -> AgeUtil.calculateAge(dob) >= AgeUtil.ADULT_AGE)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByIncludeYouth_returnResultsOfYouth() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "includeYouth": true
            }
            """);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getDateOfBirth)
            .filteredOn(Objects::nonNull)
            .allMatch(dob -> AgeUtil.calculateAge(dob) < AgeUtil.ADULT_AGE)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByIncludeCompany_returnResultsOfCompanies() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "includeCompany": true
            }
            """);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getCompany)
            .allMatch("Y"::equals)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByParentOrGuardian_returnResultsWithParentOrGuardian() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "onlyAccountsWithParentGuardian": true
            }
            """);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getParentOrGuardian)
            .allMatch("Y"::equals)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @ParameterizedTest
    @CsvSource({
        "WITH, true",
        "WITHOUT, false"
    })
    void generateReportData_filterByCollectionOrderChoice_returnResults(
        String collectionOrderChoice,
        boolean expectedValue
    ) {
        // Arrange
        List<String> expectedAccountNumbers = enforcementRepository.findAll().stream()
            .map(EnforcementEntity::getDefendantAccount)
            .filter(acc -> Boolean.valueOf(expectedValue).equals(acc.getCollectionOrder()))
            .map(DefendantAccountEntity::getAccountNumber)
            .distinct()
            .toList();

        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);

        given(reportInstance.getReportParameters()).willReturn("""
            {
              "collectionOrderChoice": "%s"
            }
            """.formatted(collectionOrderChoice));
        // Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        // Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .containsExactlyInAnyOrderElementsOf(expectedAccountNumbers)
            .isSorted();
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByAccountStatusLive_returnResults() {
        // Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);

        given(reportInstance.getReportParameters()).willReturn("""
            {
              "accountStatus": "LIVE"
            }
            """);
        // Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);

        // Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
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

    @Test
    void generateReportData_filterByAccountStatusClosed_returnResults() {
        // Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);

        given(reportInstance.getReportParameters()).willReturn("""
            {
              "accountStatus": "CLOSED"
            }
            """);
        // Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);

        // Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .isSorted();

        Map<String, Long> accountNoToId = getAccountNoToId();
        Assertions.assertThat(transactions).allSatisfy(dto -> {
            Long accountId = accountNoToId.get(dto.getAccountNo());
            DefendantAccountEntity defendant =
                defendantAccountRepository.findByDefendantAccountId(accountId).orElseThrow();

            assertThat(
                defendant.getCompletedDate() != null ||
                    BigDecimal.ZERO.compareTo(defendant.getAccountBalance()) == 0
            ).isTrue();
        });
        verifyMetadata(result, transactions);
    }

    @Test
    void generateReportData_filterByMinAndMaxBalance_returnSortedWithinMinAndMaxBalance() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "minBalance": 400.00,
              "maxBalance": 600.00
            }
            """);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getBalance)
            .filteredOn(Objects::nonNull)
            .allSatisfy(balance ->
                assertThat(balance).isBetween(BigDecimal.valueOf(400), BigDecimal.valueOf(600))
            );
        verifyMetadata(result, transactions);
    }

    @ParameterizedTest
    @MethodSource("next7DaysCases")
    void generateReportData_filterByFirstPaymentOrPayByInNext7Days_returnSortedWithFirstPaymentOrPayByInNext7Days(
        Consumer<DefendantAccountEntity> mutation
    ) {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "firstPaymentOrPayByInNext7Days": "true"
            }
            """);
        //Arrange
        DefendantAccountEntity entity =
            defendantAccountRepository.findAll().getFirst();
        mutation.accept(entity);
        defendantAccountRepository.saveAndFlush(entity);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getAccountNo)
            .contains(entity.getAccountNumber());
        verifyMetadata(result, transactions);
    }

    private static Stream<Consumer<DefendantAccountEntity>> next7DaysCases() {
        LocalDate targetDate = LocalDate.now().plusDays(7);

        return Stream.of(
            entity -> entity.setImposedHearingDate(targetDate),
            entity -> entity.setCollectionOrderEffectiveDate(targetDate),
            entity -> entity.setPaymentCardRequestedDate(targetDate)
        );
    }

    @Test
    void generateReportData_filterByNameRange_returnSortedWithinNameRange() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "lowerNameRange": "l",
              "upperNameRange": "l"
            }
            """);
        //Act
        OperationReportByEnforcementTransaction result =
            (OperationReportByEnforcementTransaction) service.generateReportData(reportInstance);
        //Assert
        List<EnforcementReportRowDto> transactions = result.getTransactionList();
        Assertions.assertThat(transactions)
            .extracting(EnforcementReportRowDto::getDefendantName)
            .allSatisfy(name ->
                assertThat(name.substring(0, 1).toLowerCase()).isEqualTo("l"));
        verifyMetadata(result, transactions);
    }
}


