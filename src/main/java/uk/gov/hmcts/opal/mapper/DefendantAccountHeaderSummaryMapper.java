package uk.gov.hmcts.opal.mapper;

import static uk.gov.hmcts.opal.util.AgeUtil.calculateAge;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Period;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountType;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon.AccountStatusCodeEnum;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response.AccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response.DebtorTypeEnum;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PaymentStateSummaryCommon;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountHeaderSummaryMapper {

    @Mappings({
        @Mapping(target = "response.defendantAccountId"),
        @Mapping(target = "response.debtorType", source = "."),
        @Mapping(target = "response.isYouth", source = "birthDate"),
        @Mapping(target = "response.parentGuardianPartyId", source = "parentGuardianAccountPartyId"),
        @Mapping(target = "response.accountStatusReference", source = "accountStatus"),
        @Mapping(target = "response.businessUnitSummary", source = "."),
        @Mapping(target = "response.paymentStateSummary", source = "."),
        @Mapping(target = "response.partyDetails", source = "."),
        @Mapping(target = "response.accountNumber", source = "entity.accountNumber"),
        @Mapping(target = "response.accountType", source = "entity.accountType"),
        @Mapping(target = "response.defendantPartyId", source = "entity.defendantAccountPartyId"),
        @Mapping(target = "response.fixedPenaltyTicketNumber", source = "entity.fixedPenaltyTicketNumber"),
        @Mapping(target = "response.prosecutorCaseReference", source = "entity.prosecutorCaseReference"),
        @Mapping(target = "response.hasConsolidatedAccounts"),
    })
    DefendantAccountHeaderSummary toDto(DefendantAccountHeaderViewEntity entity);

    @Mapping(target = "welshSpeaking", constant = "N")
    BusinessUnitSummaryCommon toBusinessUnitSummary(DefendantAccountHeaderViewEntity entity);

    @Mapping(target = "imposedAmount", source = "imposed")
    @Mapping(target = "arrearsAmount", source = "arrears")
    @Mapping(target = "paidAmount", source = "paid")
    PaymentStateSummaryCommon toPaymentStateSummary(DefendantAccountHeaderViewEntity entity);

    default AccountStatusReferenceCommon toAccountStatusReference(DefendantAccountStatus status) {
        if (status == null) {
            return null;
        }

        return AccountStatusReferenceCommon.builder()
            .accountStatusCode(AccountStatusCodeEnum.fromValue(status.getLabel()))
            .accountStatusDisplayName(status.getDisplayName())
            .build();
    }

    default PartyDetailsCommon toPartyDetails(DefendantAccountHeaderViewEntity entity) {
        boolean isOrganisation = Boolean.TRUE.equals(entity.getOrganisation());

        return PartyDetailsCommon.builder()
            .partyId(entity.getPartyId() == null ? null : String.valueOf(entity.getPartyId()))
            .organisationFlag(entity.getOrganisation())
            .organisationDetails(
                isOrganisation
                    ? OrganisationDetailsCommon.builder()
                    .organisationName(entity.getOrganisationName())
                    .organisationAliases(null)
                    .build()
                    : null
            )
            .individualDetails(
                !isOrganisation
                    ? IndividualDetailsCommon.builder()
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

    default DebtorTypeEnum resolveDebtorType(DefendantAccountHeaderViewEntity entity) {
        if (entity.getDebtorType() != null) {
            return DebtorTypeEnum.fromValue(entity.getDebtorType());
        }
        return Boolean.TRUE.equals(entity.getHasParentGuardian()) ? DebtorTypeEnum.PARENT_GUARDIAN
            : DebtorTypeEnum.DEFENDANT;
    }

    default AccountTypeEnum toAccountTypeLabel(DefendantAccountType accountType) {
        return accountType == null ? null : AccountTypeEnum.fromValue(accountType.getLabel());
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
