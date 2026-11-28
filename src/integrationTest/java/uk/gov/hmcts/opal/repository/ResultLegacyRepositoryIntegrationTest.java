package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.ImpositionCategoriesEntity;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.repository.jpa.ResultLegacySpecs;

@Transactional
class ResultLegacyRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final String RESULT_ID = "LRIT01";
    private static final String OTHER_ID = "LRIT02";

    @Autowired
    private ResultLegacyRepository legacyRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        legacyRepository.saveAllAndFlush(List.of(
            ResultLegacyEntity.builder().resultId(RESULT_ID).resultTitle("Active result")
                .resultTitleCy("Active result").resultType(ResultType.RESULT).active(true).impositionAccruing(false).build(),
            ResultLegacyEntity.builder().resultId(OTHER_ID).resultTitle("Inactive result")
                .resultTitleCy("Inactive result").resultType(ResultType.RESULT).impositionAccruing(false).build()
        ));
        entityManager.clear();
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void findById_preservesNullableFlags() {
        ResultLegacyEntity result = legacyRepository.findById(RESULT_ID).orElseThrow();
        assertTrue(result.isActive());
        assertFalse(result.isManualEnforcement());
        assertNull(result.getRequiresEmploymentData());
        assertNull(result.getAllowPaymentTerms());
        assertNull(result.getAllowAdditionalAction());
        assertNull(result.getRequiresLja());
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void fullGraph_returnsResult() {
        assertEquals(RESULT_ID, legacyRepository.findWithFullGraphByResultId(RESULT_ID).orElseThrow().getResultId());
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void specification_filtersResults() {
        var spec = ResultLegacySpecs.equalsAnyResultId(List.of(RESULT_ID, OTHER_ID))
            .and(ResultLegacySpecs.hasActive(true));
        var page = legacyRepository.findBy(spec, query -> query.page(Pageable.unpaged()));
        assertEquals(List.of(RESULT_ID), page.getContent().stream().map(ResultLegacyEntity::getResultId).toList());
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void findByResultIdIn_returnsMatchingResults() {
        assertEquals(List.of(RESULT_ID), legacyRepository.findByResultIdIn(List.of(RESULT_ID, "NOPE99"))
            .stream().map(ResultLegacyEntity::getResultId).toList());
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void fullGraph_loadsImpositionCategory() {
        ImpositionCategoriesEntity category = entityManager.createQuery(
            "select c from ImpositionCategoriesEntity c", ImpositionCategoriesEntity.class)
            .setMaxResults(1).getResultList().stream().findFirst().orElseThrow();
        String categoryId = category.getImpositionCategoryId();
        ResultLegacyEntity result = legacyRepository.findById(RESULT_ID).orElseThrow();
        result.setImpositionCategory(category);
        entityManager.flush();
        entityManager.clear();

        result = legacyRepository.findWithFullGraphByResultId(RESULT_ID).orElseThrow();
        assertTrue(entityManagerFactory.getPersistenceUnitUtil().isLoaded(result, "impositionCategory"));
        assertNotNull(result.getImpositionCategory());
        assertEquals(categoryId, result.getImpositionCategory().getImpositionCategoryId());
    }
}
