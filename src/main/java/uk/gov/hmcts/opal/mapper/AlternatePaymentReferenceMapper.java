package uk.gov.hmcts.opal.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.AlternatePaymentReferenceDto;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;

@Component
public class AlternatePaymentReferenceMapper {

    public AlternatePaymentReferenceDto toDto(AlternatePaymentReferenceEntity entity) {

        return AlternatePaymentReferenceDto.builder()
            .alternatePaymentReferenceId(entity.getAlternatePaymentReferenceId())
            .defendantAccountId(entity.getDefendantAccount().getDefendantAccountId())
            .relationship(entity.getRelationship())
            .category(entity.getCategory())
            .businessUnitCode(entity.getBusinessUnitCode())
            .aprText(entity.getAprText())
            .createdDatetime(entity.getCreatedDatetime())
            .updatedDatetime(entity.getUpdatedDatetime())
            .build();
    }
}