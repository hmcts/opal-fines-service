package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import org.mapstruct.Mapper;
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
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper {

    default GetMajorCreditorAccountHeaderSummaryResponse toOpal(
        GetMajorCreditorAccountHeaderSummaryLegacyResponse legacy) {

        if (legacy == null) {
            return null;
        }

        GetMajorCreditorAccountHeaderSummaryResponse response = new GetMajorCreditorAccountHeaderSummaryResponse();
        response.setMajorCreditor(toOpal(legacy.getMajorCreditor()));
        response.setBusinessUnitDetails(toOpal(legacy.getBusinessUnitDetails()));
        response.setAwaitingPayout(legacy.getAwaitingPayout());

        if (legacy.getMajorCreditor() != null && legacy.getMajorCreditor().getAccountVersion() != null) {
            response.setVersion(BigInteger.valueOf(legacy.getMajorCreditor().getAccountVersion()));
        }

        return response;
    }

    default GetMajorCreditorAccountHeaderSummary200ResponseMajorCreditor toOpal(MajorCreditorLegacy legacy) {
        if (legacy == null) {
            return null;
        }

        return new GetMajorCreditorAccountHeaderSummary200ResponseMajorCreditor()
            .creditorAccountId(legacy.getCreditorAccountId())
            .accountNumber(legacy.getAccountNumber())
            .name(legacy.getName())
            .accountReference(toOpal(legacy.getAccountReference()));
    }

    default BusinessUnitSummaryCommon toOpal(BusinessUnitSummary legacy) {
        if (legacy == null) {
            return null;
        }

        return new BusinessUnitSummaryCommon()
            .businessUnitId(legacy.getBusinessUnitId())
            .businessUnitName(legacy.getBusinessUnitName())
            .welshSpeaking(legacy.getWelshSpeaking());
    }

    default CreditorAccountTypeReferenceCommon toOpal(CreditorAccountTypeReference legacy) {
        if (legacy == null) {
            return null;
        }

        String accountType = legacy.getAccountType();
        CreditorAccountTypeReferenceCommon accountReference = new CreditorAccountTypeReferenceCommon();

        if (accountType != null) {
            accountReference.setAccountType(CreditorAccountTypeReferenceCommon.AccountTypeEnum.fromValue(accountType));
        }

        String displayName = CreditorAccountType.getDisplayName(accountType);
        if (displayName != null) {
            accountReference.setDisplayName(CreditorAccountTypeReferenceCommon.DisplayNameEnum.fromValue(displayName));
        }

        return accountReference;
    }
}
