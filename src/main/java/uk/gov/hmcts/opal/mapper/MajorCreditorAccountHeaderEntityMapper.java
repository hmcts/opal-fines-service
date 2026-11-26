package uk.gov.hmcts.opal.mapper;

import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorAccountHeaderSummary200ResponseMajorCreditor;
import uk.gov.hmcts.opal.mapper.common.BusinessUnitSummaryMapper;

@Component
@RequiredArgsConstructor
public class MajorCreditorAccountHeaderEntityMapper {

    private final BusinessUnitSummaryMapper businessUnitSummaryMapper;

    public GetMajorCreditorAccountHeaderSummaryResponse toResponse(MajorCreditorAccountHeaderEntity entity,
                                                                  boolean welshLanguageEnabled) {

        GetMajorCreditorAccountHeaderSummaryResponse response = new GetMajorCreditorAccountHeaderSummaryResponse();
        response.setMajorCreditor(toMajorCreditor(entity));
        response.setBusinessUnitDetails(toBusinessUnitDetails(entity, welshLanguageEnabled));
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

    private BusinessUnitSummaryCommon toBusinessUnitDetails(MajorCreditorAccountHeaderEntity entity,
                                                           boolean welshLanguageEnabled) {

        BusinessUnitSummaryCommon businessUnitDetails = new BusinessUnitSummaryCommon();
        businessUnitDetails.setBusinessUnitId(entity.getBusinessUnitId());
        businessUnitDetails.setBusinessUnitName(entity.getBusinessUnitName());
        businessUnitDetails.setBusinessUnitCode(entity.getBusinessUnitCode());
        businessUnitDetails.setWelshSpeaking(
            businessUnitSummaryMapper.toHeaderWelshSpeaking(entity.getWelshLanguage(), welshLanguageEnabled));
        return businessUnitDetails;
    }

    private CreditorAccountTypeReferenceCommon toAccountReference(CreditorAccountType type) {
        CreditorAccountTypeReferenceCommon accountReference = new CreditorAccountTypeReferenceCommon();
        if (type != null) {
            accountReference.setCreditorAccountType(
                CreditorAccountTypeReferenceCommon.CreditorAccountTypeEnum.fromValue(type.name()));
            accountReference.setCreditorAccountDisplayName(
                CreditorAccountTypeReferenceCommon.CreditorAccountDisplayNameEnum.fromValue(type.getLabel()));
        }
        return accountReference;
    }
}
