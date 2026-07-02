package uk.gov.hmcts.opal.mapper;

import static uk.gov.hmcts.opal.util.AgeUtil.calculateAge;

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
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountType;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DefendantAccountHeaderSummaryMapper {

    @Mapping(target = "debtorType", source = ".")
    @Mapping(target = "isYouth", source = "birthDate")
    @Mapping(target = "parentGuardianPartyId", source = "parentGuardianAccountPartyId")
    @Mapping(target = "accountStatusReference", source = "accountStatus")
    @Mapping(target = "businessUnitSummary", source = ".")
    @Mapping(target = "paymentStateSummary", source = ".")
    @Mapping(target = "partyDetails", source = ".")
    DefendantAccountHeaderSummary toDto(DefendantAccountHeaderViewEntity entity);

    @Mapping(target = "welshSpeaking", constant = "N")
    BusinessUnitSummary toBusinessUnitSummary(DefendantAccountHeaderViewEntity entity);

    @Mapping(target = "imposedAmount", source = "imposed")
    @Mapping(target = "arrearsAmount", source = "arrears")
    @Mapping(target = "paidAmount", source = "paid")
    PaymentStateSummary toPaymentStateSummary(DefendantAccountHeaderViewEntity entity);

    default AccountStatusReference toAccountStatusReference(DefendantAccountStatus status) {
        if (status == null) {
            return null;
        }
        return AccountStatusReference.builder()
            .accountStatusCode(status.getCode())
            .accountStatusDisplayName(status.getDisplayName())
            .build();
    }

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

    default String toAccountTypeLabel(DefendantAccountType accountType) {
        return accountType == null ? null : accountType.getLabel();
    }

    default BigDecimal toZeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    default boolean isYouth(LocalDate birthDate) {
        return birthDate != null && Period.between(birthDate, LocalDate.now()).getYears() < 18;
    }

    default BigInteger toVersion(Long version) {
        return version == null ? null : BigInteger.valueOf(version);
    }

}
