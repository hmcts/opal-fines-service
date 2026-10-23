package uk.gov.hmcts.opal.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Category;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Relationship;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesItem;

class AlternatePaymentReferenceMapperTest {

    private final AlternatePaymentReferenceMapper mapper = new AlternatePaymentReferenceMapperImpl();

    @Nested
    @DisplayName("Map an alternate payment reference to a response item")
    class ToAlternatePaymentReferencesItem {

        @Test
        @DisplayName("Maps reference details, timestamps, and associated account and business unit identifiers")
        void mapsReferenceDetails() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 10, 20, 9, 15, 30);
            LocalDateTime updatedAt = LocalDateTime.of(2026, 10, 21, 10, 20, 40);
            AlternatePaymentReferenceEntity entity = AlternatePaymentReferenceEntity.builder()
                .alternatePaymentReferenceId(123L)
                .defendantAccount(DefendantAccountEntity.builder().defendantAccountId(456L).build())
                .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 78).build())
                .relationship(Relationship.APR)
                .category(Category.APR)
                .aprText("APR-REFERENCE-1")
                .createdDatetime(createdAt)
                .updatedDatetime(updatedAt)
                .versionNumber(2L)
                .build();

            AlternatePaymentReferencesItem result = mapper.toAlternatePaymentReferencesItem(entity);

            assertThat(result.getAlternatePaymentReferenceId()).isEqualTo(123L);
            assertThat(result.getDefendantAccountId()).isEqualTo(456L);
            assertThat(result.getBusinessUnitId()).isEqualTo((short) 78);
            assertThat(result.getRelationship()).isEqualTo(AlternatePaymentReferencesItem.RelationshipEnum.APR);
            assertThat(result.getCategory()).isEqualTo(AlternatePaymentReferencesItem.CategoryEnum.APR);
            assertThat(result.getAprText()).isEqualTo("APR-REFERENCE-1");
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(result.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Leaves identifiers empty when the associated account and business unit are missing")
        void handlesMissingAssociations() {
            AlternatePaymentReferenceEntity entity = AlternatePaymentReferenceEntity.builder()
                .alternatePaymentReferenceId(123L)
                .aprText("APR-REFERENCE-1")
                .build();

            AlternatePaymentReferencesItem result = mapper.toAlternatePaymentReferencesItem(entity);

            assertThat(result.getAlternatePaymentReferenceId()).isEqualTo(123L);
            assertThat(result.getAprText()).isEqualTo("APR-REFERENCE-1");
            assertThat(result.getDefendantAccountId()).isNull();
            assertThat(result.getBusinessUnitId()).isNull();
        }

        @Test
        @DisplayName("Returns null when no alternate payment reference is supplied")
        void returnsNullForNullEntity() {
            assertThat(mapper.toAlternatePaymentReferencesItem(null)).isNull();
        }
    }
}
