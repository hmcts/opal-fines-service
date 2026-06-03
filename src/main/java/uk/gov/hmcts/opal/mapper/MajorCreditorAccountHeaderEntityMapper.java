package uk.gov.hmcts.opal.mapper;

import java.math.BigInteger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorAccountHeaderSummary200ResponseMajorCreditor;

@Component
public class MajorCreditorAccountHeaderEntityMapper {

    public GetMajorCreditorAccountHeaderSummaryResponse toResponse(MajorCreditorAccountHeaderEntity entity) {
        GetMajorCreditorAccountHeaderSummaryResponse response = new GetMajorCreditorAccountHeaderSummaryResponse();
        response.setMajorCreditor(toMajorCreditor(entity));
        response.setBusinessUnitDetails(toBusinessUnitDetails(entity));
        response.setAwaitingPayout(entity.getAwaitingPayout());
        response.setVersion(entity.getVersionNumber() == null ? null : BigInteger.valueOf(entity.getVersionNumber()));
        return response;
    }

    private GetMajorCreditorAccountHeaderSummary200ResponseMajorCreditor toMajorCreditor(
        MajorCreditorAccountHeaderEntity entity) {
        GetMajorCreditorAccountHeaderSummary200ResponseMajorCreditor majorCreditor =
            new GetMajorCreditorAccountHeaderSummary200ResponseMajorCreditor();
        majorCreditor.setCreditorAccountId(entity.getCreditorAccountId());
        majorCreditor.setAccountNumber(entity.getCreditorAccountNumber());
        majorCreditor.setName(entity.getName());
        majorCreditor.setAccountReference(toAccountReference(entity.getCreditorAccountType()));
        return majorCreditor;
    }

    private BusinessUnitSummaryCommon toBusinessUnitDetails(MajorCreditorAccountHeaderEntity entity) {
        BusinessUnitSummaryCommon businessUnitDetails = new BusinessUnitSummaryCommon();
        businessUnitDetails.setBusinessUnitId(String.valueOf(entity.getBusinessUnitId()));
        businessUnitDetails.setBusinessUnitName(entity.getBusinessUnitName());
        businessUnitDetails.setWelshSpeaking("N");
        return businessUnitDetails;
    }

    private CreditorAccountTypeReferenceCommon toAccountReference(CreditorAccountType type) {
        CreditorAccountTypeReferenceCommon accountReference = new CreditorAccountTypeReferenceCommon();
        if (type != null) {
            accountReference.setAccountType(CreditorAccountTypeReferenceCommon.AccountTypeEnum.fromValue(type.name()));
            accountReference.setDisplayName(
                CreditorAccountTypeReferenceCommon.DisplayNameEnum.fromValue(type.getLabel()));
        }
        return accountReference;
    }
}
