package uk.gov.hmcts.opal.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.jpa.EnforcementReportSpecs;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.gov.hmcts.opal.service.report.ReportFiltersDto;

@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_CLASS)
public class EnforcementReportSpecsTest extends AbstractIntegrationTest {

    @Autowired
    private DefendantAccountRepository defendantAccountRepository;

    @Autowired
    private EnforcementRepository enforcementRepository;

    @Test
    void enforcementSpec_filterContainsNoEnforcementMode_returnConjunction() {
        //Arrange
        long total = defendantAccountRepository.count();
        ReportFiltersDto filters = ReportFiltersDto.builder().build();
        Specification<DefendantAccountEntity> spec = EnforcementReportSpecs.enforcementSpec(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results).hasSize((int) total);
    }

    @Test
    void enforcementSpec_filterContainsEnforcementModeAllWithNoDateFilters_returnAll() {
        //Arrange
        long total = defendantAccountRepository.count();
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.ALL).build();
        Specification<DefendantAccountEntity> spec = EnforcementReportSpecs.enforcementSpec(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results).hasSize((int) total);
    }

    @Test
    void enforcementSpec_filterContainsEnforcementModeAllWithDateFilters_returnAllBetweenDates() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.ALL)
            .enforcementDateTo(LocalDate.of(2000, 2, 1))
            .enforcementDateFrom(LocalDate.of(2000, 1, 1))
            .build();
        Specification<DefendantAccountEntity> spec = EnforcementReportSpecs.enforcementSpec(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results).hasSize(1);
    }

    @Test
    void enforcementSpec_filterByLastActionWithDateFilters_returnsLastAction() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.LAST_ACTION)
            .enforcementDateTo(LocalDate.of(2000, 2, 1))
            .enforcementDateFrom(LocalDate.of(2000, 1, 1))
            .build();
        Specification<DefendantAccountEntity> spec = EnforcementReportSpecs.enforcementSpec(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .allMatch(entity -> entity.getFineRegistrationDate() != null)
            .extracting(DefendantAccountEntity::getDefendantAccountId);
    }

    @Test
    void enforcementSpec_filterByRegistrationOfFine_returnDefendantsWithRegf() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.REGF).build();
        Specification<DefendantAccountEntity> spec = EnforcementReportSpecs.enforcementSpec(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        List<Long> idsUnderEnforcement = enforcementRepository.findAll()
            .stream()
            .filter(enforcement -> enforcement.getResultId() != null && enforcement.getResultId().equals("REGF"))
            .map(EnforcementEntity::getDefendantAccountId)
            .toList();
        assertThat(results)
            .allMatch(entity -> entity.getFineRegistrationDate() != null)
            .extracting(DefendantAccountEntity::getDefendantAccountId)
            .containsAll(idsUnderEnforcement);
    }

    @Test
    void enforcementSpec_filterByNoEnforcementMode_returnDefendantsWithNoEnforcementMode() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.NOT_UNDER_ENFORCEMENT).build();
        Specification<DefendantAccountEntity> spec = EnforcementReportSpecs.enforcementSpec(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        List<Long> idsUnderEnforcement = enforcementRepository.findAll()
            .stream()
            .map(EnforcementEntity::getDefendantAccountId)
            .toList();
        assertThat(results)
            .extracting(DefendantAccountEntity::getDefendantAccountId)
            .doesNotContainAnyElementsOf(idsUnderEnforcement);
    }
}

