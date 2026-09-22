package uk.gov.hmcts.opal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Category;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Relationship;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

public class AlternatePaymentReferenceIntegrationTest extends AbstractIntegrationTest {

    private static final long TYPICAL_ID = 920001L;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AlternatePaymentReferenceRepository repository;

    void saveAndFlushEntity(AlternatePaymentReferenceEntity entity) {
        repository.saveAndFlush(entity);
    }

    AlternatePaymentReferenceEntity withAlternatePaymentReference() {
        return AlternatePaymentReferenceEntity.builder()
            .alternatePaymentReference(TYPICAL_ID)
            .defendantAccountId(2000L)
            .relationship(Relationship.AMALGAMATED)
            .category(Category.ACC)
            .businessUnitCode("A01")
            .aprText("12345678A")
            .build();
    }

    @BeforeEach
    void setUp() {
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

        assertThat(fetched.getAlternatePaymentReference()).isEqualTo(original.getAlternatePaymentReference());
        assertThat(fetched.getBusinessUnitCode()).isEqualTo(original.getBusinessUnitCode());
        assertThat(fetched.getDefendantAccountId()).isEqualTo(original.getDefendantAccountId());
        assertThat(fetched.getRelationship()).isEqualTo(original.getRelationship());
        assertThat(fetched.getCategory()).isEqualTo(original.getCategory());
        assertThat(fetched.getAprText()).isEqualTo(original.getAprText());
    }

    @Test
    @JiraStory("PO-6461")
    @JiraEpic("PO-3497")
    void shouldFetchByAPRText() {
        AlternatePaymentReferenceEntity original = withAlternatePaymentReference();
        saveAndFlushEntity(original);

        entityManager.clear();

        AlternatePaymentReferenceEntity fetched = repository.findByAprText("12345678A").orElseThrow();

        assertThat(fetched.getAlternatePaymentReference()).isEqualTo(original.getAlternatePaymentReference());
        assertThat(fetched.getBusinessUnitCode()).isEqualTo(original.getBusinessUnitCode());
        assertThat(fetched.getDefendantAccountId()).isEqualTo(original.getDefendantAccountId());
        assertThat(fetched.getRelationship()).isEqualTo(original.getRelationship());
        assertThat(fetched.getCategory()).isEqualTo(original.getCategory());
        assertThat(fetched.getAprText()).isEqualTo(original.getAprText());
    }

}

