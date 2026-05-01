package uk.gov.hmcts.opal.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.jpa.EnforcementReportSpecs;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.gov.hmcts.opal.service.report.ReportFiltersDto;

@ActiveProfiles({"integration"})
@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_CLASS)
class EnforcementReportSpecsTest extends AbstractIntegrationTest {

    @Autowired
    private DefendantAccountRepository defendantAccountRepository;

    @Autowired
    private EnforcementRepository enforcementRepository;

    // ----------------------------------------
    // Helper
    // ----------------------------------------
    private List<DefendantAccountEntity> getAccounts(ReportFiltersDto filters) {
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
        ReportFiltersDto filters = ReportFiltersDto.builder().build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).isNotEmpty();
    }

    @Test
    void allMode_noDates_returnsAllAccountsWithEnforcements() {
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.ALL)
            .build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).isNotEmpty();
    }

    @Test
    void allMode_withDates_filtersCorrectly() {
        ReportFiltersDto filters = ReportFiltersDto.builder()
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
    void lastAction_filtersLatestOnly() {
        LocalDateTime start = LocalDate.now().minusDays(2).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.LAST_ACTION)
            .lastActionDateFrom(start.toLocalDate())
            .lastActionDateTo(end.toLocalDate())
            .build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).allSatisfy(account -> {
            LocalDateTime maxPostedDate = enforcementRepository
                .findTopByDefendantAccountIdOrderByPostedDateDescEnforcementIdDesc(
                    account.getDefendantAccountId()
                )
                .orElseThrow()
                .getPostedDate();

            assertThat(maxPostedDate)
                .isAfterOrEqualTo(start)
                .isBeforeOrEqualTo(end);
        });
    }

    @Test
    void regfMode_returnsAccountsWithRegf() {
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.REGF)
            .build();

        List<DefendantAccountEntity> results = getAccounts(filters);

        assertThat(results).allSatisfy(account -> {
            List<EnforcementEntity> regf = enforcementRepository.findAll().stream()
                .filter(e -> e.getDefendantAccountId().equals(account.getDefendantAccountId()))
                .filter(e -> "REGF".equals(e.getResultId()))
                .toList();

            assertThat(regf).isNotEmpty();
        });
    }

    @Test
    void notUnderEnforcement_returnsAccountsWithoutEnforcements() {
        Specification<DefendantAccountEntity> spec =
            EnforcementReportSpecs.notUnderEnforcement();

        List<DefendantAccountEntity> results =
            defendantAccountRepository.findAll(spec);

        List<Long> enforcedIds = enforcementRepository.findAll().stream()
            .map(EnforcementEntity::getDefendantAccountId)
            .toList();

        assertThat(results)
            .extracting(DefendantAccountEntity::getDefendantAccountId)
            .doesNotContainAnyElementsOf(enforcedIds);
    }
}