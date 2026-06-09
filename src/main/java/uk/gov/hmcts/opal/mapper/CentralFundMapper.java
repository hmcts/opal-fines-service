package uk.gov.hmcts.opal.mapper;

import java.math.BigInteger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.dto.CentralFundResponse;
import uk.gov.hmcts.opal.generated.model.GetCentralFundResponse;
import uk.gov.hmcts.opal.repository.CentralFundProjection;

@Mapper(componentModel = "spring")
public interface CentralFundMapper {

    default CentralFundResponse toCentralFundResponse(CentralFundProjection centralFund) {
        return CentralFundResponse.builder()
            .payload(toPayload(centralFund))
            .version(toVersion(centralFund.getVersionNumber()))
            .build();
    }

    @Mapping(target = "majorCreditor.creditorAccountId", source = "creditorAccountId")
    @Mapping(target = "majorCreditor.accountNumber", source = "accountNumber")
    @Mapping(target = "majorCreditor.name", source = "name")
    @Mapping(target = "businessUnitDetails.businessUnitId", source = "businessUnitId")
    @Mapping(target = "businessUnitDetails.businessUnitName", source = "businessUnitName")
    @Mapping(
        target = "businessUnitDetails.welshSpeaking",
        source = "welshLanguage",
        qualifiedByName = "toWelshSpeaking"
    )
    GetCentralFundResponse toPayload(CentralFundProjection centralFund);

    @Named("toWelshSpeaking")
    default String toWelshSpeaking(Boolean welshLanguage) {
        return Boolean.TRUE.equals(welshLanguage) ? "Y" : "N";
    }

    default BigInteger toVersion(Long versionNumber) {
        return versionNumber == null ? null : BigInteger.valueOf(versionNumber);
    }
}
