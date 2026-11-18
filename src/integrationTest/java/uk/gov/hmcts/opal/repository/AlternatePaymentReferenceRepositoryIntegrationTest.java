package uk.gov.hmcts.opal.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Category;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Relationship;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
public class AlternatePaymentReferenceRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final short NE_REGION_BU_ID = 78;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AlternatePaymentReferenceRepository repository;

    void saveAndFlushEntity(AlternatePaymentReferenceEntity entity) {
        repository.saveAndFlush(entity);
    }

    AlternatePaymentReferenceEntity withAlternatePaymentReference() {
        DefendantAccountEntity t = entityManager.getReference(
            DefendantAccountEntity.builder().defendantAccountId(77L).build()
        );
        BusinessUnitEntity bu = entityManager.getReference(BusinessUnitEntity.class, NE_REGION_BU_ID);

        return AlternatePaymentReferenceEntity.builder()
            .defendantAccount(t)
            .relationship(Relationship.AMALGAMATED)
            .category(Category.ACC)
            .businessUnit(bu)
            .aprText("12345678A")
            .createdDatetime(LocalDateTime.now())
            .updatedDatetime(LocalDateTime.now())
            .build();
    }

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    @JiraStory("PO-6461")
    @JiraEpic("PO-3497")
    void shouldPersistAndLoadAlternatePaymentReference() {
        AlternatePaymentReferenceEntity original = withAlternatePaymentReference();
        saveAndFlushEntity(original);

        entityManager.clear();

        AlternatePaymentReferenceEntity fetched = repository.findById(original.getAlternatePaymentReferenceId())
            .orElseThrow();

        assertThat(fetched.getAlternatePaymentReferenceId()).isEqualTo(original.getAlternatePaymentReferenceId());
        assertThat(fetched.getBusinessUnit()).isEqualTo(original.getBusinessUnit());
        assertThat(fetched.getRelationship()).isEqualTo(original.getRelationship());
        assertThat(fetched.getCategory()).isEqualTo(original.getCategory());
        assertThat(fetched.getAprText()).isEqualTo(original.getAprText());
        assertThat(fetched.getDefendantAccount().getDefendantAccountId())
            .isEqualTo(original.getDefendantAccount().getDefendantAccountId());
    }

    @Test
    @JiraStory("PO-6461")
    @JiraEpic("PO-3497")
    void shouldFetchByAPRText() {
        AlternatePaymentReferenceEntity original = withAlternatePaymentReference();
        saveAndFlushEntity(original);

        entityManager.clear();

        AlternatePaymentReferenceEntity fetched =
            repository.findByAprTextAndBusinessUnit_businessUnitId("12345678A", NE_REGION_BU_ID).orElseThrow();

        assertThat(fetched.getAlternatePaymentReferenceId()).isEqualTo(original.getAlternatePaymentReferenceId());
        assertThat(fetched.getBusinessUnit()).isEqualTo(original.getBusinessUnit());
        assertThat(fetched.getRelationship()).isEqualTo(original.getRelationship());
        assertThat(fetched.getCategory()).isEqualTo(original.getCategory());
        assertThat(fetched.getAprText()).isEqualTo(original.getAprText());
        assertThat(fetched.getDefendantAccount().getDefendantAccountId())
            .isEqualTo(original.getDefendantAccount().getDefendantAccountId());
    }

}
