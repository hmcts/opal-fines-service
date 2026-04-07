package uk.gov.hmcts.opal.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Period;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.util.DefendantAccountStatusDisplay;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DefendantAccountHeaderSummaryMapper {

    @Mapping(target = "defendantAccountId",
        expression = "java(entity.getDefendantAccountId() == null ? null : "
            + "String.valueOf(entity.getDefendantAccountId()))")
    @Mapping(target = "defendantAccountPartyId",
        expression = "java(entity.getDefendantAccountPartyId() == null ? null : "
            + "String.valueOf(entity.getDefendantAccountPartyId()))")
    @Mapping(target = "debtorType", expression = "java(resolveDebtorType(entity))")
    @Mapping(target = "isYouth", expression = "java(isYouth(entity.getBirthDate()))")
    @Mapping(target = "parentGuardianPartyId",
        expression = "java(entity.getParentGuardianAccountPartyId() == null ? null : "
            + "String.valueOf(entity.getParentGuardianAccountPartyId()))")
    @Mapping(
        target = "accountType",
        expression = "java(entity.getAccountType() == null ? null : entity.getAccountType().getLabel())"
    )
    @Mapping(target = "accountStatusReference", source = "accountStatus")
    @Mapping(target = "businessUnitSummary", source = ".")
    @Mapping(target = "paymentStateSummary", source = ".")
    @Mapping(target = "partyDetails", source = ".")
    @Mapping(target = "version", expression = "java(toVersion(entity.getVersion()))")
    DefendantAccountHeaderSummary toDto(DefendantAccountHeaderViewEntity entity);

    default AccountStatusReference toAccountStatusReference(DefendantAccountStatus status) {
        if (status == null) {
            return null;
        }
        return AccountStatusReference.builder()
            .accountStatusCode(status.name())
            .accountStatusDisplayName(DefendantAccountStatusDisplay.toDisplayName(status))
            .build();
    }

    @Mapping(target = "businessUnitId",
        expression = "java(entity.getBusinessUnitId() == null ? null : String.valueOf(entity.getBusinessUnitId()))")
    @Mapping(target = "businessUnitName", source = "businessUnitName")
    @Mapping(target = "welshSpeaking", constant = "N")
    BusinessUnitSummary toBusinessUnitSummary(DefendantAccountHeaderViewEntity entity);

    @Mapping(target = "imposedAmount", expression = "java(nullToZero(entity.getImposed()))")
    @Mapping(target = "arrearsAmount", expression = "java(nullToZero(entity.getArrears()))")
    @Mapping(target = "paidAmount", expression = "java(nullToZero(entity.getPaid()))")
    @Mapping(target = "accountBalance", expression = "java(nullToZero(entity.getAccountBalance()))")
    PaymentStateSummary toPaymentStateSummary(DefendantAccountHeaderViewEntity entity);

    default PartyDetails toPartyDetails(DefendantAccountHeaderViewEntity entity) {
        boolean isOrganisation = Boolean.TRUE.equals(entity.getOrganisation());

        return PartyDetails.builder()
            .partyId(entity.getPartyId() == null ? null : String.valueOf(entity.getPartyId()))
            .organisationFlag(entity.getOrganisation())
            .organisationDetails(
                isOrganisation
                    ? OrganisationDetails.builder()
                    .organisationName(entity.getOrganisationName())
                    .organisationAliases(null)
                    .build()
                    : null
            )
            .individualDetails(
                !isOrganisation
                    ? IndividualDetails.builder()
                    .title(entity.getTitle())
                    .forenames(entity.getFirstnames())
                    .surname(entity.getSurname())
                    .dateOfBirth(entity.getBirthDate() != null ? entity.getBirthDate().toString() : null)
                    .age(entity.getBirthDate() != null ? String.valueOf(calculateAge(entity.getBirthDate())) : null)
                    .nationalInsuranceNumber(null)
                    .individualAliases(null)
                    .build()
                    : null
            )
            .build();
    }

    default String resolveDebtorType(DefendantAccountHeaderViewEntity entity) {
        if (entity.getDebtorType() != null) {
            return entity.getDebtorType();
        }
        return Boolean.TRUE.equals(entity.getHasParentGuardian()) ? "Parent/Guardian" : "Defendant";
    }

    default boolean isYouth(LocalDate birthDate) {
        return birthDate != null && Period.between(birthDate, LocalDate.now()).getYears() < 18;
    }

    default BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    default BigInteger toVersion(Long version) {
        return version == null ? null : BigInteger.valueOf(version);
    }

    default int calculateAge(LocalDate dateOfBirth) {
        return dateOfBirth == null ? 0 : Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
