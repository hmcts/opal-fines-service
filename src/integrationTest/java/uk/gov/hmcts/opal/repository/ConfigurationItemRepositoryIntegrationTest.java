package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@DisplayName("Configuration Item Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_business_units_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_business_units_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/insertData/insert_into_configuration_items_json_shapes.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_configuration_items_json_shapes.sql",
    executionPhase = AFTER_TEST_METHOD
)
class ConfigurationItemRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final long CONFIGURATION_ITEM_ID = 95001L;
    private static final long ARRAY_CONFIGURATION_ITEM_ID = 95003L;
    private static final long OBJECT_CONFIGURATION_ITEM_ID = 95004L;
    private static final short BUSINESS_UNIT_ID = 501;

    @Autowired
    private ConfigurationItemRepository configurationItemRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    private PersistenceUnitUtil unitUtil;

    @BeforeEach
    void setUp() {
        entityManager.clear();
        unitUtil = entityManagerFactory.getPersistenceUnitUtil();
    }

    @Test
    @JiraStory("PO-2881")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6290")
    void shouldKeepBusinessUnitLazyWhenNoEntityGraphIsUsed() {
        ConfigurationItemEntity configurationItem = entityManager.find(
            ConfigurationItemEntity.class,
            CONFIGURATION_ITEM_ID
        );

        assertFalse(unitUtil.isLoaded(configurationItem, "businessUnit"));
    }

    @Test
    @JiraStory("PO-2881")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6291")
    void shouldLoadLiteEntityGraphForDirectFetch() {
        ConfigurationItemEntity configurationItem = findWithEntityGraph(ConfigurationItemEntity.ENTITY_GRAPH_LITE);

        assertFalse(unitUtil.isLoaded(configurationItem, "businessUnit"));
    }

    @Test
    @JiraStory("PO-2881")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6292")
    void shouldLoadFullEntityGraphForDirectFetch() {
        ConfigurationItemEntity configurationItem = findWithEntityGraph(ConfigurationItemEntity.ENTITY_GRAPH_FULL);
        BusinessUnitEntity businessUnit = configurationItem.getBusinessUnit();

        assertAll(
            () -> assertTrue(unitUtil.isLoaded(configurationItem, "businessUnit")),
            () -> assertEquals(BUSINESS_UNIT_ID, businessUnit.getBusinessUnitId()),
            () -> assertFalse(unitUtil.isLoaded(businessUnit, "parentBusinessUnit")),
            () -> assertFalse(unitUtil.isLoaded(businessUnit, "configurationItems"))
        );
    }

    @Test
    @JiraStory("PO-10530")
    @JiraEpic("PO-2468")
    void shouldReadArrayItemValues() {
        ConfigurationItemEntity configurationItem = configurationItemRepository.findById(ARRAY_CONFIGURATION_ITEM_ID)
            .orElseThrow();

        assertTrue(configurationItem.getItemValues().isArray());
        assertEquals("123456", configurationItem.getItemValues().get(0).get("sort_code").asText());
        assertEquals("01234567", configurationItem.getItemValues().get(0).get("account_number").asText());
        assertEquals("Interface Job Test Bank", configurationItem.getItemValues().get(0).get("name").asText());
    }

    @Test
    @JiraStory("PO-10530")
    @JiraEpic("PO-2468")
    void shouldReadObjectItemValues() {
        ConfigurationItemEntity configurationItem = configurationItemRepository.findById(OBJECT_CONFIGURATION_ITEM_ID)
            .orElseThrow();

        assertTrue(configurationItem.getItemValues().isObject());
        assertEquals("Graph Object Item", configurationItem.getItemValues().get("name").asText());
    }

    private ConfigurationItemEntity findWithEntityGraph(String entityGraphName) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
        return entityManager.find(
            ConfigurationItemEntity.class,
            CONFIGURATION_ITEM_ID,
            Map.of("jakarta.persistence.fetchgraph", entityGraph)
        );
    }
}
