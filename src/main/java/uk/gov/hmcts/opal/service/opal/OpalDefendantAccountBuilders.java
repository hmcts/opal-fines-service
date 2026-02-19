package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.AddressDetails.AddressDetailsBuilder;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.EmployerDetails.EmployerDetailsBuilder;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.common.EnforcementOverrideResult;
import uk.gov.hmcts.opal.dto.common.EnforcementStatusSummary;
import uk.gov.hmcts.opal.dto.common.Enforcer;
import uk.gov.hmcts.opal.dto.common.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.LJA;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.LastEnforcementAction;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.dto.common.VehicleDetails.VehicleDetailsBuilder;
import uk.gov.hmcts.opal.dto.common.VehicleFixedPenaltyDetails;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity.Lite;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon.AccountStatusCodeEnum;
import uk.gov.hmcts.opal.generated.model.CollectionOrderCommon;
import uk.gov.hmcts.opal.generated.model.CourtReferenceCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementActionDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideResultReferenceCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverviewDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcerReferenceCommon;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponseDefendantAccount.DefendantAccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.LjaReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ResultReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ResultResponsesCommon;
import uk.gov.hmcts.opal.util.DateTimeUtils;

public class OpalDefendantAccountBuilders {

    static PaymentStateSummary buildPaymentStateSummary(DefendantAccountHeaderViewEntity e) {
        return PaymentStateSummary.builder()
            .imposedAmount(nz(e.getImposed()))
            .arrearsAmount(nz(e.getArrears()))
            .paidAmount(nz(e.getPaid()))
            .accountBalance(nz(e.getAccountBalance()))
            .build();
    }

    static PartyDetails buildPartyDetails(DefendantAccountHeaderViewEntity e) {
        boolean isOrganisation = Boolean.TRUE.equals(e.getOrganisation());

        return PartyDetails.builder()
            .partyId(
                e.getPartyId() != null ? e.getPartyId().toString() : null
            )
            .organisationFlag(e.getOrganisation())

            .organisationDetails(
                isOrganisation
                    ? OrganisationDetails.builder()
                    .organisationName(e.getOrganisationName())
                    .organisationAliases(null)
                    .build()
                    : null
            )

            .individualDetails(
                !isOrganisation
                    ? IndividualDetails.builder()
                    .title(e.getTitle())
                    .forenames(e.getFirstnames())
                    .surname(e.getSurname())
                    .dateOfBirth(e.getBirthDate() != null ? e.getBirthDate().toString() : null)
                    .age(e.getBirthDate() != null ? String.valueOf(calculateAge(e.getBirthDate())) : null)
                    .nationalInsuranceNumber(null)
                    .individualAliases(null)
                    .build()
                    : null
            )
            .build();
    }

    static PartyDetails buildPartyDetails(PartyEntity party, List<AliasEntity> aliases) {
        List<OrganisationAlias> organisationAliases =
            buildOrganisationAliasesFromEntities(aliases);
        List<IndividualAlias> individualAliases =
            buildIndividualAliasesFromEntities(aliases);

        return PartyDetails.builder()
            .partyId(String.valueOf(party.getPartyId()))
            .organisationFlag(party.isOrganisation())
            .organisationDetails(
                party.isOrganisation()
                    ? OrganisationDetails.builder()
                    .organisationName(party.getOrganisationName())
                    .organisationAliases(organisationAliases.isEmpty() ? null : organisationAliases)
                    .build()
                    : null
            )
            .individualDetails(
                !party.isOrganisation()
                    ? IndividualDetails.builder()
                    .title(party.getTitle())
                    .forenames(party.getForenames())
                    .surname(party.getSurname())
                    .dateOfBirth(String.valueOf(party.getBirthDate()))
                    .age(String.valueOf(party.getAge()))
                    .nationalInsuranceNumber(party.getNiNumber())
                    .individualAliases(individualAliases.isEmpty() ? null : individualAliases)
                    .build()
                    : null
            )
            .build();
    }

    static PartyDetails buildPartyDetails(DefendantAccountSummaryViewEntity entity) {
        return PartyDetails.builder()
            .partyId(String.valueOf(entity.getPartyId()))
            .organisationFlag(entity.getOrganisation())
            // Only one of organisationDetails or individualDetails will be populated
            // if organisationFlag is true, then organisationDetails is populated
            .organisationDetails(
                entity.getOrganisation()
                    ? buildOrganisationDetails(entity)
                    : null
            )
            // if organisationFlag is false, then individualDetails is populated
            .individualDetails(
                !entity.getOrganisation()
                    ? buildIndividualDetails(entity)
                    : null
            )
            .build();
    }

    static AccountStatusReference buildAccountStatusReference(String code) {
        return AccountStatusReference.builder()
            .accountStatusCode(code)
            .accountStatusDisplayName(resolveStatusDisplayName(code))
            .build();
    }

    static AccountStatusReferenceCommon buildAccountStatusReferenceCommon(String code) {
        return AccountStatusReferenceCommon.builder()
            .accountStatusCode(AccountStatusCodeEnum.fromValue(code))
            .accountStatusDisplayName(resolveStatusDisplayName(code))
            .build();
    }

    static BusinessUnitSummary buildBusinessUnitSummary(DefendantAccountHeaderViewEntity e) {
        return BusinessUnitSummary.builder()
            .businessUnitId(e.getBusinessUnitId() != null ? String.valueOf(e.getBusinessUnitId()) : null)
            .businessUnitName(e.getBusinessUnitName())
            .welshSpeaking("N")
            .build();
    }

