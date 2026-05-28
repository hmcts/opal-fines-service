package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;
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

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DefendantAccountSearchMapper {

    @Mapping(target = "businessUnitIds", source = "businessUnitIds", qualifiedByName = "toShorts")
    @Mapping(target = "consolidationSearch", source = "consolidationSearch", qualifiedByName = "valueOrNull")
    @Mapping(target = "referenceNumberDto", source = "referenceNumber")
    AccountSearchDto toDto(PostDefendantAccountSearchRequestDefendantAccount request);

    @Mapping(target = "accountNumber", source = "accountNumber", qualifiedByName = "valueOrNull")
    @Mapping(target = "prosecutorCaseReference", source = "prosecutorCaseReference",
             qualifiedByName = "valueOrNull")
    ReferenceNumberDto toDto(DefendantAccountSearchReferenceNumberDefendantAccount referenceNumber);

    @Mapping(target = "addressLine1", source = "addressLine1", qualifiedByName = "valueOrNull")
    @Mapping(target = "postcode", source = "postcode", qualifiedByName = "valueOrNull")
    @Mapping(target = "organisationName", source = "organisationName", qualifiedByName = "valueOrNull")
    @Mapping(target = "exactMatchOrganisationName", source = "exactMatchOrganisationName",
             qualifiedByName = "valueOrNull")
    @Mapping(target = "surname", source = "surname", qualifiedByName = "valueOrNull")
    @Mapping(target = "exactMatchSurname", source = "exactMatchSurname", qualifiedByName = "valueOrNull")
    @Mapping(target = "forenames", source = "forenames", qualifiedByName = "valueOrNull")
    @Mapping(target = "exactMatchForenames", source = "exactMatchForenames", qualifiedByName = "valueOrNull")
    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "valueOrNull")
    @Mapping(target = "nationalInsuranceNumber", source = "nationalInsuranceNumber",
             qualifiedByName = "valueOrNull")
    DefendantDto toDto(DefendantAccountSearchDefendantDefendantAccount defendant);

    @Mapping(target = "defendantAccounts", source = "defendantAccounts", qualifiedByName = "toGeneratedAccounts")
    PostDefendantAccountSearchResponseDefendantAccount toResponse(DefendantAccountSearchResultsDto dto);

    @Mapping(target = "aliases", source = "aliases", qualifiedByName = "toGeneratedAliases")
    @Mapping(target = "postcode", source = "postcode", qualifiedByName = "toNullable")
    @Mapping(target = "prosecutorCaseReference", source = "prosecutorCaseReference",
             qualifiedByName = "toNullable")
    @Mapping(target = "lastEnforcementAction", source = "lastEnforcementAction", qualifiedByName = "toNullable")
    @Mapping(target = "organisationName", source = "organisationName", qualifiedByName = "toNullable")
    @Mapping(target = "defendantTitle", source = "defendantTitle", qualifiedByName = "toNullable")
    @Mapping(target = "defendantFirstnames", source = "defendantFirstnames", qualifiedByName = "toNullable")
    @Mapping(target = "defendantSurname", source = "defendantSurname", qualifiedByName = "toNullable")
    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "toNullableLocalDate")
    @Mapping(target = "nationalInsuranceNumber", source = "nationalInsuranceNumber",
             qualifiedByName = "toNullable")
    @Mapping(target = "parentGuardianSurname", source = "parentGuardianSurname", qualifiedByName = "toNullable")
    @Mapping(target = "parentGuardianFirstnames", source = "parentGuardianFirstnames",
             qualifiedByName = "toNullable")
    @Mapping(target = "hasCollectionOrder", source = "hasCollectionOrder",
             qualifiedByName = "toOptionalNullable")
    @Mapping(target = "accountVersion", source = "accountVersion", qualifiedByName = "toOptionalNullableInteger")
    @Mapping(target = "checks", source = "checks", qualifiedByName = "toOptionalNullableChecks")
    DefendantAccountSearchResultDefendantAccount toGenerated(DefendantAccountSummaryDto dto);

    @Mapping(target = "organisationName", source = "organisationName", qualifiedByName = "toNullable")
    @Mapping(target = "surname", source = "surname", qualifiedByName = "toNullable")
    @Mapping(target = "forenames", source = "forenames", qualifiedByName = "toNullable")
    DefendantAccountSearchAliasDefendantAccount toGenerated(AliasDto alias);

    @Mapping(target = "warnings", source = "warnings", qualifiedByName = "toGeneratedChecks")
    @Mapping(target = "errors", source = "errors", qualifiedByName = "toGeneratedChecks")
    DefendantAccountSearchChecksDefendantAccount toGenerated(Checks checks);

    DefendantAccountSearchCheckDefendantAccount toGenerated(WarnError check);

    @Named("toGeneratedAccounts")
    default JsonNullable<List<DefendantAccountSearchResultDefendantAccount>> toGeneratedAccounts(
        List<DefendantAccountSummaryDto> accounts) {

        return JsonNullable.of(Optional.ofNullable(accounts)
                                   .orElse(Collections.emptyList())
                                   .stream()
                                   .map(this::toGenerated)
                                   .toList());
    }

    @Named("toGeneratedAliases")
    default JsonNullable<List<DefendantAccountSearchAliasDefendantAccount>> toGeneratedAliases(
        List<AliasDto> aliases) {

        return JsonNullable.of(Optional.ofNullable(aliases)
                                   .orElse(Collections.emptyList())
                                   .stream()
                                   .map(this::toGenerated)
                                   .toList());
    }

    @Named("toGeneratedChecks")
    default List<DefendantAccountSearchCheckDefendantAccount> toGeneratedChecks(List<WarnError> checks) {
        return Optional.ofNullable(checks)
            .orElse(Collections.emptyList())
            .stream()
            .map(this::toGenerated)
            .toList();
    }

    @Named("toNullable")
    default <T> JsonNullable<T> toNullable(T value) {
        return JsonNullable.of(value);
    }

    @Named("toNullableLocalDate")
    default JsonNullable<LocalDate> toNullableLocalDate(String value) {
        return JsonNullable.of(value == null || value.isBlank() ? null : LocalDate.parse(value));
    }

    @Named("toOptionalNullable")
    default <T> JsonNullable<T> toOptionalNullable(T value) {
        return value == null ? JsonNullable.undefined() : JsonNullable.of(value);
    }

    @Named("toOptionalNullableInteger")
    default JsonNullable<Integer> toOptionalNullableInteger(BigInteger value) {
        return value == null ? JsonNullable.undefined() : JsonNullable.of(value.intValue());
    }

    @Named("toOptionalNullableChecks")
    default JsonNullable<DefendantAccountSearchChecksDefendantAccount> toOptionalNullableChecks(Checks checks) {
        return checks == null ? JsonNullable.undefined() : JsonNullable.of(toGenerated(checks));
    }

    @Named("toShorts")
    default List<Short> toShorts(JsonNullable<List<Integer>> values) {
        List<Integer> unwrappedValues = valueOrNull(values);
        return unwrappedValues == null ? null : unwrappedValues.stream()
            .map(value -> value == null ? null : value.shortValue())
            .toList();
    }

    @Named("valueOrNull")
    default <T> T valueOrNull(JsonNullable<T> value) {
        return value != null && value.isPresent() ? value.get() : null;
    }
}
