package uk.gov.hmcts.opal.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.jpa.EnforcementReportSpecs;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.gov.hmcts.opal.service.report.OperationReportByEnforcementFiltersDto;

@ActiveProfiles({"integration"})
@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_enforcements.sql", executionPhase = AFTER_TEST_CLASS)
class EnforcementReportSpecsTest extends AbstractIntegrationTest {

    @Autowired
    private EnforcementRepository enforcementRepository;

    // ----------------------------------------
    // Helper
    // ----------------------------------------
    private List<DefendantAccountEntity> getAccounts(OperationReportByEnforcementFiltersDto filters) {
        Specification<EnforcementEntity> spec = EnforcementReportSpecs.build(filters);

        return enforcementRepository.findAll(spec).stream()
            .map(EnforcementEntity::getDefendantAccount)
            .distinct()
            .toList();
    }

    // ----------------------------------------
    // Tests
    // ----------------------------------------

    @Test
    void noMode_returnsAllAccountsWithEnforcements() {
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder().build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).isNotEmpty();
    }

    @Test
    void allMode_noDates_returnsAllAccountsWithEnforcements() {
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.ALL)
            .build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).isNotEmpty();
    }

    @Test
    void allMode_withDates_filtersCorrectly() {
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.ALL)
            .enforcementDateFrom(LocalDate.of(2000, 1, 1))
            .enforcementDateTo(LocalDate.of(2000, 2, 1))
            .build();

        List<EnforcementEntity> enforcements =
            enforcementRepository.findAll(EnforcementReportSpecs.build(filters));

        assertThat(enforcements)
            .allSatisfy(e ->
                assertThat(e.getPostedDate().toLocalDate())
                    .isBetween(LocalDate.of(2000, 1, 1),
                        LocalDate.of(2000, 2, 1))
            );
    }

    @Test
    void lastAction_withEnforcementActionAndDates_returnsAccountsWithSpecifiedLastActionBetweenDates() {
        LocalDateTime start = LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(2000, 12, 30).atStartOfDay();
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.LAST_ACTION)
            .enforcementAction(ResultId.ABDC.value())
            .lastActionDateFrom(start.toLocalDate())
            .lastActionDateTo(end.toLocalDate())
            .build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).isNotEmpty();
        assertThat(results).allSatisfy(account -> {
            EnforcementEntity latestEnforcement = enforcementRepository
                .findTopByDefendantAccountIdAndResultIdOrderByPostedDateDescResultIdDesc(
                    account.getDefendantAccountId(), ResultId.ABDC.value()
                );
            assertThat(latestEnforcement.getResultId()).isEqualTo(ResultId.ABDC.value());
            assertThat(latestEnforcement.getPostedDate())
                .isAfterOrEqualTo(start)
                .isBefore(end.plusDays(1));
        });
    }

    @Test
    void lastAction_withEnforcementActionAndNoDates_returnsAccountsWithSpecifiedLastAction() {
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.LAST_ACTION)
            .enforcementAction(ResultId.ABDC.value())
            .build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).isNotEmpty();
        assertThat(results).allSatisfy(account -> {
            EnforcementEntity latestEnforcement = enforcementRepository
                .findTopByDefendantAccountIdAndResultIdOrderByPostedDateDescResultIdDesc(
                    account.getDefendantAccountId(), ResultId.ABDC.value()
                );
            assertThat(latestEnforcement.getResultId()).isEqualTo(ResultId.ABDC.value());
        });
    }

    @Test
    void lastAction_withNoEnforcementAction_throwsError() {
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.LAST_ACTION)
            .build();
        InvalidDataAccessApiUsageException ex = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> getAccounts(filters)
        );

        assertThat(ex.getCause())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("enforcementAction is required for LAST_ACTION");
    }

    @Test
    void regfMode_returnsAccountsWithRegf() {
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.REGF)
            .build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).allSatisfy(account -> {
            List<EnforcementEntity> regf = enforcementRepository.findAll().stream()
                .filter(enforcement -> enforcement.getDefendantAccountId().equals(account.getDefendantAccountId()))
                .filter(enforcement -> "REGF".equals(enforcement.getResultId()))
                .toList();
            assertThat(regf).isNotEmpty();
        });
    }

    @Test
    void regfMode_regfEnforcementBetweenDateRange_returnsAccountsWithRegf() {

        LocalDate from = LocalDate.of(2000, 1, 1);
        LocalDate to = LocalDate.of(2000, 1, 1);

        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.REGF)
            .regfDateFrom(from)
            .regfDateTo(to)
            .build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).allSatisfy(account -> {

            List<EnforcementEntity> regf = enforcementRepository.findAll().stream()
                .filter(enforcement -> enforcement.getDefendantAccountId().equals(account.getDefendantAccountId()))
                .filter(enforcement -> "REGF".equals(enforcement.getResultId()))
                .toList();

            assertThat(regf).isNotEmpty();

            assertThat(regf).allSatisfy(enforcement -> {
                assertThat(enforcement.getPostedDate())
                    .isAfterOrEqualTo(from.atStartOfDay());

                assertThat(enforcement.getPostedDate())
                    .isBefore(to.plusDays(1).atStartOfDay());
            });
        });
    }
}