    static List<AliasDto> buildSearchAliases(SearchDefendantAccount e) {
        boolean isOrganisation = Boolean.TRUE.equals(e.getOrganisation());
        String[] aliasValues = {e.getAlias1(), e.getAlias2(), e.getAlias3(), e.getAlias4(), e.getAlias5()};

        List<AliasDto> out = new ArrayList<>();
        for (int i = 0; i < aliasValues.length; i++) {
            String value = aliasValues[i];
            if (value == null || value.isBlank()) {
                continue;
            }

            AliasDto.AliasDtoBuilder b = AliasDto.builder().aliasNumber(i + 1);

            if (isOrganisation) {
                b.organisationName(value.trim());
            } else {
                String[] parts = splitForenamesSurname(value);
                b.forenames(parts[0]).surname(parts[1] == null ? parts[0] : parts[1]);

                if (parts[1] == null) { // TODO: Magic number
                    b.forenames(null).surname(parts[0]);
                }
            }
            out.add(b.build());
        }
        return out;
    }

    static List<OrganisationAlias> buildOrganisationAliasesFromEntities(List<AliasEntity> aliases) {
        return aliases.stream()
            .filter(a -> a.getOrganisationName() != null && !a.getOrganisationName().isBlank())
            .sorted(Comparator.comparing(AliasEntity::getSequenceNumber))
            .map(a -> OrganisationAlias.builder()
                .aliasId(String.valueOf(a.getAliasId()))
                .sequenceNumber(a.getSequenceNumber())
                .organisationName(a.getOrganisationName())
                .build())
            .toList();
    }

    static List<IndividualAlias> buildIndividualAliasesFromEntities(List<AliasEntity> aliases) {
        return aliases.stream()
            .filter(a -> a.getSurname() != null && !a.getSurname().isBlank())
            .sorted(Comparator.comparing(AliasEntity::getSequenceNumber))
            .map(a -> IndividualAlias.builder()
                .aliasId(String.valueOf(a.getAliasId()))
                .sequenceNumber(a.getSequenceNumber())
                .surname(a.getSurname())
                .forenames(a.getForenames())
                .build())
            .toList();
    }

    // ---- public-ish helpers your builders can call ----
    static List<IndividualAlias> buildIndividualAliasesList(DefendantAccountSummaryViewEntity e) {
        // If the entity is an organisation, there should be no individual aliases to emit.
        if (Boolean.TRUE.equals(e.getOrganisation())) {
            return List.of();
        }

        return streamAliasSlots(e)
            .map(raw -> parseAliasRaw(raw, /* isOrganisation= */ false)) // Optional<ParsedAlias>
            .flatMap(Optional::stream)
            .map(pa -> IndividualAlias.builder()
                .aliasId(pa.aliasId())
                .sequenceNumber(pa.sequenceNumber())
                .forenames(pa.forenames())
                .surname(pa.surname())
                .build())
            .toList();
    }

    static List<OrganisationAlias> buildOrganisationAliasesList(DefendantAccountSummaryViewEntity e) {
        // If the entity is an individual, there should be no organisation aliases to emit.
        if (!Boolean.TRUE.equals(e.getOrganisation())) {
            return List.of();
        }

        return streamAliasSlots(e)
            .map(raw -> parseAliasRaw(raw, /* isOrganisation= */ true)) // Optional<ParsedAlias>
            .flatMap(Optional::stream)
            .map(pa -> OrganisationAlias.builder()
                .aliasId(pa.aliasId())
                .sequenceNumber(pa.sequenceNumber())
                .organisationName(pa.organisationName())
                .build())
            .toList();
    }

    // ---- unchanged ----
    static IndividualDetails buildIndividualDetails(DefendantAccountSummaryViewEntity e) {
        return IndividualDetails.builder()
            .title(e.getTitle())
            .forenames(e.getForenames())
            .surname(e.getSurname())
            .dateOfBirth(e.getBirthDate() != null ? e.getBirthDate().toLocalDate().toString() : null)
            .age(e.getBirthDate() != null ? String.valueOf(calculateAge(e.getBirthDate().toLocalDate())) : null)
            .individualAliases(buildIndividualAliasesList(e))
            .nationalInsuranceNumber(e.getNationalInsuranceNumber())
            .build();
    }

    static OrganisationDetails buildOrganisationDetails(DefendantAccountSummaryViewEntity e) {
        return OrganisationDetails.builder()
            .organisationName(e.getOrganisationName())
            .organisationAliases(buildOrganisationAliasesList(e))
            .build();
    }

