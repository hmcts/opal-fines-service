package uk.gov.hmcts.opal.mapper.legacy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Creditor;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Imposition;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Offence;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.AccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.DisplayNameEnum;
import uk.gov.hmcts.opal.generated.model.OffenceReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ResultReferenceCommon;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountImpositionsLegacyResponseMapper {

    @Mapping(target = "payload", source = "impositions", qualifiedByName = "toPayload")
    GetDefendantAccountImpositionsResponse toOpal(GetDefendantAccountImpositionsLegacyResponse legacy);

    @Mapping(target = "dateAdded", source = "postedDetails.postedDate", qualifiedByName = "toLocalDate")
    @Mapping(target = "imposition", source = "result")
    @Mapping(target = "imposedBy", ignore = true)
    DefendantAccountImpositionCommon toOpal(Imposition legacy);

    ResultReferenceCommon toOpal(GetDefendantAccountImpositionsLegacyResponse.Result legacy);

    @Mapping(target = "accountType", source = "creditorAccountType.creditorAccountType",
        qualifiedByName = "toAccountType")
    @Mapping(target = "displayName", source = "creditorAccountType.creditorAccountType",
        qualifiedByName = "toDisplayName")
    @Mapping(target = "majorCreditorId", ignore = true)
    @Mapping(target = "minorCreditorPartyId", ignore = true)
    @Mapping(target = "name", source = "majorCreditorName")
    ImpositionCreditorReferenceCommon toOpal(Creditor legacy);

    @Mapping(target = "id", source = "offenceId")
    @Mapping(target = "code", source = "cjsCode")
    @Mapping(target = "title", source = "offenceTitle")
    OffenceReferenceCommon toOpal(Offence legacy);

    @Named("toPayload")
    default DefendantAccountImpositionsResponseCommon toPayload(List<Imposition> impositions) {
        if (impositions == null) {
            return null;
        }

        return DefendantAccountImpositionsResponseCommon.builder()
            .impositions(impositions.stream()
                .filter(Objects::nonNull)
                .map(this::toOpal)
                .toList())
            .build();
    }

    @Named("toLocalDate")
    default LocalDate toLocalDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    @Named("toAccountType")
    default AccountTypeEnum toAccountType(String value) {
        return value == null ? null : AccountTypeEnum.fromValue(value);
    }

    @Named("toDisplayName")
    default DisplayNameEnum toDisplayName(String accountType) {
        String displayName = CreditorAccountType.getDisplayName(accountType);
        return displayName == null ? null : DisplayNameEnum.fromValue(displayName);
    }
}
