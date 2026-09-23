package uk.gov.hmcts.opal.mapper.till;

import static uk.gov.hmcts.opal.entity.DestinationType.F;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.generated.model.TillsDefendantDetail;
import uk.gov.hmcts.opal.generated.model.TillsIndividualNames;
import uk.gov.hmcts.opal.generated.model.TillsOrganisationNames;
import uk.gov.hmcts.opal.generated.model.TillsPartyDetails;

@Mapper(componentModel = "spring")
public abstract class GetTillDefendantMapper {

    @Named("resolveDefendantDetail")
    public JsonNullable<TillsDefendantDetail> resolveDefendantDetail(
        PaymentInEntity payment, @Context Map<Long, DefendantAccountEntity> defendantAccounts) {

        if (payment.getDestinationType() != F) {
            return JsonNullable.of(null);
        }

        Long defendantAccountId = Long.valueOf(payment.getAssociatedRecordId());
        DefendantAccountEntity account = defendantAccounts.get(defendantAccountId);
        if (account == null) {
            throw new EntityNotFoundException("Defendant account not found for associated_record_id: "
                + defendantAccountId);
        }

        PartyEntity defendant = account.getParties().stream()
            .filter(party -> party.getAssociationType() == AssociationType.DEFENDANT)
            .map(DefendantAccountPartiesEntity::getParty)
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant party not found for defendant_account_id: " + defendantAccountId));

        return JsonNullable.of(toDefendantDetail(account, defendant));
    }

    @Mapping(target = "defendantAccountNumber", source = "account.accountNumber")
    @Mapping(target = "partyDetails", source = "party")
    protected abstract TillsDefendantDetail toDefendantDetail(
        DefendantAccountEntity account, PartyEntity party);

    @Mapping(target = "organisationFlag", source = "organisation")
    @Mapping(target = "individualNames", source = ".", qualifiedByName = "resolveIndividualNames")
    @Mapping(target = "organisationNames", source = ".", qualifiedByName = "resolveOrganisationNames")
    protected abstract TillsPartyDetails toPartyDetails(PartyEntity party);

    @Named("resolveIndividualNames")
    protected JsonNullable<TillsIndividualNames> resolveIndividualNames(PartyEntity party) {
        return party.isOrganisation() ? JsonNullable.undefined() : JsonNullable.of(toIndividualNames(party));
    }

    protected abstract TillsIndividualNames toIndividualNames(PartyEntity party);

    @Named("resolveOrganisationNames")
    protected JsonNullable<TillsOrganisationNames> resolveOrganisationNames(PartyEntity party) {
        return party.isOrganisation() ? JsonNullable.of(toOrganisationNames(party)) : JsonNullable.undefined();
    }

    protected abstract TillsOrganisationNames toOrganisationNames(PartyEntity party);
}
