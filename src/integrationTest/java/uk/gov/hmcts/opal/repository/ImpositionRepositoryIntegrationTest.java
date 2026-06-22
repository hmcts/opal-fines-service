package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountType;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountImpositionData;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountVersionData;
import uk.gov.hmcts.opal.repository.jpa.ImpositionSpecs;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@DisplayName("Imposition Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_impositions_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_impositions_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
class ImpositionRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final long IMPOSITION_ID = 551005L;
    private static final long DEFENDANT_ACCOUNT_ID = 551002L;
    private static final long MINOR_CREDITOR_DEFENDANT_ACCOUNT_ID = 551008L;
    private static final long EMPTY_DEFENDANT_ACCOUNT_ID = 551010L;
    private static final long UNKNOWN_DEFENDANT_ACCOUNT_ID = 559999L;
    private static final long COURT_ID = 551001L;
    private static final long CREDITOR_ACCOUNT_ID = 551004L;

    @Autowired
    private ImpositionRepository impositionRepository;

    @Autowired
    private DefendantAccountRepository defendantAccountRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @JiraStory("PO-2884")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6304")
    void shouldKeepAssociationsLazyWhenNoEntityGraphIsUsed() {
        entityManager.clear();

        ImpositionEntity imposition = entityManager.find(ImpositionEntity.class, IMPOSITION_ID);

        assertNotNull(imposition);
        assertAssociationsRemainLazy(imposition);
    }

    @Test
    @JiraStory("PO-2884")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6307")
    void shouldLoadFullEntityGraphForDirectFetch() {
        entityManager.clear();

        ImpositionEntity imposition = impositionRepository.findById(IMPOSITION_ID).orElseThrow();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(unitUtil.isLoaded(imposition, "defendantAccount"));
        assertTrue(unitUtil.isLoaded(imposition, "imposingCourt"));
        assertTrue(unitUtil.isLoaded(imposition, "creditorAccount"));
        assertEquals(DEFENDANT_ACCOUNT_ID, imposition.getDefendantAccount().getDefendantAccountId());
        assertEquals(COURT_ID, imposition.getImposingCourt().getCourtId());
        assertEquals(CREDITOR_ACCOUNT_ID, imposition.getCreditorAccount().getCreditorAccountId());
    }

    @Test
    @JiraStory("PO-2884")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6305")
    void shouldKeepLiteEntityGraphAssociationsLazyForDefendantAccountFetch() {
        entityManager.clear();

        List<ImpositionEntity> impositions = impositionRepository.findAllByDefendantAccountId(DEFENDANT_ACCOUNT_ID);
        ImpositionEntity imposition = impositions.getFirst();

        assertEquals(1, impositions.size());
        assertEquals(IMPOSITION_ID, imposition.getImpositionId());
        assertAssociationsRemainLazy(imposition);
    }

    @Test
    @JiraStory("PO-2884")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6306")
    void shouldKeepLiteEntityGraphAssociationsLazyForSpecificationFetch() {
        entityManager.clear();

        Specification<ImpositionEntity> spec = ImpositionSpecs.equalsCreditorAccountId(CREDITOR_ACCOUNT_ID);
        List<ImpositionEntity> impositions = impositionRepository.findAll(spec);
        ImpositionEntity imposition = impositions.getFirst();

        assertEquals(1, impositions.size());
        assertEquals(IMPOSITION_ID, imposition.getImpositionId());
        assertAssociationsRemainLazy(imposition);
    }

    @Test
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7807")
    void shouldReturnDefendantAccountImpositionProjectionForMajorCreditor() {
        entityManager.clear();

        List<DefendantAccountImpositionData> impositions = impositionRepository
            .findDefendantAccountImpositionsByDefendantAccountId(DEFENDANT_ACCOUNT_ID);
        DefendantAccountImpositionData imposition = impositions.getFirst();

        assertEquals(1, impositions.size());
        assertEquals(DEFENDANT_ACCOUNT_ID, imposition.defendantAccountId());
        assertEquals(7L, imposition.accountVersionNumber());
        assertEquals(DefendantAccountType.FINES, imposition.defendantAccountType());
        assertEquals(IMPOSITION_ID, imposition.impositionId());
        assertEquals(LocalDateTime.of(2026, 4, 17, 10, 0), imposition.postedDate());
        assertEquals("IGR001", imposition.resultId());
        assertEquals("Imposition Graph Result", imposition.resultTitle());
        assertEquals(CREDITOR_ACCOUNT_ID, imposition.creditorAccountId());
        assertEquals(CreditorAccountType.MJ, imposition.creditorAccountType());
        assertEquals(551003L, imposition.majorCreditorId());
        assertEquals("Graph Major Creditor", imposition.majorCreditorName());
        assertNull(imposition.minorCreditorPartyId());
        assertEquals(LocalDateTime.of(2026, 4, 16, 9, 30), imposition.imposedDate());
        assertEquals(new BigDecimal("250.00"), imposition.imposedAmount());
        assertEquals(new BigDecimal("25.00"), imposition.paidAmount());
        assertEquals(5510L, imposition.offenceId());
        assertNull(imposition.impositionOffenceCode());
        assertNull(imposition.impositionOffenceTitle());
        assertEquals("IG5510", imposition.offenceCode());
        assertEquals("Imposition Graph Offence", imposition.offenceTitle());
        assertEquals(COURT_ID, imposition.imposingCourtId());
        assertEquals(Short.valueOf((short) 101), imposition.imposingCourtCode());
        assertEquals("Graph Test Court", imposition.imposingCourtName());
    }

    @Test
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7804")
    void shouldReturnDefendantAccountImpositionProjectionForMinorCreditor() {
        entityManager.clear();

        List<DefendantAccountImpositionData> impositions = impositionRepository
            .findDefendantAccountImpositionsByDefendantAccountId(MINOR_CREDITOR_DEFENDANT_ACCOUNT_ID);
        DefendantAccountImpositionData imposition = impositions.getFirst();

        assertEquals(1, impositions.size());
        assertEquals(MINOR_CREDITOR_DEFENDANT_ACCOUNT_ID, imposition.defendantAccountId());
        assertEquals(8L, imposition.accountVersionNumber());
        assertEquals(551009L, imposition.impositionId());
        assertEquals(551007L, imposition.creditorAccountId());
        assertEquals(CreditorAccountType.MN, imposition.creditorAccountType());
        assertNull(imposition.majorCreditorId());
        assertNull(imposition.majorCreditorName());
        assertEquals(551006L, imposition.minorCreditorPartyId());
        assertEquals(Boolean.FALSE, imposition.minorCreditorOrganisation());
        assertNull(imposition.minorCreditorOrganisationName());
        assertEquals("Ms", imposition.minorCreditorTitle());
        assertEquals("Creditor", imposition.minorCreditorForenames());
        assertEquals("Minor", imposition.minorCreditorSurname());
        assertNull(imposition.imposingCourtId());
        assertNull(imposition.imposingCourtCode());
        assertNull(imposition.imposingCourtName());
    }

    @Test
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7806")
    void shouldReturnNoImpositionProjectionRowsForExistingAccountWithoutImpositions() {
        entityManager.clear();

        List<DefendantAccountImpositionData> impositions = impositionRepository
            .findDefendantAccountImpositionsByDefendantAccountId(EMPTY_DEFENDANT_ACCOUNT_ID);

        assertTrue(impositions.isEmpty());
    }

    @Test
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7803")
    void shouldReturnDefendantAccountVersionProjectionForExistingAccount() {
        entityManager.clear();

        DefendantAccountVersionData versionData = defendantAccountRepository
            .findVersionDataByDefendantAccountId(EMPTY_DEFENDANT_ACCOUNT_ID)
            .orElseThrow();

        assertEquals(EMPTY_DEFENDANT_ACCOUNT_ID, versionData.defendantAccountId());
        assertEquals(9L, versionData.versionNumber());
    }

    @Test
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7805")
    void shouldReturnEmptyDefendantAccountVersionProjectionForUnknownAccount() {
        entityManager.clear();

        assertTrue(defendantAccountRepository
            .findVersionDataByDefendantAccountId(UNKNOWN_DEFENDANT_ACCOUNT_ID)
            .isEmpty());
    }

    private void assertAssociationsRemainLazy(ImpositionEntity imposition) {
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertFalse(unitUtil.isLoaded(imposition, "defendantAccount"));
        assertFalse(unitUtil.isLoaded(imposition, "imposingCourt"));
        assertFalse(unitUtil.isLoaded(imposition, "creditorAccount"));
    }

}
