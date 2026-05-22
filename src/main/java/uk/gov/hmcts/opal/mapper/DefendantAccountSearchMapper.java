package uk.gov.hmcts.opal.mapper;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.Checks;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.WarnError;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchAliasDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchCheckDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchChecksDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchDefendantDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchReferenceNumberDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchResultDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class DefendantAccountSearchMapper {

    public AccountSearchDto toDto(PostDefendantAccountSearchRequestDefendantAccount request) {
        if (request == null) {
            return null;
        }

        return AccountSearchDto.builder()
            .activeAccountsOnly(request.getActiveAccountsOnly())
            .businessUnitIds(toShorts(valueOrNull(request.getBusinessUnitIds())))
            .consolidationSearch(valueOrNull(request.getConsolidationSearch()))
            .referenceNumberDto(toDto(request.getReferenceNumber()))
            .defendant(toDto(request.getDefendant()))
            .build();
    }

    private ReferenceNumberDto toDto(DefendantAccountSearchReferenceNumberDefendantAccount referenceNumber) {
        if (referenceNumber == null) {
            return null;
        }

        return ReferenceNumberDto.builder()
            .organisation(referenceNumber.getOrganisation())
            .accountNumber(valueOrNull(referenceNumber.getAccountNumber()))
            .prosecutorCaseReference(valueOrNull(referenceNumber.getProsecutorCaseReference()))
            .build();
    }

    private DefendantDto toDto(DefendantAccountSearchDefendantDefendantAccount defendant) {
        if (defendant == null) {
            return null;
        }

        return DefendantDto.builder()
            .includeAliases(defendant.getIncludeAliases())
            .organisation(defendant.getOrganisation())
            .addressLine1(valueOrNull(defendant.getAddressLine1()))
            .postcode(valueOrNull(defendant.getPostcode()))
            .organisationName(valueOrNull(defendant.getOrganisationName()))
            .exactMatchOrganisationName(valueOrNull(defendant.getExactMatchOrganisationName()))
            .surname(valueOrNull(defendant.getSurname()))
            .exactMatchSurname(valueOrNull(defendant.getExactMatchSurname()))
            .forenames(valueOrNull(defendant.getForenames()))
            .exactMatchForenames(valueOrNull(defendant.getExactMatchForenames()))
            .birthDate(valueOrNull(defendant.getBirthDate()))
            .nationalInsuranceNumber(valueOrNull(defendant.getNationalInsuranceNumber()))
            .build();
    }

    public PostDefendantAccountSearchResponseDefendantAccount toResponse(DefendantAccountSearchResultsDto dto) {
        if (dto == null) {
            return null;
        }

        List<DefendantAccountSearchResultDefendantAccount> accounts = Optional
            .ofNullable(dto.getDefendantAccounts())
            .orElse(Collections.emptyList())
            .stream()
            .map(this::toGenerated)
            .toList();

        return PostDefendantAccountSearchResponseDefendantAccount.builder()
            .count(dto.getCount())
            .defendantAccounts(accounts)
            .build();
    }

    private DefendantAccountSearchResultDefendantAccount toGenerated(DefendantAccountSummaryDto dto) {
        DefendantAccountSearchResultDefendantAccount account = DefendantAccountSearchResultDefendantAccount.builder()
            .defendantAccountId(dto.getDefendantAccountId())
            .accountNumber(dto.getAccountNumber())
            .organisation(dto.getOrganisation())
            .aliases(toGeneratedAliases(dto.getAliases()))
            .addressLine1(dto.getAddressLine1())
            .postcode(dto.getPostcode())
            .businessUnitName(dto.getBusinessUnitName())
            .businessUnitId(dto.getBusinessUnitId())
            .prosecutorCaseReference(dto.getProsecutorCaseReference())
            .lastEnforcementAction(dto.getLastEnforcementAction())
            .accountBalance(dto.getAccountBalance())
            .organisationName(dto.getOrganisationName())
            .defendantTitle(dto.getDefendantTitle())
            .defendantFirstnames(dto.getDefendantFirstnames())
            .defendantSurname(dto.getDefendantSurname())
            .birthDate(toLocalDate(dto.getBirthDate()))
            .nationalInsuranceNumber(dto.getNationalInsuranceNumber())
            .parentGuardianSurname(dto.getParentGuardianSurname())
            .parentGuardianFirstnames(dto.getParentGuardianFirstnames())
            .build();

        if (dto.getHasCollectionOrder() != null) {
            account.hasCollectionOrder(dto.getHasCollectionOrder());
        }
        if (dto.getAccountVersion() != null) {
            account.accountVersion(toInteger(dto.getAccountVersion()));
        }
        if (dto.getChecks() != null) {
            account.checks(toGenerated(dto.getChecks()));
        }

        return account;
    }

    private DefendantAccountSearchAliasDefendantAccount toGenerated(AliasDto alias) {
        return DefendantAccountSearchAliasDefendantAccount.builder()
            .aliasNumber(alias.getAliasNumber())
            .organisationName(alias.getOrganisationName())
            .surname(alias.getSurname())
            .forenames(alias.getForenames())
            .build();
    }

    private DefendantAccountSearchChecksDefendantAccount toGenerated(Checks checks) {
        return DefendantAccountSearchChecksDefendantAccount.builder()
            .warnings(toGeneratedChecks(checks.getWarnings()))
            .errors(toGeneratedChecks(checks.getErrors()))
            .build();
    }

    private DefendantAccountSearchCheckDefendantAccount toGenerated(WarnError check) {
        return DefendantAccountSearchCheckDefendantAccount.builder()
            .reference(check.getReference())
            .message(check.getMessage())
            .build();
    }

    private List<DefendantAccountSearchAliasDefendantAccount> toGeneratedAliases(List<AliasDto> aliases) {
        return Optional.ofNullable(aliases)
            .orElse(Collections.emptyList())
            .stream()
            .map(this::toGenerated)
            .toList();
    }

    private List<DefendantAccountSearchCheckDefendantAccount> toGeneratedChecks(List<WarnError> checks) {
        return Optional.ofNullable(checks)
            .orElse(Collections.emptyList())
            .stream()
            .map(this::toGenerated)
            .toList();
    }

    private List<Short> toShorts(List<Integer> values) {
        return values == null ? null : values.stream()
            .map(value -> value == null ? null : value.shortValue())
            .toList();
    }

    private LocalDate toLocalDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private Integer toInteger(BigInteger value) {
        return value == null ? null : value.intValue();
    }

    private <T> T valueOrNull(JsonNullable<T> value) {
        return value != null && value.isPresent() ? value.get() : null;
    }
}
