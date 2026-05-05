package uk.gov.hmcts.opal.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.CLOSED;
import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.LIVE;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITH;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITHOUT;
import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.PARENT_GUARDIAN;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.ReportSpecs;
import uk.gov.hmcts.opal.service.report.ReportFiltersDto;

@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
public class ReportSpecsTest extends AbstractIntegrationTest {

    @Autowired
    private DefendantAccountRepository defendantAccountRepository;

    @ParameterizedTest
    @NullAndEmptySource
    void businessUnitSpec_businessUnitIdsNullOrEmpty_returnConjunction(List<Long> buIds) {
        //Arrange
        long total = defendantAccountRepository.count();
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .businessUnitIds(buIds)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results).hasSize((int) total);
    }

    @Test
    void businessUnitSpec_businessUnitIdsList_returnAllFromBusinessUnitIds() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .businessUnitIds(List.of(78L))
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .extracting(account -> account.getBusinessUnit().getBusinessUnitId())
            .containsOnly((short) 78);
    }

    @Test
    void accountTypesSpec_includeAdult_returnAllAdultAccounts() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .includeAdult(true)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .allSatisfy(account ->
                assertThat(account.getParties())
                    .anySatisfy(parties ->
                        assertThat(parties.getParty().getAge()).isGreaterThanOrEqualTo((short) 18))
            );
    }

    @Test
    void accountTypesSpec_includeYouth_returnAllYouthAccounts() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .includeYouth(true)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .allSatisfy(account ->
                assertThat(account.getParties())
                    .anySatisfy(parties ->
                        assertThat(parties.getParty().getAge()).isLessThan((short) 18))
            );
    }

    @Test
    void accountTypesSpec_includeCompany_returnAllCompanyAccounts() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .includeCompany(true)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .allSatisfy(account ->
                assertThat(account.getParties())
                    .anySatisfy(parties ->
                        assertThat(parties.getParty().isOrganisation()).isTrue()
                    )
            );
    }

    @Test
    void parentGuardianSpec_returnAllAccountsWithParentGuardian() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .onlyAccountsWithParentGuardian(true)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results).allSatisfy(account ->
            assertThat(account.getParties()).anySatisfy(parties ->
                assertThat(parties.getAssociationType()).isEqualTo(PARENT_GUARDIAN))
        );
    }

    @Test
    void collectionOrderSpec_withCollectionOrder_returnAllAccountsWithCollectionOrder() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .collectionOrderChoice(WITH)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .extracting(DefendantAccountEntity::getCollectionOrder)
            .containsOnly(true);
    }

    @Test
    void collectionOrderSpec_withoutCollectionOrder_returnAllAccountsWithoutCollectionOrder() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .collectionOrderChoice(WITHOUT)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .extracting(DefendantAccountEntity::getCollectionOrder)
            .containsOnly(false);
    }

    @Test
    void accountStatusSpec_live_returnAllAccountsWithLiveStatus() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .accountStatus(LIVE)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .allSatisfy(r -> {
                assertThat(r.getCompletedDate()).isNull();
                assertThat(r.getAccountBalance()).isGreaterThan(BigDecimal.ZERO);
            });
    }

    @Test
    void accountStatusSpec_closed_returnAllAccountsWithClosedStatus() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .accountStatus(CLOSED)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .anySatisfy(r -> {
                assertThat(r.getCompletedDate()).isNotNull();
                assertThat(r.getAccountBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            });
    }

    @Test
    void balanceRangeSpec_minAndMaxGiven_returnAllAccountsWithinRange() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .maxBalance(BigDecimal.valueOf(600))
            .minBalance(BigDecimal.valueOf(400))
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .allSatisfy(r -> {
                assertThat(r.getAccountBalance()).isGreaterThanOrEqualTo(BigDecimal.valueOf(400));
                assertThat(r.getAccountBalance()).isLessThanOrEqualTo(BigDecimal.valueOf(600));
            });
    }

    @Test
    void nameRangeSpec_lowerAndUpperGiven_returnAllAccountsWithinRange() {
        //Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .lowerNameRange("l")
            .upperNameRange("l")
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results).allSatisfy(account ->
            assertThat(account.getParties()).anySatisfy(parties -> {
                String name = displayName(parties.getParty());
                assertThat(name).isNotNull();
                assertThat(name.substring(0, 1).toLowerCase()).isEqualTo("l");
            })
        );
    }

    @Test
    void next7DaysSpec_true_returnsAccountsWhereRelevantDateIsInNext7Days() {
        //Arrange
        DefendantAccountEntity entity =
            defendantAccountRepository.findByDefendantAccountId(77L).orElseThrow();
        LocalDate inPast = LocalDate.now().minusDays(7);
        entity.setImposedHearingDate(inPast);
        entity.setCollectionOrderEffectiveDate(inPast);
        entity.setPaymentCardRequestedDate(inPast);
        defendantAccountRepository.saveAndFlush(entity);

        DefendantAccountEntity entity2 =
            defendantAccountRepository.findByDefendantAccountId(78L).orElseThrow();
        LocalDate in7 = LocalDate.now().plusDays(7);
        entity2.setImposedHearingDate(in7);
        entity2.setCollectionOrderEffectiveDate(in7);
        entity2.setPaymentCardRequestedDate(in7);
        defendantAccountRepository.saveAndFlush(entity2);

        ReportFiltersDto filters = ReportFiltersDto.builder()
            .firstPaymentOrPayByInNext7Days(true)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results)
            .extracting(DefendantAccountEntity::getDefendantAccountId)
            .contains(78L)
            .doesNotContain(77L);
    }

    private static String displayName(PartyEntity party) {
        return party.getSurname() != null
            ? party.getSurname()
            : party.getOrganisationName();
    }

}

