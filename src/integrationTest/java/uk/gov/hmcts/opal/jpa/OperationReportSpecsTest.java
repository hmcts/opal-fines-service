package uk.gov.hmcts.opal.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.CLOSED;
import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.LIVE;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITH;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITHOUT;
import static uk.gov.hmcts.opal.dto.ResultId.ABDC;
import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.PARENT_GUARDIAN;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByEnforcementFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportFiltersDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.repository.jpa.OperationReportSpecs;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
class OperationReportSpecsTest extends AbstractIntegrationTest {

    @Autowired
    private DefendantAccountRepository defendantAccountRepository;

    @Autowired
    private PaymentTermsRepository paymentTermsRepository;

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("businessUnitFiltersNullOrEmpty")
    void businessUnitSpec_businessUnitIdsNullOrEmpty_returnConjunction(OperationReportFiltersDto filters) {
        long total = defendantAccountRepository.count();

        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results).hasSize((int) total);
    }

    private static Stream<OperationReportFiltersDto> businessUnitFiltersNullOrEmpty() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().businessUnitIds(null).build(),
            OperationReportByEnforcementFiltersDto.builder().businessUnitIds(List.of()).build(),
            OperationReportByPaymentFiltersDto.builder().businessUnitIds(null).build(),
            OperationReportByPaymentFiltersDto.builder().businessUnitIds(List.of()).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("businessUnitFiltersList")
    void businessUnitSpec_businessUnitIdsList_returnAllFromBusinessUnitIds(OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results)
            .extracting(account -> account.getBusinessUnit().getBusinessUnitId())
            .containsOnly((short) 78);
    }

    private static Stream<OperationReportFiltersDto> businessUnitFiltersList() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().businessUnitIds(List.of(78L)).build(),
            OperationReportByPaymentFiltersDto.builder().businessUnitIds(List.of(78L)).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("adultFilters")
    void accountTypesSpec_includeAdult_returnAllAdultAccounts(OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results)
            .allSatisfy(account ->
                assertThat(account.getParties())
                    .anySatisfy(parties ->
                        assertThat(parties.getParty().getAge()).isGreaterThanOrEqualTo((short) 18))
            );
    }

    private static Stream<OperationReportFiltersDto> adultFilters() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().includeAdult(true).build(),
            OperationReportByPaymentFiltersDto.builder().includeAdult(true).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("youthFilters")
    void accountTypesSpec_includeYouth_returnAllYouthAccounts(OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results)
            .allSatisfy(account ->
                assertThat(account.getParties())
                    .anySatisfy(parties ->
                        assertThat(parties.getParty().getAge()).isLessThan((short) 18))
            );
    }

    private static Stream<OperationReportFiltersDto> youthFilters() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().includeYouth(true).build(),
            OperationReportByPaymentFiltersDto.builder().includeYouth(true).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("companyFilters")
    void accountTypesSpec_includeCompany_returnAllCompanyAccounts(OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results)
            .allSatisfy(account ->
                assertThat(account.getParties())
                    .anySatisfy(parties -> assertThat(parties.getParty().isOrganisation()).isTrue())
            );
    }

    private static Stream<OperationReportFiltersDto> companyFilters() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().includeCompany(true).build(),
            OperationReportByPaymentFiltersDto.builder().includeCompany(true).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("parentGuardianFilters")
    void parentGuardianSpec_returnAllAccountsWithParentGuardian(OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results).allSatisfy(account ->
            assertThat(account.getParties()).anySatisfy(parties ->
                assertThat(parties.getAssociationType()).isEqualTo(PARENT_GUARDIAN))
        );
    }

    private static Stream<OperationReportFiltersDto> parentGuardianFilters() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().onlyAccountsWithParentGuardian(true).build(),
            OperationReportByPaymentFiltersDto.builder().onlyAccountsWithParentGuardian(true).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("collectionOrderFiltersWithValue")
    void collectionOrderSpec_withCollectionOrder_returnAllAccountsWithCollectionOrder(
        OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results)
            .extracting(DefendantAccountEntity::getCollectionOrder)
            .containsOnly(true);
    }

    private static Stream<OperationReportFiltersDto> collectionOrderFiltersWithValue() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().collectionOrderChoice(WITH).build(),
            OperationReportByPaymentFiltersDto.builder().collectionOrderChoice(WITH).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("collectionOrderFiltersWithoutValue")
    void collectionOrderSpec_withoutCollectionOrder_returnAllAccountsWithoutCollectionOrder(
        OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results)
            .extracting(DefendantAccountEntity::getCollectionOrder)
            .containsOnly(false);
    }

    private static Stream<OperationReportFiltersDto> collectionOrderFiltersWithoutValue() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().collectionOrderChoice(WITHOUT).build(),
            OperationReportByPaymentFiltersDto.builder().collectionOrderChoice(WITHOUT).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("liveStatusFilters")
    void accountStatusSpec_live_returnAllAccountsWithLiveStatus(OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results)
            .allSatisfy(r -> {
                assertThat(r.getCompletedDate()).isNull();
                assertThat(r.getAccountBalance()).isGreaterThan(BigDecimal.ZERO);
            });
    }

    private static Stream<OperationReportFiltersDto> liveStatusFilters() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().accountStatus(LIVE).build(),
            OperationReportByPaymentFiltersDto.builder().accountStatus(LIVE).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("closedStatusFilters")
    void accountStatusSpec_closed_returnAllAccountsWithClosedStatus(OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results)
            .anySatisfy(r -> {
                assertThat(r.getCompletedDate()).isNotNull();
                assertThat(r.getAccountBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            });
    }

    private static Stream<OperationReportFiltersDto> closedStatusFilters() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().accountStatus(CLOSED).build(),
            OperationReportByPaymentFiltersDto.builder().accountStatus(CLOSED).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("balanceRangeFilters")
    void balanceRangeSpec_minAndMaxGiven_returnAllAccountsWithinRange(OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results)
            .allSatisfy(r -> {
                assertThat(r.getAccountBalance()).isGreaterThanOrEqualTo(BigDecimal.valueOf(400));
                assertThat(r.getAccountBalance()).isLessThanOrEqualTo(BigDecimal.valueOf(600));
            });
    }

    private static Stream<OperationReportFiltersDto> balanceRangeFilters() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder()
                .maxBalance(BigDecimal.valueOf(600))
                .minBalance(BigDecimal.valueOf(400))
                .build(),
            OperationReportByPaymentFiltersDto.builder()
                .maxBalance(BigDecimal.valueOf(600))
                .minBalance(BigDecimal.valueOf(400))
                .build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("nameRangeFilters")
    void nameRangeSpec_lowerAndUpperGiven_returnAllAccountsWithinRange(OperationReportFiltersDto filters) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results).allSatisfy(account ->
            assertThat(account.getParties()).anySatisfy(parties -> {
                String name = displayName(parties.getParty());
                assertThat(name).isNotNull();
                assertThat(name.substring(0, 1).toLowerCase()).isEqualTo("l");
            })
        );
    }

    private static Stream<OperationReportFiltersDto> nameRangeFilters() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder()
                .lowerNameRange("l")
                .upperNameRange("l")
                .build(),
            OperationReportByPaymentFiltersDto.builder()
                .lowerNameRange("l")
                .upperNameRange("l")
                .build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("next7DaysFilters")
    void next7DaysSpec_true_returnsAccountsWhereRelevantDateIsInNext7Days(OperationReportFiltersDto filters) {
        PaymentTermsEntity paymentTermsForSeededData = paymentTermsRepository
            .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(77L)
            .stream().findFirst().orElseThrow();
        paymentTermsForSeededData.setEffectiveDate(LocalDate.now().plusDays(7));
        paymentTermsRepository.saveAndFlush(paymentTermsForSeededData);

        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

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

    private static Stream<OperationReportFiltersDto> next7DaysFilters() {
        return Stream.of(
            OperationReportByEnforcementFiltersDto.builder().firstPaymentOrPayByInNext7Days(true).build(),
            OperationReportByPaymentFiltersDto.builder().firstPaymentOrPayByInNext7Days(true).build()
        );
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    void notUnderEnforcement_returnsAccountsWithoutEnforcements() {
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.NOT_UNDER_ENFORCEMENT)
            .build();

        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results).allSatisfy(account -> assertThat(account.getLastEnforcement()).isNull());
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    void paymentSpec_isPaymentMade_true_withFutureSinceDate_returnsNoAccounts() {
        OperationReportByPaymentFiltersDto filters = OperationReportByPaymentFiltersDto.builder()
            .isPaymentMade(true)
            .sinceDate(LocalDate.now().plusYears(10))
            .build();

        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results).isEmpty();
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    void paymentSpec_isPaymentMade_false_withFutureSinceDate_returnsAllAccounts() {
        long total = defendantAccountRepository.count();

        OperationReportByPaymentFiltersDto filters = OperationReportByPaymentFiltersDto.builder()
            .isPaymentMade(false)
            .sinceDate(LocalDate.now().plusYears(10))
            .build();

        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results).hasSize((int) total);
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2256")
    @JiraEpic("PO-2248")
    @Test
    void paymentSpec_sinceLastEnforcementAction_returnsMatchingAccounts() {
        OperationReportByPaymentFiltersDto filters = OperationReportByPaymentFiltersDto.builder()
            .sinceLastEnforcementAction(ABDC)
            .build();

        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(OperationReportSpecs.build(filters));

        assertThat(results).allSatisfy(account ->
            assertThat(account.getLastEnforcement()).isEqualTo(ABDC.value())
        );
    }


    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @Test
    void defendantAccountIdsIn_account() {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(
            OperationReportSpecs.defendantAccountIdsIn(List.of(77L, 78L))
        );

        assertThat(results)
            .extracting(DefendantAccountEntity::getDefendantAccountId)
            .containsExactlyInAnyOrder(77L, 78L);
    }

    @JiraStory("PO-2286")
    @JiraStory("PO-2255")
    @JiraEpic("PO-2248")
    @ParameterizedTest
    @MethodSource("emptyAccountIdLists")
    void defendantAccountIdsIn_emptyOrNullList_returnsNoResults(List<Long> accountIds) {
        List<DefendantAccountEntity> results = defendantAccountRepository.findAll(
            OperationReportSpecs.defendantAccountIdsIn(accountIds)
        );

        assertThat(results).isEmpty();
    }

    private static Stream<List<Long>> emptyAccountIdLists() {
        return Stream.of(
            null,
            List.of()
        );
    }

    private static String displayName(PartyEntity party) {
        return party.getSurname() != null
            ? party.getSurname()
            : party.getOrganisationName();
    }
}