    static EnforcementStatusSummary buildEnforcementStatusSummary(DefendantAccountSummaryViewEntity entity) {
        LastEnforcementAction lastEnforcementAction = LastEnforcementAction.builder()
            .lastEnforcementActionId(entity.getLastEnforcement())
            .lastEnforcementActionTitle(entity.getLastEnforcementTitle())
            .build();

        EnforcementOverrideResult enforcementOverrideResult = EnforcementOverrideResult.builder()
            .enforcementOverrideId(entity.getEnforcementOverrideResultId())
            .enforcementOverrideTitle(entity.getEnforcementOverrideTitle())
            .build();

        Enforcer enforcer = (entity.getEnforcerId() != null)
            ? Enforcer.builder()
            .enforcerId(entity.getEnforcerId())
            .enforcerName(entity.getEnforcerName())
            .build()
            : null;

        LJA lja = LJA.builder()
            .ljaId(null == entity.getLjaId() ? null : Integer.parseInt(entity.getLjaId()))
            .ljaName(entity.getLjaName())
            .build();

        EnforcementOverride enforcementOverride = EnforcementOverride.builder()
            .enforcementOverrideResult(enforcementOverrideResult)
            .enforcer(enforcer)
            .lja(lja)
            .build();

        return EnforcementStatusSummary.builder()
            .lastEnforcementAction(lastEnforcementAction)
            .collectionOrderMade(entity.getCollectionOrder())
            .defaultDaysInJail(entity.getJailDays())
            .enforcementOverride(enforcementOverride)
            .lastMovementDate(entity.getLastMovementDate().toLocalDate())
            .build();
    }

    static PaymentTermsSummary buildPaymentTerms(DefendantAccountSummaryViewEntity entity) {
        return PaymentTermsSummary.builder()
            .paymentTermsType(
                PaymentTermsType.builder()
                    .paymentTermsTypeCode(safePaymentTermsTypeCode(entity.getTermsTypeCode()))
                    .build()
            )
            .effectiveDate(null == entity.getEffectiveDate() ? null : entity.getEffectiveDate().toLocalDate())
            .instalmentPeriod(
                InstalmentPeriod.builder()
                    .instalmentPeriodCode(safeInstalmentPeriodCode(entity.getInstalmentPeriod()))
                    .build()
            )
            .lumpSumAmount(entity.getInstalmentLumpSum())
            .instalmentAmount(entity.getInstalmentAmount())
            .build();
    }

    static PaymentTerms buildPaymentTerms(PaymentTermsEntity entity, DefendantAccountEntity account) {
        return PaymentTerms.builder()
            .daysInDefault(entity.getJailDays())
            .dateDaysInDefaultImposed(account.getSuspendedCommittalDate())
            .extension(entity.getExtension())
            .reasonForExtension(entity.getReasonForExtension())
            .paymentTermsType(
                PaymentTermsType.builder()
                    .paymentTermsTypeCode(
                        safePaymentTermsTypeCode(entity.getTermsTypeCode())
                    )
                    .build()
            )
            .effectiveDate(entity.getEffectiveDate())
            .instalmentPeriod(
                InstalmentPeriod.builder()
                    .instalmentPeriodCode(
                        safeInstalmentPeriodCode(entity.getInstalmentPeriod())
                    )
                    .build()
            )
            .lumpSumAmount(entity.getInstalmentLumpSum())
            .instalmentAmount(entity.getInstalmentAmount())
            .postedDetails(PostedDetails.builder()
                .postedDate(entity.getPostedDate())
                .postedBy(entity.getPostedBy())
                .postedByName(entity.getPostedByUsername())
                .build())
            .build();
    }

    static LanguagePreferences buildLanguagePreferences(DebtorDetailEntity debtorDetail) {
        return LanguagePreferences.builder()
            .documentLanguagePreference(buildLanguagePreference(debtorDetail, DebtorDetailEntity::getDocumentLanguage))
            .hearingLanguagePreference(buildLanguagePreference(debtorDetail, DebtorDetailEntity::getHearingLanguage))
            .build();
    }

    static LanguagePreferences buildLanguagePreferences(DefendantAccountSummaryViewEntity entity) {
        // if both language preferences are not set, as they are optional objects.
        if ((null == entity.getDocumentLanguage()) && (null == entity.getHearingLanguage())) {
            return null;
        }

        LanguagePreference documentLanguagePref =
            null == entity.getDocumentLanguage() ? null : LanguagePreference.fromCode(entity.getDocumentLanguage());

        LanguagePreference hearingLanguagePref =
            null == entity.getHearingLanguage() ? null : LanguagePreference.fromCode(entity.getHearingLanguage());

        return LanguagePreferences.builder()
            .documentLanguagePreference(documentLanguagePref)
            .hearingLanguagePreference(hearingLanguagePref)
            .build();
    }

    static LanguagePreference buildLanguagePreference(DebtorDetailEntity debtorDetail,
        Function<DebtorDetailEntity, String> getter) {
        String code = debtorDetail == null ? null : getter.apply(debtorDetail);
        return LanguagePreference.fromCode(code);
    }

    static VehicleDetails buildVehicleDetails(DebtorDetailEntity debtorDetail) {
        VehicleDetailsBuilder builder = VehicleDetails.builder();
        if (debtorDetail != null) {
            builder.vehicleMakeAndModel(debtorDetail.getVehicleMake())
                .vehicleRegistration(debtorDetail.getVehicleRegistration());
        }
        return builder.build();
    }

    static ContactDetails buildContactDetails(PartyEntity party) {
        return ContactDetails.builder()
            .primaryEmailAddress(party.getPrimaryEmailAddress())
            .secondaryEmailAddress(party.getSecondaryEmailAddress())
            .mobileTelephoneNumber(party.getMobileTelephoneNumber())
            .homeTelephoneNumber(party.getHomeTelephoneNumber())
            .workTelephoneNumber(party.getWorkTelephoneNumber())
            .build();
    }

