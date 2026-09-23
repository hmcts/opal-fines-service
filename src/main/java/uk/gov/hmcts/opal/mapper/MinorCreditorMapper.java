package uk.gov.hmcts.opal.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchCreditor;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchDefendant;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchResultMinorCreditor;

@Mapper(componentModel = "spring")
public interface MinorCreditorMapper {

    Creditor toCreditor(MinorCreditorAccountSearchCreditor creditor);

    @Mapping(target = "creditorAccountId", source = "creditorId")
    @Mapping(target = "postcode", source = "postCode")
    @Mapping(target = "accountBalance", source = "creditorAccountBalance", qualifiedByName = "toBalance")
    @Mapping(target = "firstnames", source = "forenames")
    @Mapping(target = "defendant", source = ".")
    MinorCreditorAccountSearchResultMinorCreditor toCreditorAccountDto(MinorCreditorEntity entity);

    @Named("toBalance")
    default BigDecimal toBalance(Integer balance) {
        return balance != null ? BigDecimal.valueOf(balance) : BigDecimal.ZERO;
    }

    default MinorCreditorAccountSearchDefendant toDefendantDto(MinorCreditorEntity entity) {
        return MinorCreditorAccountSearchDefendant.builder()
            .defendantAccountId(entity.getDefendantAccountId() != null
                ? String.valueOf(entity.getDefendantAccountId()) : null)
            .organisation(entity.isOrganisation())
            .organisationName(entity.getDefendantOrganisationName())
            .firstnames(entity.getDefendantFornames())
            .surname(entity.getDefendantSurname())
            .build();
    }
}
