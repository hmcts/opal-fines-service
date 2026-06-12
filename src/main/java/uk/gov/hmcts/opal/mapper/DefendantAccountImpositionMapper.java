package uk.gov.hmcts.opal.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountType;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountImpositionData;
import uk.gov.hmcts.opal.generated.model.CourtReferenceCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon;
import uk.gov.hmcts.opal.generated.model.OffenceReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ResultReferenceCommon;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DefendantAccountImpositionMapper {

    default DefendantAccountImpositionsResponseCommon toResponse(List<DefendantAccountImpositionData> impositions) {
        return new DefendantAccountImpositionsResponseCommon()
            .impositions(impositions == null ? List.of() : toImpositions(impositions));
    }

    List<DefendantAccountImpositionCommon> toImpositions(List<DefendantAccountImpositionData> impositions);

    @Mapping(target = "dateAdded", source = "postedDate", qualifiedByName = "toLocalDate")
    @Mapping(target = "dateImposed", source = "imposedDate", qualifiedByName = "toLocalDate")
    @Mapping(target = "imposition", ignore = true)
    @Mapping(target = "creditor", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "offence", ignore = true)
    @Mapping(target = "imposedBy", ignore = true)
    DefendantAccountImpositionCommon toImposition(DefendantAccountImpositionData imposition);

    @AfterMapping
    default void mapDerivedFields(
        DefendantAccountImpositionData imposition,
        @MappingTarget DefendantAccountImpositionCommon target
    ) {
        target.setImposition(toResultReference(imposition));
        target.setCreditor(toCreditorReference(imposition));
        target.setBalance(toBalance(imposition));
        target.setOffence(toOffenceReference(imposition));
        target.setImposedBy(toImposedByReference(imposition));
    }

    @Named("toLocalDate")
    default LocalDate toLocalDate(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toLocalDate();
    }

    default ResultReferenceCommon toResultReference(DefendantAccountImpositionData imposition) {
        if (imposition == null) {
            return null;
        }
        return new ResultReferenceCommon()
            .resultId(imposition.resultId())
            .resultTitle(imposition.resultTitle());
    }

    default ImpositionCreditorReferenceCommon toCreditorReference(DefendantAccountImpositionData imposition) {
        if (imposition == null) {
            return null;
        }
        return new ImpositionCreditorReferenceCommon()
            .creditorAccountId(imposition.creditorAccountId())
            .accountType(toAccountType(imposition.creditorAccountType()))
            .displayName(toDisplayName(imposition.creditorAccountType()))
            .majorCreditorId(imposition.majorCreditorId())
            .minorCreditorPartyId(imposition.minorCreditorPartyId())
            .name(toCreditorName(imposition));
    }

    default BigDecimal toBalance(DefendantAccountImpositionData imposition) {
        if (imposition == null || imposition.imposedAmount() == null) {
            return null;
        }
        BigDecimal paidAmount = imposition.paidAmount() == null ? BigDecimal.ZERO : imposition.paidAmount();
        return imposition.imposedAmount().subtract(paidAmount);
    }

    default OffenceReferenceCommon toOffenceReference(DefendantAccountImpositionData imposition) {
        if (imposition == null) {
            return null;
        }
        return new OffenceReferenceCommon()
            .id(imposition.offenceId())
            .code(firstNonBlank(imposition.impositionOffenceCode(), imposition.offenceCode()))
            .title(firstNonBlank(imposition.impositionOffenceTitle(), imposition.offenceTitle()));
    }

    default CourtReferenceCommon toImposedByReference(DefendantAccountImpositionData imposition) {
        if (imposition == null
            || imposition.imposingCourtId() == null
            || imposition.defendantAccountType() != DefendantAccountType.FINES) {
            return null;
        }
        return new CourtReferenceCommon()
            .courtId(imposition.imposingCourtId())
            .courtCode(toInteger(imposition.imposingCourtCode()))
            .courtName(imposition.imposingCourtName());
    }

    private ImpositionCreditorReferenceCommon.AccountTypeEnum toAccountType(CreditorAccountType creditorAccountType) {
        return creditorAccountType == null
            ? null
            : ImpositionCreditorReferenceCommon.AccountTypeEnum.fromValue(creditorAccountType.name());
    }

    private ImpositionCreditorReferenceCommon.DisplayNameEnum toDisplayName(CreditorAccountType creditorAccountType) {
        return creditorAccountType == null
            ? null
            : ImpositionCreditorReferenceCommon.DisplayNameEnum.fromValue(creditorAccountType.getLabel());
    }

    private String toCreditorName(DefendantAccountImpositionData imposition) {
        if (imposition.creditorAccountType() == null) {
            return null;
        }
        return switch (imposition.creditorAccountType()) {
            case MJ -> imposition.majorCreditorName();
            case MN -> toMinorCreditorName(imposition);
            case CF -> firstNonBlank(imposition.majorCreditorName(), CreditorAccountType.CF.getLabel());
        };
    }

    private String toMinorCreditorName(DefendantAccountImpositionData imposition) {
        if (Boolean.TRUE.equals(imposition.minorCreditorOrganisation())) {
            return firstNonBlank(imposition.minorCreditorOrganisationName());
        }
        String individualName = Stream.of(
                imposition.minorCreditorTitle(),
                imposition.minorCreditorForenames(),
                imposition.minorCreditorSurname()
            )
            .filter(this::hasText)
            .map(String::trim)
            .collect(Collectors.joining(" "));
        return firstNonBlank(individualName, imposition.minorCreditorOrganisationName());
    }

    private Integer toInteger(Short value) {
        return value == null ? null : value.intValue();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