    static EmployerDetails buildEmployerDetails(DebtorDetailEntity debtorDetail) {

        AddressDetails employerAddress = OpalDefendantAccountBuilders.buildEmployerAddressDetails(debtorDetail);
        EmployerDetailsBuilder builder = EmployerDetails.builder();

        if (debtorDetail != null) {
            builder.employerName(debtorDetail.getEmployerName())
                .employerReference(debtorDetail.getEmployeeReference())
                .employerEmailAddress(debtorDetail.getEmployerEmail())
                .employerTelephoneNumber(debtorDetail.getEmployerTelephone());
        }

        return builder.employerAddress(employerAddress).build();
    }

    static AddressDetails buildEmployerAddressDetails(DebtorDetailEntity debtorDetail) {
        AddressDetailsBuilder builder = AddressDetails.builder();

        if (debtorDetail != null) {
            builder.addressLine1(debtorDetail.getEmployerAddressLine1())
                .addressLine2(debtorDetail.getEmployerAddressLine2())
                .addressLine3(debtorDetail.getEmployerAddressLine3())
                .addressLine4(debtorDetail.getEmployerAddressLine4())
                .addressLine5(debtorDetail.getEmployerAddressLine5())
                .postcode(debtorDetail.getEmployerPostcode());
        } else {
            builder.addressLine1("");
        }

        return builder.build();
    }

    static AddressDetails buildPartyAddressDetails(PartyEntity party) {
        return AddressDetails.builder()
            .addressLine1(party.getAddressLine1())
            .addressLine2(party.getAddressLine2())
            .addressLine3(party.getAddressLine3())
            .addressLine4(party.getAddressLine4())
            .addressLine5(party.getAddressLine5())
            .postcode(party.getPostcode())
            .build();
    }

    static AddressDetails buildAddress(DefendantAccountSummaryViewEntity entity) {
        return AddressDetails.builder()
            .addressLine1(entity.getAddressLine1())
            .addressLine2(entity.getAddressLine2())
            .addressLine3(entity.getAddressLine3())
            .addressLine4(entity.getAddressLine4())
            .addressLine5(entity.getAddressLine5())
            .postcode(entity.getPostcode())
            .build();
    }

