package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountService implements DefendantAccountServiceInterface {

    private final DefendantAccountRepository defendantAccountRepository;
    private final DefendantAccountSpecs defendantAccountSpecs;

    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        log.debug(":getHeaderSummary: id: {} - NOT YET IMPLEMENTED.", defendantAccountId);
        // TODO: implement this when Opal mode is supported
        throw new EntityNotFoundException("Defendant Account not found with id: " + defendantAccountId);
    }

    @Override
    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.debug(":searchDefendantAccounts (Opal): criteria: {}", accountSearchDto);

        Specification<DefendantAccountEntity> spec =
            defendantAccountSpecs.filterByBusinessUnits(accountSearchDto.getBusinessUnitIds())
                .and(defendantAccountSpecs.filterByActiveOnly(accountSearchDto.getActiveAccountsOnly()))
                .and(defendantAccountSpecs.filterByAccountNumberStartsWithWithCheckLetter(accountSearchDto))
                .and(defendantAccountSpecs.filterByPcrExact(accountSearchDto))
                .and(defendantAccountSpecs.filterByNameIncludingAliases(accountSearchDto))
                .and(defendantAccountSpecs.filterByAliasesIfRequested(accountSearchDto))
                .and(defendantAccountSpecs.filterByDobStartsWith(accountSearchDto))
                .and(defendantAccountSpecs.filterByNiStartsWith(accountSearchDto))
                .and(defendantAccountSpecs.filterByAddress1StartsWith(accountSearchDto))
                .and(defendantAccountSpecs.filterByPostcodeStartsWith(accountSearchDto));


        List<DefendantAccountEntity> rows = defendantAccountRepository.findAll(spec);

        List<DefendantAccountSummaryDto> summaries = new ArrayList<>(rows.size());
        for (DefendantAccountEntity e : rows) {
            summaries.add(toSummaryDto(e));
        }

        return DefendantAccountSearchResultsDto.builder()
                .defendantAccounts(summaries)
                .count(summaries.size())
                .build();
    }

    private DefendantAccountSummaryDto toSummaryDto(DefendantAccountEntity e) {
        PartyEntity party = Optional.ofNullable(e.getParties())
            .flatMap(list -> list.stream()
                .map(DefendantAccountPartiesEntity::getParty)
                .findFirst())
            .orElse(null);

        boolean isOrganisation = party != null && party.isOrganisation();
        String organisationName = party != null ? party.getOrganisationName() : null;
        String title = party != null ? party.getTitle() : null;
        String forenames = party != null ? party.getForenames() : null;
        String surname = party != null ? party.getSurname() : null;

        List<AliasDto> aliases = Optional.ofNullable(party)
            .map(PartyEntity::getAliasEntities) // Get aliasEntities from PartyEntity
            .orElseGet(List::of) // Return an empty list if aliasEntities is null
            .stream()
            .map(a -> AliasDto.builder()
                .aliasNumber(a.getSequenceNumber()) // Map sequenceNumber to aliasNumber
                .organisationName(a.getOrganisationName()) // Map organisationName
                .surname(a.getSurname()) // Map surname
                .forenames(a.getForenames()) // Map forenames
                .build())
            .toList();

        return DefendantAccountSummaryDto.builder()
            .defendantAccountId(String.valueOf(e.getDefendantAccountId()))
            .accountNumber(e.getAccountNumber())
            .organisation(isOrganisation)
            .organisationName(organisationName)
            .defendantTitle(!isOrganisation ? title : null)
            .defendantFirstnames(!isOrganisation ? forenames : null)
            .defendantSurname(!isOrganisation ? surname : null)
            .addressLine1(party != null ? party.getAddressLine1() : null)
            .postcode(party != null ? party.getPostcode() : null)
            .businessUnitName(e.getBusinessUnit() != null ? e.getBusinessUnit().getBusinessUnitName() : null)
            .businessUnitId(e.getBusinessUnit() != null
                ? String.valueOf(e.getBusinessUnit().getBusinessUnitId()) : null)
            .prosecutorCaseReference(e.getProsecutorCaseReference())
            .lastEnforcementAction(e.getLastEnforcement())
            .accountBalance(e.getAccountBalance())
            .birthDate(party != null && !isOrganisation
                ? uk.gov.hmcts.opal.util.DateTimeUtils.toString(party.getDateOfBirth())
                : null)
            .aliases(aliases)
            .build();
    }

}
