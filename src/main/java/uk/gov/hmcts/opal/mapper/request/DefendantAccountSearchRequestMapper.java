package uk.gov.hmcts.opal.mapper.request;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchDefendantDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchReferenceNumberDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountSearchRequestMapper {

    @Mapping(target = "referenceNumberDto", source = "referenceNumber")
    AccountSearchDto toAccountSearchDto(PostDefendantAccountSearchRequestDefendantAccount request);

    ReferenceNumberDto toReferenceNumberDto(DefendantAccountSearchReferenceNumberDefendantAccount referenceNumber);

    DefendantDto toDefendantDto(DefendantAccountSearchDefendantDefendantAccount defendant);

    default List<Short> mapBusinessUnitIds(List<Integer> businessUnitIds) {
        return businessUnitIds == null
            ? null
            : businessUnitIds.stream().map(this::toShort).toList();
    }

    default Short toShort(Integer value) {
        return value == null ? null : value.shortValue();
    }
}
