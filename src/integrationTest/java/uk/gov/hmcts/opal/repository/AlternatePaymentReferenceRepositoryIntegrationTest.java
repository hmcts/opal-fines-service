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
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
public class AlternatePaymentReferenceRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final long TYPICAL_ID = 920001L;

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
        return AlternatePaymentReferenceEntity.builder()
            .alternatePaymentReferenceId(TYPICAL_ID)
            .defendantAccount(t)
            .relationship(Relationship.AMALGAMATED)
            .category(Category.ACC)
            .businessUnitCode("A01")
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

        AlternatePaymentReferenceEntity fetched = repository.findById(TYPICAL_ID).orElseThrow();

        assertThat(fetched.getAlternatePaymentReferenceId()).isEqualTo(original.getAlternatePaymentReferenceId());
        assertThat(fetched.getBusinessUnitCode()).isEqualTo(original.getBusinessUnitCode());
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
            repository.findByAprTextAndBusinessUnitCode("12345678A", "A01").orElseThrow();

        assertThat(fetched.getAlternatePaymentReferenceId()).isEqualTo(original.getAlternatePaymentReferenceId());
        assertThat(fetched.getBusinessUnitCode()).isEqualTo(original.getBusinessUnitCode());
        assertThat(fetched.getRelationship()).isEqualTo(original.getRelationship());
        assertThat(fetched.getCategory()).isEqualTo(original.getCategory());
        assertThat(fetched.getAprText()).isEqualTo(original.getAprText());
        assertThat(fetched.getDefendantAccount().getDefendantAccountId())
            .isEqualTo(original.getDefendantAccount().getDefendantAccountId());
    }

}

