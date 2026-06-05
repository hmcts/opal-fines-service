package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse.MajorCreditorLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorAccountHeaderSummary200ResponseMajorCreditor;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper {

    @Mapping(target = "version", source = "majorCreditor.accountVersion", qualifiedByName = "toVersion")
    GetMajorCreditorAccountHeaderSummaryResponse toOpal(
        GetMajorCreditorAccountHeaderSummaryLegacyResponse legacy);

    GetMajorCreditorAccountHeaderSummary200ResponseMajorCreditor toOpal(MajorCreditorLegacy legacy);

    BusinessUnitSummaryCommon toOpal(BusinessUnitSummary legacy);

    @Mapping(target = "accountType", source = "accountType", qualifiedByName = "toAccountType")
    @Mapping(target = "displayName", source = "accountType", qualifiedByName = "toDisplayName")
    CreditorAccountTypeReferenceCommon toOpal(CreditorAccountTypeReference legacy);

    @Named("toVersion")
    default BigInteger toVersion(Long value) {
        return value == null ? null : BigInteger.valueOf(value);
    }

    @Named("toAccountType")
    default CreditorAccountTypeReferenceCommon.AccountTypeEnum toAccountType(String accountType) {
        return accountType == null ? null : CreditorAccountTypeReferenceCommon.AccountTypeEnum.fromValue(accountType);
    }

    @Named("toDisplayName")
    default CreditorAccountTypeReferenceCommon.DisplayNameEnum toDisplayName(String accountType) {
        String displayName = CreditorAccountType.getDisplayName(accountType);
        return displayName == null ? null : CreditorAccountTypeReferenceCommon.DisplayNameEnum.fromValue(displayName);
    }
}
