package uk.gov.hmcts.opal.service.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.repository.ResultLegacyRepository;
import uk.gov.hmcts.opal.repository.ResultOpalRepository;

@Transactional
class ResultOpalRepositoryServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ResultOpalRepositoryService service;

    @Autowired
    private ResultOpalRepository resultOpalRepository;

    @Autowired
    private ResultLegacyRepository resultLegacyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void getResultById_readsOpalResult() {
        String id = "RSIT01";
        resultOpalRepository.save(ResultOpalEntity.builder().resultId(id).resultTitle("Opal title")
            .resultType(ResultType.RESULT).build());
        resultLegacyRepository.save(ResultLegacyEntity.builder().resultId(id).resultTitle("Legacy title")
            .resultTitleCy("Legacy title").resultType(ResultType.RESULT).impositionAccruing(false).build());
        entityManager.flush();
        entityManager.clear();

        assertEquals("Opal title", service.getResultById(id).orElseThrow().getResultTitle());
    }
}
