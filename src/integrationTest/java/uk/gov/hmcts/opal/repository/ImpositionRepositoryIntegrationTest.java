package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.jpa.ImpositionSpecs;

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
    private static final long COURT_ID = 551001L;
    private static final long CREDITOR_ACCOUNT_ID = 551004L;

    @Autowired
    private ImpositionRepository impositionRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldKeepAssociationsLazyWhenNoEntityGraphIsUsed() {
        entityManager.clear();

        ImpositionEntity imposition = entityManager.find(ImpositionEntity.class, IMPOSITION_ID);

        assertNotNull(imposition);
        assertAssociationsRemainLazy(imposition);
    }

    @Test
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
    void shouldKeepLiteEntityGraphAssociationsLazyForDefendantAccountFetch() {
        entityManager.clear();

        List<ImpositionEntity> impositions = impositionRepository.findAllByDefendantAccountId(DEFENDANT_ACCOUNT_ID);
        ImpositionEntity imposition = impositions.getFirst();

        assertEquals(1, impositions.size());
        assertEquals(IMPOSITION_ID, imposition.getImpositionId());
        assertAssociationsRemainLazy(imposition);
    }

    @Test
    void shouldKeepLiteEntityGraphAssociationsLazyForSpecificationFetch() {
        entityManager.clear();

        Specification<ImpositionEntity> spec = ImpositionSpecs.equalsCreditorAccountId(CREDITOR_ACCOUNT_ID);
        List<ImpositionEntity> impositions = impositionRepository.findAll(spec);
        ImpositionEntity imposition = impositions.getFirst();

        assertEquals(1, impositions.size());
        assertEquals(IMPOSITION_ID, imposition.getImpositionId());
        assertAssociationsRemainLazy(imposition);
    }

    private void assertAssociationsRemainLazy(ImpositionEntity imposition) {
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertFalse(unitUtil.isLoaded(imposition, "defendantAccount"));
        assertFalse(unitUtil.isLoaded(imposition, "imposingCourt"));
        assertFalse(unitUtil.isLoaded(imposition, "creditorAccount"));
    }

}
