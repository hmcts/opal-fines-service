package uk.gov.hmcts.opal.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountType;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountImpositionData;
import uk.gov.hmcts.opal.generated.model.CompanyNameCommon;
import uk.gov.hmcts.opal.generated.model.CourtReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorSummaryCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.IndividualNameCommon;
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

    default CreditorSummaryCommon toCreditorReference(DefendantAccountImpositionData imposition) {
        if (imposition == null) {
            return null;
        }
        return new CreditorSummaryCommon()
            .creditorAccountType(toCreditorAccountTypeReference(imposition.creditorAccountType()))
            .creditorAccountId(imposition.creditorAccountId())
            .majorCreditorName(toMajorCreditorName(imposition))
            .minorCreditorOrganisationFlag(toMinorCreditorOrganisationFlag(imposition))
            .individualName(toIndividualName(imposition))
            .companyName(toCompanyName(imposition));
    }

    default BigDecimal toBalance(DefendantAccountImpositionData imposition) {
        if (imposition == null || imposition.imposedAmount() == null) {
            return null;
        }
        BigDecimal paidAmount = imposition.paidAmount() == null ? BigDecimal.ZERO : imposition.paidAmount();
        if (imposition.imposedAmount().compareTo(BigDecimal.ZERO) > 0) {
            return imposition.imposedAmount().subtract(paidAmount);
        }
        return imposition.imposedAmount().add(paidAmount);
    }

    default OffenceReferenceCommon toOffenceReference(DefendantAccountImpositionData imposition) {
        if (imposition == null) {
            return null;
        }
        return new OffenceReferenceCommon()
            .offenceId(imposition.offenceId())
            .cjsCode(firstNonBlank(imposition.impositionOffenceCode(), imposition.offenceCode()))
            .offenceTitle(firstNonBlank(imposition.impositionOffenceTitle(), imposition.offenceTitle()));
    }

    default CourtReferenceCommon toImposedByReference(DefendantAccountImpositionData imposition) {
        if (imposition == null
            || imposition.imposingCourtId() == null
            || imposition.defendantAccountType() != DefendantAccountType.FINES) {
            return null;
        }
        return new CourtReferenceCommon()
            .courtId(imposition.imposingCourtId())
            .courtCode(imposition.imposingCourtCode())
            .courtName(imposition.imposingCourtName());
    }

    private CreditorAccountTypeReferenceCommon toCreditorAccountTypeReference(CreditorAccountType creditorAccountType) {
        if (creditorAccountType == null) {
            return null;
        }
        return new CreditorAccountTypeReferenceCommon()
            .accountType(CreditorAccountTypeReferenceCommon.AccountTypeEnum.fromValue(creditorAccountType.name()))
            .displayName(CreditorAccountTypeReferenceCommon.DisplayNameEnum.fromValue(creditorAccountType.getLabel()));
    }

    private String toMajorCreditorName(DefendantAccountImpositionData imposition) {
        if (imposition.creditorAccountType() != CreditorAccountType.MJ) {
            return null;
        }
        return trimToNull(imposition.majorCreditorName());
    }

    private Boolean toMinorCreditorOrganisationFlag(DefendantAccountImpositionData imposition) {
        if (imposition.creditorAccountType() != CreditorAccountType.MN) {
            return null;
        }
        return Boolean.TRUE.equals(imposition.minorCreditorOrganisation());
    }

    private IndividualNameCommon toIndividualName(DefendantAccountImpositionData imposition) {
        if (imposition.creditorAccountType() != CreditorAccountType.MN
            || Boolean.TRUE.equals(imposition.minorCreditorOrganisation())
            || !hasText(imposition.minorCreditorSurname())) {
            return null;
        }
        return new IndividualNameCommon()
            .forenames(trimToNull(imposition.minorCreditorForenames()))
            .surname(imposition.minorCreditorSurname().trim());
    }

    private CompanyNameCommon toCompanyName(DefendantAccountImpositionData imposition) {
        if (imposition.creditorAccountType() == null) {
            return null;
        }
        String organisationName = switch (imposition.creditorAccountType()) {
            case MN -> Boolean.TRUE.equals(imposition.minorCreditorOrganisation())
                ? imposition.minorCreditorOrganisationName()
                : null;
            case CF -> firstNonBlank(imposition.majorCreditorName(), CreditorAccountType.CF.getLabel());
            case MJ -> null;
        };
        String trimmedOrganisationName = trimToNull(organisationName);
        if (trimmedOrganisationName == null) {
            return null;
        }
        return new CompanyNameCommon().organisationName(trimmedOrganisationName);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