    static Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Integer.valueOf(s);
    }

    static String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    static String orEmpty(String s) {
        return (s == null) ? "" : s;
    }

    static String resolveStatusDisplayName(String code) {
        return switch (code) {
            case "L" -> "Live";
            case "C" -> "Completed";
            case "TO" -> "TFO to be acknowledged";
            case "TS" -> "TFO to NI/Scotland to be acknowledged";
            case "TA" -> "TFO acknowledged";
            case "CS" -> "Account consolidated";
            case "WO" -> "Account written off";
            default -> "Unknown";
        };
    }

    static String normaliseAccountType(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.trim()) {
            case "Fines", "Fine" -> "Fine";
            case "Conditional Caution" -> "Conditional Caution";
            case "Confiscation" -> "Confiscation";
            case "Fixed Penalty", "Fixed Penalty Registration" -> "Fixed Penalty";
            default -> raw;
        };
    }

    static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    static PaymentTermsType.PaymentTermsTypeCode safePaymentTermsTypeCode(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        try {
            return PaymentTermsType.PaymentTermsTypeCode.fromValue(dbValue);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    static InstalmentPeriod.InstalmentPeriodCode safeInstalmentPeriodCode(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        try {
            return InstalmentPeriod.InstalmentPeriodCode.fromValue(dbValue);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Split a full name into forenames + surname (surname = last token).
     */
    static String[] splitForenamesSurname(String fullName) {
        if (fullName == null) {
            return new String[] {null, null};
        }
        String s = fullName.trim().replaceAll("\\s+", " ");
        int idx = s.lastIndexOf(' ');
        if (idx < 0) {
            // Single token — treat as forename only (no surname)
            return new String[] {emptyToNull(s), null};
        }
        String forenames = emptyToNull(s.substring(0, idx));
        String surname = emptyToNull(s.substring(idx + 1));
        return new String[] {forenames, surname};
    }

    // ---- core parsing (shared) ----
    static Stream<String> streamAliasSlots(DefendantAccountSummaryViewEntity e) {
        return Stream.of(e.getAlias1(), e.getAlias2(), e.getAlias3(), e.getAlias4(), e.getAlias5())
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty());
    }

    /**
     * Parse alias in the new unified shape: id|seq|name Use isOrganisation to decide whether "name" is an organisation
     * name or a personal full name.
     */
    static Optional<ParsedAlias> parseAliasRaw(String raw, boolean isOrganisation) {
        String[] parts = Arrays.stream(raw.split("\\|", -1))
            .map(String::trim)
            .toArray(String[]::new);

        try {
            if (parts.length != 3) { // TODO: Magic numbers
                return Optional.empty(); // unexpected shape
            }

            String aliasId = emptyToNull(parts[0]);
            Integer seq = parseIntOrNull(parts[1]);
            String name = emptyToNull(parts[2]);

            if (isOrganisation) {
                return Optional.of(new ParsedAlias(
                    aliasId,
                    seq,
                    null,
                    null,
                    name
                ));
            } else {
                String[] split = splitForenamesSurname(name);
                return Optional.of(new ParsedAlias(
                    aliasId,
                    seq,
                    split[0],
                    split[1],
                    null
                ));
            }
        } catch (Exception ex) {
            return Optional.empty(); // malformed data
        }
    }

    static CommentsAndNotes buildCommentsAndNotes(DefendantAccountEntity entity) {
        return CommentsAndNotes.builder()
            .accountNotesAccountComments(orEmpty(entity.getAccountComments()))
            .accountNotesFreeTextNote1(orEmpty(entity.getAccountNote1()))
            .accountNotesFreeTextNote2(orEmpty(entity.getAccountNote2()))
            .accountNotesFreeTextNote3(orEmpty(entity.getAccountNote3()))
            .build();
    }

    static CommentsAndNotes buildCommentsAndNotes(DefendantAccountSummaryViewEntity entity) {
        // Return null if all fields don't have values, as they are optional objects.
        if ((null == entity.getAccountComments()) && (null == entity.getAccountNote1())
            && (null == entity.getAccountNote2()) && (null == entity.getAccountNote3())) {
            return null;
        }

        return CommentsAndNotes.builder()
            .accountNotesAccountComments(entity.getAccountComments())
            .accountNotesFreeTextNote1(entity.getAccountNote1())
            .accountNotesFreeTextNote2(entity.getAccountNote2())
            .accountNotesFreeTextNote3(entity.getAccountNote3())
            .build();
    }

    static CourtEntity.Lite asLite(CourtEntity court) {
        return CourtEntity.Lite.builder()
            .courtId(court.getCourtId())
            .businessUnitId(court.getBusinessUnitId())
            .courtCode(court.getCourtCode())
            .localJusticeAreaId(court.getLocalJusticeAreaId())
            .courtType(court.getCourtType())
            .division(court.getDivision())
            .name(court.getName())
            .build();
    }

    static Enforcer buildEnforcer(EnforcerEntity enforcer) {
        if (enforcer == null) {
            return null;
        }
        return Enforcer.builder()
            .enforcerId(enforcer.getEnforcerId())
            .enforcerName(enforcer.getName())
            .build();
    }

    static EnforcerReferenceCommon buildEnforcer(Enforcer enforcer) {
        return Optional.ofNullable(enforcer).map(e -> EnforcerReferenceCommon.builder()
                .enforcerId(e.getEnforcerId())
                .enforcerName(e.getEnforcerName())
                .build())
            .orElse(null);
    }

    static EnforcementOverrideResult buildEnforcementOverrideResult(ResultEntity.Lite entity) {
        if (entity == null) {
            return null;
        }
        return EnforcementOverrideResult.builder()
            .enforcementOverrideId(entity.getResultId())
            .enforcementOverrideTitle(entity.getResultTitle())
            .build();
    }

    static EnforcementOverrideResultReferenceCommon buildEnforcementOverrideResultReferenceCommon(
        EnforcementOverrideResult eor) {

        return Optional.ofNullable(eor).map(o -> EnforcementOverrideResultReferenceCommon.builder()
                .enforcementOverrideResultId(o.getEnforcementOverrideId())
                .enforcementOverrideResultName(o.getEnforcementOverrideTitle())
                .build())
            .orElse(null);
    }

    static EnforcementOverrideCommon buildEnforcementOverrideCommon(EnforcementOverride original) {
        return Optional.ofNullable(original).map(o -> EnforcementOverrideCommon.builder()
                .enforcementOverrideResult(
                    buildEnforcementOverrideResultReferenceCommon(o.getEnforcementOverrideResult()))
                .enforcer(buildEnforcer(o.getEnforcer()))
                .lja(buildLja(o.getLja()))
                .build())
            .orElse(null);
    }

    static EnforcementOverviewDefendantAccount buildEnforcementOverview(DefendantAccountEntity defendantEntity) {
        return EnforcementOverviewDefendantAccount.builder()
            .collectionOrder(buildCollectionOrderCommon(defendantEntity))
            .enforcementCourt(buildCourtReferenceCommon(defendantEntity.getEnforcingCourt()))
            .daysInDefault(defendantEntity.getJailDays())
            .build();
    }

    static EnforcementStatus buildEnforcementStatus(DefendantAccountEntity defendantEntity,
        DefendantAccountPartiesEntity defendantParty, DebtorDetailEntity debtDetails,
        ResultEntity.Lite recentResult, EnforcementOverride override,
        EnforcementActionDefendantAccount enfActDefAcc) {

        return EnforcementStatus.newBuilder()
            .enforcementOverview(buildEnforcementOverview(defendantEntity))
            .enforcementOverride(buildEnforcementOverrideCommon(override))
            .lastEnforcementAction(enfActDefAcc)
            .nextEnforcementActionData(recentResult != null ? recentResult.getEnfNextPermittedActions() : null)
            .accountStatusReference(buildAccountStatusReferenceCommon(defendantEntity.getAccountStatus()))
            .defendantAccountType(determineAccountType(defendantParty))
            .employerFlag(debtDetails != null && debtDetails.getEmployerName() != null)
            .isHmrcCheckEligible(false)  // TODO: See PO-2370
            .version(defendantEntity.getVersion())
            .build();
    }

    static EnforcementActionDefendantAccount buildEnforcementAction(Lite recentEnforcement,
        EnforcerEntity recentEnforcer) {
        if (recentEnforcement == null) {
            return null;
        }

        ResultEntity.Lite recentResult = recentEnforcement.getResult();

        return EnforcementActionDefendantAccount.builder()
            .enforcementAction(ResultReferenceCommon.builder()
                .resultId(recentEnforcement.getResultId())
                .resultTitle(recentResult != null ? recentResult.getResultTitle() : null)
                .build())
            .enforcer(EnforcerReferenceCommon.builder()
                .enforcerId(recentEnforcer != null ? recentEnforcer.getEnforcerId() : null)
                .enforcerName(recentEnforcer != null ? recentEnforcer.getName() : null)
                .build())
            .dateAdded(recentEnforcement.getPostedDate())
            .reason(recentEnforcement.getReason())
            .warrantNumber(recentEnforcement.getWarrantReference())
            .resultResponses(
                buildResultResponses(
                    recentResult != null ? recentResult.getResultParameters() : null,
                    ToJsonString.toOptionalJsonNode(recentEnforcement.getResultResponses()).orElse(null)))
            .build();
    }

    static List<ResultResponsesCommon> buildResultResponses(String resultParameters, JsonNode responses) {

        return convertStringToStreamOfJsonNodes(resultParameters) // resultParameters is a String of Json
            .map(OpalDefendantAccountBuilders::getNameTextValueAsString) // Just need the 'name' value from each node
            .flatMap(Optional::stream) // A stream of response 'keys' (i.e. 'name' values) for this enforcement
            .map(key -> ResultResponsesCommon.builder()
                .parameterName(key)
                .response(getTextValueFromJsonNode(responses, key).orElse(null)) // 'Lookup' the response
                .build())
            .toList();
    }

    static Stream<JsonNode> convertStringToStreamOfJsonNodes(String json) {
        return ToJsonString.toOptionalJsonNode(json)
            .map(JsonNode::spliterator)  // A JsonNode is Iterable
            .map(s -> StreamSupport.stream(s, false))
            .orElse(Stream.empty());
    }

    static Optional<String> getNameTextValueAsString(JsonNode node) {
        return Optional.ofNullable(node.findValue("name")).map(JsonNode::textValue);
    }

    static Optional<String> getTextValueFromJsonNode(JsonNode node, String key) {
        return Optional.ofNullable(node).map(n -> n.findValue(key)).map(JsonNode::asText);
    }

    static LJA buildLja(LocalJusticeAreaEntity entity) {
        if (entity == null) {
            return null;
        }
        return LJA.builder()
            .ljaId(Optional.ofNullable(entity.getLocalJusticeAreaId()).map(Short::intValue).orElse(null))
            .ljaCode(entity.getLjaCode())
            .ljaName(Optional.ofNullable(entity.getName()).orElse(entity.getLjaCode()))
            .build();
    }

    static LjaReferenceCommon buildLja(LJA lja) {
        return Optional.ofNullable(lja).map(l -> LjaReferenceCommon.builder()
                .ljaId(l.getLjaId())
                .ljaCode(l.getLjaCode())
                .ljaName(l.getLjaName())
                .build())
            .orElse(null);
    }

    static CollectionOrderCommon buildCollectionOrderCommon(DefendantAccountEntity entity) {
        return CollectionOrderCommon.builder()
            .collectionOrderFlag(entity.getCollectionOrder())
            .collectionOrderDate(entity.getCollectionOrderEffectiveDate())
            .build();
    }

    static DefendantAccountResponse.EnforcementCourtResponse buildUpdateEnforcementCourt(CourtEntity.Lite court) {
        return Optional.ofNullable(court)
            .map(c -> DefendantAccountResponse.EnforcementCourtResponse.builder()
                .enforcingCourtId(c.getCourtId())
                .courtName(c.getName())
                .build())
            .orElse(null);
    }

    static DefendantAccountResponse.CollectionOrderResponse buildUpdateCollectionOrder(DefendantAccountEntity entity) {
        return DefendantAccountResponse.CollectionOrderResponse.builder()
            .collectionOrder(entity.getCollectionOrder())
            .collectionOrderDate(entity.getCollectionOrderEffectiveDate() != null
                ? entity.getCollectionOrderEffectiveDate().toString()
                : null)
            .build();
    }

    static DefendantAccountResponse.EnforcementOverrideResponse buildUpdateEnforcementOverride(
        DefendantAccountEntity entity) {
        return DefendantAccountResponse.EnforcementOverrideResponse.builder()
            .enforcementOverrideResultId(entity.getEnforcementOverrideResultId())
            .enforcementOverrideEnforcerId(entity.getEnforcementOverrideEnforcerId() == null
                ? null : entity.getEnforcementOverrideEnforcerId())
            .enforcementOverrideTfoLjaId(entity.getEnforcementOverrideTfoLjaId() == null
                ? null : entity.getEnforcementOverrideTfoLjaId().intValue())
            .build();
    }

    static CourtReferenceCommon buildCourtReferenceCommon(CourtEntity.Lite court) {
        return Optional.ofNullable(court)
            .map(c -> CourtReferenceCommon.builder()
                .courtId(c.getCourtId())
                .courtCode(c.getCourtCode().intValue())
                .courtName(c.getName())
                .build())
            .orElse(null);
    }

    /**
     * Determines if the individual is considered a youth (under 18 years old).
     *
     * <p>
     * If the birth date is provided, it calculates the age based on the current date. If the birth date is not
     * provided, the age parameter is used if available. If neither is available, it returns null.
     * </p>
     *
     * @param birthDate The birth date of the individual.
     * @param age       Age of the individual.
     * @return True if the individual is under 18, false otherwise.
     */
    static Boolean isYouth(LocalDateTime birthDate, Short age) {
        if (birthDate != null) {
            return calculateAge(birthDate.toLocalDate()) < 18;
        } else if (age != null) {
            return age < 18;
        } else {
            return Boolean.FALSE; // return FALSE if both are null
        }
    }

    static int calculateAge(LocalDate birthDate) {
        return birthDate != null
            ? java.time.Period.between(birthDate, LocalDate.now()).getYears()
            : 0;
    }

    static DefendantAccountAtAGlanceResponse buildAtAGlanceResponse(
        DefendantAccountSummaryViewEntity entity) {

        if (null == entity) {
            return null;
        }

        return DefendantAccountAtAGlanceResponse.builder()
            .defendantAccountId(entity.getDefendantAccountId().toString())
            .accountNumber(entity.getAccountNumber())
            .debtorType(entity.getDebtorType())
            .isYouth(isYouth(entity.getBirthDate(), entity.getAge()))
            .partyDetails(buildPartyDetails(entity))
            .addressDetails(buildAddress(entity))
            .languagePreferences(buildLanguagePreferences(entity))
            .paymentTermsSummary(buildPaymentTerms(entity))
            .enforcementStatus(buildEnforcementStatusSummary(entity))
            .commentsAndNotes(buildCommentsAndNotes(entity))
            .version(entity.getVersion())
            .build();
    }

    static GetDefendantAccountPaymentTermsResponse buildPaymentTermsResponse(PaymentTermsEntity entity) {
        if (entity == null) {
            return null;
        }

        DefendantAccountEntity account = entity.getDefendantAccount();

        return GetDefendantAccountPaymentTermsResponse.builder()
            .version(Optional.ofNullable(account.getVersion()).orElse(BigInteger.ONE))
            .paymentTerms(buildPaymentTerms(entity, account))
            .paymentCardLastRequested(account.getPaymentCardRequestedDate())
            .lastEnforcement(account.getLastEnforcement())
            .build();
    }

    static NoteEntity buildNoteEntity(DefendantAccountEntity managed, String combined, String postedBy) {
        return NoteEntity.builder()
            .noteText(combined)
            .noteType("AA")
            .associatedRecordId(String.valueOf(managed.getDefendantAccountId()))
            .associatedRecordType(RecordType.DEFENDANT_ACCOUNTS.toString())
            .businessUnitUserId(String.valueOf(managed.getBusinessUnit().getBusinessUnitId()))
            .postedDate(LocalDateTime.now())
            .postedByUsername(postedBy)
            .build();
    }

    public static DefendantAccountTypeEnum determineAccountType(DefendantAccountPartiesEntity defendantParty) {
        boolean defendantIsCompany = defendantParty.getParty().isOrganisation();
        boolean defendantIsAdult = (!defendantIsCompany) && isNotYouth(defendantParty.getParty());

        return defendantIsCompany ? DefendantAccountTypeEnum.COMPANY :
            defendantIsAdult ? DefendantAccountTypeEnum.ADULT : DefendantAccountTypeEnum.YOUTH;
    }

    public static boolean isNotYouth(PartyEntity party) {
        return !isYouth(party.getBirthDate().atStartOfDay(), party.getAge());
    }

    public static DefendantAccountPartiesEntity filterDefendantParty(DefendantAccountEntity account) {
        return account.getParties().stream()
            .filter(p ->
                p.getAssociationType().equalsIgnoreCase("Defendant"))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Party not found for Defendant Account Id: " + account.getDefendantAccountId()));
    }


    static void applyCollectionOrder(DefendantAccountEntity entity,
        UpdateDefendantAccountRequest.CollectionOrderRequest co) {
        if (co.getCollectionOrder() == null) {
            throw new IllegalArgumentException("collection_order is required");
        }
        entity.setCollectionOrder(co.getCollectionOrder());
        if (Boolean.TRUE.equals(co.getCollectionOrder())) {
            if (co.getCollectionOrderDate() == null) {
                entity.setCollectionOrderEffectiveDate(LocalDate.now());
                return;
            }
        }

        if (co.getCollectionOrderDate() != null) {
            try {
                entity.setCollectionOrderEffectiveDate(LocalDate.parse(co.getCollectionOrderDate()));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("collection_order_date must be ISO date (yyyy-MM-dd)", ex);
            }
        }
    }

    static void applyEnforcementOverride(DefendantAccountEntity entity,
        UpdateDefendantAccountRequest.EnforcementOverrideRequest override) {
        entity.setEnforcementOverrideResultId(override.getEnforcementOverrideResultId());
        entity.setEnforcementOverrideEnforcerId(override.getEnforcementOverrideEnforcerId());
        entity.setEnforcementOverrideTfoLjaId(override.getEnforcementOverrideTfoLjaId() == null
            ? null : override.getEnforcementOverrideTfoLjaId().shortValue());
    }

    static Long safeParseLong(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static Short safeParseShort(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            int i = Integer.parseInt(s.trim());
            if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) {
                return null;
            }
            return (short) i;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static LocalDate safeParseLocalDate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim()); // ISO yyyy-MM-dd
        } catch (Exception ex) {
            return null;
        }

    }

    static void applyPartyCoreReplace(PartyEntity party, PartyDetails details) {

        Boolean orgFlag = details.getOrganisationFlag();
        party.setOrganisation(orgFlag);

        if (orgFlag) {
            OrganisationDetails od = details.getOrganisationDetails();
            if (od != null) {
                party.setOrganisationName(od.getOrganisationName());
            } else {
                party.setOrganisationName(null);
            }
            party.setTitle(null);
            party.setForenames(null);
            party.setSurname(null);
            party.setBirthDate(null);
            party.setAge(null);
            party.setNiNumber(null);
        } else {
            IndividualDetails id = details.getIndividualDetails();
            if (id != null) {
                party.setTitle(id.getTitle());
                party.setForenames(id.getForenames());
                party.setSurname(id.getSurname());
                party.setBirthDate(safeParseLocalDate(id.getDateOfBirth()));
                party.setAge(safeParseShort(id.getAge()));
                party.setNiNumber(id.getNationalInsuranceNumber());
            } else {
                party.setTitle(null);
                party.setForenames(null);
                party.setSurname(null);
                party.setBirthDate(null);
                party.setAge(null);
                party.setNiNumber(null);
            }
            party.setOrganisationName(null);
        }
    }

    static void applyPartyAddressReplace(PartyEntity party, AddressDetails a) {
        if (a == null) {
            party.setAddressLine1(null);
            party.setAddressLine2(null);
            party.setAddressLine3(null);
            party.setAddressLine4(null);
            party.setAddressLine5(null);
            party.setPostcode(null);
            return;
        }
        party.setAddressLine1(a.getAddressLine1());
        party.setAddressLine2(a.getAddressLine2());
        party.setAddressLine3(a.getAddressLine3());
        party.setAddressLine4(a.getAddressLine4());
        party.setAddressLine5(a.getAddressLine5());
        party.setPostcode(a.getPostcode());
    }

    static void applyPartyContactReplace(PartyEntity party, ContactDetails c) {
        if (c == null) {
            party.setPrimaryEmailAddress(null);
            party.setSecondaryEmailAddress(null);
            party.setMobileTelephoneNumber(null);
            party.setHomeTelephoneNumber(null);
            party.setWorkTelephoneNumber(null);
            return;
        }
        party.setPrimaryEmailAddress(c.getPrimaryEmailAddress());
        party.setSecondaryEmailAddress(c.getSecondaryEmailAddress());
        party.setMobileTelephoneNumber(c.getMobileTelephoneNumber());
        party.setHomeTelephoneNumber(c.getHomeTelephoneNumber());
        party.setWorkTelephoneNumber(c.getWorkTelephoneNumber());
    }

    static GetDefendantAccountFixedPenaltyResponse toFixedPenaltyResponse(
        DefendantAccountEntity account, FixedPenaltyOffenceEntity offence) {

        boolean isVehicle =
            offence.getVehicleRegistration() != null
                && !"NV".equalsIgnoreCase(offence.getVehicleRegistration());

        FixedPenaltyTicketDetails ticketDetails = FixedPenaltyTicketDetails.builder()
            .issuingAuthority(account.getOriginatorName())
            .ticketNumber(offence.getTicketNumber())
            .timeOfOffence(
                offence.getTimeOfOffence() != null
                    ? offence.getTimeOfOffence().toString()
                    : null
            )
            .placeOfOffence(offence.getOffenceLocation())
            .build();

        VehicleFixedPenaltyDetails vehicleDetails = isVehicle
            ? VehicleFixedPenaltyDetails.builder()
            .vehicleRegistrationNumber(offence.getVehicleRegistration())
            .vehicleDriversLicense(offence.getLicenceNumber())
            .noticeNumber(offence.getNoticeNumber())
            .dateNoticeIssued(DateTimeUtils.toString(offence.getIssuedDate()))
            .build()
            : null;
        return GetDefendantAccountFixedPenaltyResponse.builder()
            .vehicleFixedPenaltyFlag(isVehicle)
            .fixedPenaltyTicketDetails(ticketDetails)
            .vehicleFixedPenaltyDetails(vehicleDetails)
            .version(account.getVersion())
            .build();
    }

    static DefendantAccountHeaderSummary mapToDto(DefendantAccountHeaderViewEntity e) {
        return DefendantAccountHeaderSummary.builder()
            .defendantAccountId(
                e.getDefendantAccountId() != null ? e.getDefendantAccountId().toString() : null
            )
            .defendantAccountPartyId(
                e.getDefendantAccountPartyId() != null ? e.getDefendantAccountPartyId().toString() : null
            )
            .debtorType(
                e.getDebtorType() != null
                    ? e.getDebtorType()
                    : (Boolean.TRUE.equals(e.getHasParentGuardian()) ? "Parent/Guardian" : "Defendant")
            )
            .isYouth(
                e.getBirthDate() != null
                    ? java.time.Period.between(e.getBirthDate(), java.time.LocalDate.now()).getYears() < 18
                    : Boolean.FALSE
            )
            .parentGuardianPartyId(Optional.ofNullable(e.getParentGuardianAccountPartyId())
                .map(Object::toString).orElse(null))
            .accountNumber(e.getAccountNumber())
            .accountType(normaliseAccountType(e.getAccountType()))
            .prosecutorCaseReference(e.getProsecutorCaseReference())
            .fixedPenaltyTicketNumber(e.getFixedPenaltyTicketNumber())
            .accountStatusReference(buildAccountStatusReference(e.getAccountStatus()))
            .businessUnitSummary(OpalDefendantAccountBuilders.buildBusinessUnitSummary(e))
            .paymentStateSummary(OpalDefendantAccountBuilders.buildPaymentStateSummary(e))
            .partyDetails(buildPartyDetails(e))
            .version(BigInteger.valueOf(e.getVersion()))
            .build();
    }

    record ParsedAlias(
        String aliasId,
        Integer sequenceNumber,
        String forenames,
        String surname,
        String organisationName
    ) {
    }

}
