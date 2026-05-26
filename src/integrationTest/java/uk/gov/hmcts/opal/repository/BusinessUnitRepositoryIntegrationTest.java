package uk.gov.hmcts.opal.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@ActiveProfiles({"integration"})
@DisplayName("Business Unit Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_business_units_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_business_units_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
class BusinessUnitRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final short BUSINESS_UNIT_ID = 501;
    private static final short PARENT_BUSINESS_UNIT_ID = 599;

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    @Autowired
    private BusinessUnitLiteRepository businessUnitLiteRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    private final BusinessUnitSpecs specs = new BusinessUnitSpecs();

    @Test
    @JiraStory("PO-2880")
    @JiraEpic("PO-304")
    void shouldKeepAssociationsLazyWhenNoEntityGraphIsUsed() {
        entityManager.clear();

        BusinessUnitEntity businessUnit = entityManager.find(BusinessUnitEntity.class, BUSINESS_UNIT_ID);
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertNotNull(businessUnit);
        assertFalse(unitUtil.isLoaded(businessUnit, "parentBusinessUnit"));
        assertFalse(unitUtil.isLoaded(businessUnit, "configurationItems"));
    }

    @Test
    @JiraStory("PO-2880")
    @JiraEpic("PO-304")
    void shouldLoadFullEntityGraphForDirectFetch() {
        entityManager.clear();

        BusinessUnitEntity businessUnit = businessUnitRepository.findById(BUSINESS_UNIT_ID).orElseThrow();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(unitUtil.isLoaded(businessUnit, "parentBusinessUnit"));
        assertTrue(unitUtil.isLoaded(businessUnit, "configurationItems"));
        assertNotNull(businessUnit.getParentBusinessUnit());
        assertEquals(PARENT_BUSINESS_UNIT_ID, businessUnit.getParentBusinessUnit().getBusinessUnitId());
        assertEquals(2, businessUnit.getConfigurationItems().size());
    }

    @Test
    @JiraStory("PO-2880")
    @JiraEpic("PO-304")
    void shouldLoadFullEntityGraphForSpecificationFetch() {
        entityManager.clear();

        Page<BusinessUnitEntity> page = businessUnitRepository.findBy(
            BusinessUnitSpecs.equalsBusinessUnitId(BUSINESS_UNIT_ID),
            ffq -> ffq.page(Pageable.unpaged())
        );
        BusinessUnitEntity businessUnit = page.getContent().getFirst();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(unitUtil.isLoaded(businessUnit, "parentBusinessUnit"));
        assertTrue(unitUtil.isLoaded(businessUnit, "configurationItems"));
        assertEquals(PARENT_BUSINESS_UNIT_ID, businessUnit.getParentBusinessUnit().getBusinessUnitId());
        assertEquals(2, businessUnit.getConfigurationItems().size());
    }

    @Test
    @JiraStory("PO-2880")
    @JiraEpic("PO-304")
    void shouldLoadLiteEntityGraphForReferenceDataFetchOnly() {
        entityManager.clear();

        Specification<BusinessUnitEntity> spec = specs.referenceDataFilter(java.util.Optional.of("Graph Child"));
        Page<BusinessUnitEntity> page = businessUnitLiteRepository.findBy(
            spec,
            ffq -> ffq.sortBy(Sort.by(Sort.Direction.ASC, "businessUnitName"))
                .page(Pageable.unpaged())
        );
        BusinessUnitEntity businessUnit = page.getContent().getFirst();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(unitUtil.isLoaded(businessUnit, "configurationItems"));
        assertFalse(unitUtil.isLoaded(businessUnit, "parentBusinessUnit"));
        assertEquals(2, businessUnit.getConfigurationItems().size());
    }

    @Test
    @JiraStory("PO-2880")
    @JiraEpic("PO-304")
    void shouldNotDuplicateBusinessUnitRowsWhenFullEntityGraphFetchesConfigurationItems() {
        entityManager.clear();

        Page<BusinessUnitEntity> page = businessUnitRepository.findBy(
            BusinessUnitSpecs.equalsBusinessUnitId(BUSINESS_UNIT_ID),
            ffq -> ffq.page(Pageable.unpaged())
        );

        assertEquals(1, page.getContent().size());
    }

    @Test
    @JiraStory("PO-2880")
    @JiraEpic("PO-304")
    void shouldNotDuplicateBusinessUnitRowsWhenLiteEntityGraphFetchesConfigurationItems() {
        entityManager.clear();

        Specification<BusinessUnitEntity> spec = specs.referenceDataFilter(java.util.Optional.of("Graph Child"));
        Page<BusinessUnitEntity> page = businessUnitLiteRepository.findBy(
            spec,
            ffq -> ffq.sortBy(Sort.by(Sort.Direction.ASC, "businessUnitName"))
                .page(Pageable.unpaged())
        );

        assertEquals(1, page.getContent().size());
    }
}
