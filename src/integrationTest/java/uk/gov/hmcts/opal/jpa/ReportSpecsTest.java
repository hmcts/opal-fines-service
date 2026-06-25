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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationReportByEnforcementFiltersDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.repository.jpa.ReportSpecs;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
public class ReportSpecsTest extends AbstractIntegrationTest {

    @Autowired
    private DefendantAccountRepository defendantAccountRepository;
    @Autowired
    private PaymentTermsRepository paymentTermsRepository;

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7796")
    void businessUnitSpec_businessUnitIdsNull_returnConjunction() {
        assertBusinessUnitIdsNullOrEmptyReturnConjunction(null);
    }

    @Test
    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8221")
    void businessUnitSpec_businessUnitIdsEmpty_returnConjunction() {
        assertBusinessUnitIdsNullOrEmptyReturnConjunction(List.of());
    }

    private void assertBusinessUnitIdsNullOrEmptyReturnConjunction(List<Long> buIds) {
        //Arrange
        long total = defendantAccountRepository.count();
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .businessUnitIds(buIds)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results).hasSize((int) total);
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7791")
    void businessUnitSpec_businessUnitIdsList_returnAllFromBusinessUnitIds() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7789")
    void accountTypesSpec_includeAdult_returnAllAdultAccounts() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7802")
    void accountTypesSpec_includeYouth_returnAllYouthAccounts() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7798")
    void accountTypesSpec_includeCompany_returnAllCompanyAccounts() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7801")
    void parentGuardianSpec_returnAllAccountsWithParentGuardian() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7793")
    void collectionOrderSpec_withCollectionOrder_returnAllAccountsWithCollectionOrder() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7788")
    void collectionOrderSpec_withoutCollectionOrder_returnAllAccountsWithoutCollectionOrder() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7787")
    void accountStatusSpec_live_returnAllAccountsWithLiveStatus() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7792")
    void accountStatusSpec_closed_returnAllAccountsWithClosedStatus() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7799")
    void balanceRangeSpec_minAndMaxGiven_returnAllAccountsWithinRange() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7785")
    void nameRangeSpec_lowerAndUpperGiven_returnAllAccountsWithinRange() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
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

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7800")
    void next7DaysSpec_true_returnsAccountsWhereRelevantDateIsInNext7Days() {
        //Arrange
        PaymentTermsEntity paymentTermsForSeededData = paymentTermsRepository
            .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(77L)
            .stream().findFirst().orElseThrow();
        paymentTermsForSeededData.setEffectiveDate(LocalDate.now().plusDays(7));
        paymentTermsRepository.saveAndFlush(paymentTermsForSeededData);
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .firstPaymentOrPayByInNext7Days(true)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results).allSatisfy(account -> {
            List<PaymentTermsEntity> paymentTerms = paymentTermsRepository
                .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(
                    account.getDefendantAccountId()
                );

            assertThat(paymentTerms).isNotEmpty();

            assertThat(paymentTerms).anySatisfy(term ->
                assertThat(term.getEffectiveDate())
                    .isBetween(LocalDate.now(), LocalDate.now().plusDays(7))
            );
        });
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7790")
    void notUnderEnforcement_returnsAccountsWithoutEnforcements() {
        //Arrange
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.NOT_UNDER_ENFORCEMENT)
            .build();
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        //Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        //Assert
        assertThat(results).allSatisfy(account ->
            assertThat(account.getLastEnforcement()).isNull()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7786")
    void defendantAccountIdsIn_account() {
        // Arrange
        Specification<DefendantAccountEntity> spec =
            ReportSpecs.defendantAccountIdsIn(List.of(77L, 78L));
        // Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        // Assert
        assertThat(results)
            .extracting(DefendantAccountEntity::getDefendantAccountId)
            .containsExactlyInAnyOrder(77L, 78L);
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    @JiraTestKey("PO-7794")
    void defendantAccountIdsIn_nullList_returnsNoResults() {
        assertDefendantAccountIdsInReturnsNoResults(null);
    }

    @Test
    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-8222")
    void defendantAccountIdsIn_emptyList_returnsNoResults() {
        assertDefendantAccountIdsInReturnsNoResults(List.of());
    }

    private void assertDefendantAccountIdsInReturnsNoResults(List<Long> accountIds) {
        // Arrange
        Specification<DefendantAccountEntity> spec =
            ReportSpecs.defendantAccountIdsIn(accountIds);
        // Act
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(spec);
        // Assert
        assertThat(results).isEmpty();
    }

    private static String displayName(PartyEntity party) {
        return party.getSurname() != null
            ? party.getSurname()
            : party.getOrganisationName();
    }

}
