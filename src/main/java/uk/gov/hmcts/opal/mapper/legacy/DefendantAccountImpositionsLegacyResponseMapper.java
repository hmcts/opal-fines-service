package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigDecimal;
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
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReferenceCommon.CreditorAccountDisplayNameEnum;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReferenceCommon.CreditorAccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.CreditorSummaryCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.OffenceReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ResultReferenceCommon;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountImpositionsLegacyResponseMapper {

    @Mapping(target = "payload", source = "impositions", qualifiedByName = "toPayload")
    GetDefendantAccountImpositionsResponse toOpal(GetDefendantAccountImpositionsLegacyResponse legacy);

    @Mapping(target = "dateAdded", source = "postedDetails.postedDate", qualifiedByName = "toLocalDate")
    @Mapping(target = "imposition", source = "result")
    @Mapping(target = "imposedAmount", source = "imposedAmount", qualifiedByName = "toPositiveAmount")
    @Mapping(target = "imposedBy", ignore = true)
    DefendantAccountImpositionCommon toOpal(Imposition legacy);

    ResultReferenceCommon toOpal(GetDefendantAccountImpositionsLegacyResponse.Result legacy);

    @Mapping(target = "creditorAccountTypeReference", source = ".")
    CreditorSummaryCommon toOpal(Creditor legacy);

    @Mapping(target = "offenceId", source = "offenceId")
    @Mapping(target = "cjsCode", source = "cjsCode")
    @Mapping(target = "offenceTitle", source = "offenceTitle")
    OffenceReferenceCommon toOpal(Offence legacy);

    @Mapping(target = "creditorAccountType", source = "creditorAccountType.creditorAccountType",
        qualifiedByName = "toAccountType")
    @Mapping(target = "creditorAccountDisplayName", source = "creditorAccountType.creditorAccountType",
        qualifiedByName = "toDisplayName")
    CreditorAccountTypeReferenceCommon toCreditorAccountTypeReference(Creditor legacy);

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

    @Named("toPositiveAmount")
    default BigDecimal toPositiveAmount(BigDecimal value) {
        return value == null ? null : value.abs();
    }

    @Named("toAccountType")
    default CreditorAccountTypeEnum toAccountType(String value) {
        return value == null ? null : CreditorAccountTypeEnum.fromValue(value);
    }

    @Named("toDisplayName")
    default CreditorAccountDisplayNameEnum toDisplayName(String accountType) {
        String displayName = CreditorAccountType.getDisplayName(accountType);
        return displayName == null ? null : CreditorAccountDisplayNameEnum.fromValue(displayName);
    }
}
