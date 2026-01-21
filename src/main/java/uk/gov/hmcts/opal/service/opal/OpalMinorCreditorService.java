package uk.gov.hmcts.opal.service.opal;

import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.opal.dto.CreditorAccountDto;
import uk.gov.hmcts.opal.dto.DefendantDto;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
import uk.gov.hmcts.opal.repository.jpa.MinorCreditorSpecs;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j(topic = "opal.OpalMinorCreditorService")
@RequiredArgsConstructor
public class OpalMinorCreditorService implements MinorCreditorServiceInterface {

    private final MinorCreditorRepository minorCreditorRepository;
    private final MinorCreditorAccountHeaderRepository minorCreditorAccountHeaderRepository;

    private final MinorCreditorSpecs specs = new MinorCreditorSpecs();

    @Override
    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch criteria) {
        Specification<MinorCreditorEntity> spec =
            specs.findBySearchCriteria(criteria);

        List<MinorCreditorEntity> results =
            minorCreditorRepository.findAll(spec);

        return toResponse(results);
    }

    @Override
    public GetMinorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long minorCreditorAccountId) {
        log.debug(":getHeaderSummary (Opal): minorCreditorAccountId={}", minorCreditorAccountId);

        MinorCreditorAccountHeaderEntity entity = minorCreditorAccountHeaderRepository.findById(minorCreditorAccountId)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Minor creditor account not found: " + minorCreditorAccountId
            ));

        return toHeaderSummaryResponse(entity);
    }


    private CreditorAccountDto toCreditorAccountDto(MinorCreditorEntity entity) {
        return CreditorAccountDto.builder()
            .creditorAccountId(String.valueOf(entity.getCreditorId()))
            .accountNumber(entity.getAccountNumber())
            .organisation(entity.isOrganisation())
            .organisationName(entity.getOrganisationName()) // confirm if this is creditor org or defendant org
            .firstnames(entity.getForenames())
            .surname(entity.getSurname())
            .addressLine1(entity.getAddressLine1())
            .postcode(entity.getPostCode())
            .businessUnitName(entity.getBusinessUnitName())
            .businessUnitId(String.valueOf(entity.getBusinessUnitId()))
            .accountBalance(java.util.Optional.ofNullable(entity.getCreditorAccountBalance())
                                .map(BigDecimal::valueOf)
                                .orElse(BigDecimal.ZERO))
            .defendant(toDefendantDto(entity))
            .build();
    }

    private DefendantDto toDefendantDto(MinorCreditorEntity entity) {
        return DefendantDto.builder()
            .defendantAccountId(entity.getDefendantAccountId() != null
                                    ? String.valueOf(entity.getDefendantAccountId()) : null)
            .organisation(entity.isOrganisation()) // or map from a column if you have a defendant_organisation flag
            .organisationName(entity.getDefendantOrganisationName())
            .firstnames(entity.getDefendantFornames())
            .surname(entity.getDefendantSurname())
            .build();
    }

    private PostMinorCreditorAccountsSearchResponse toResponse(List<MinorCreditorEntity> entities) {
        List<CreditorAccountDto> accounts = entities.stream()
            .map(this::toCreditorAccountDto)
            .toList();

        return PostMinorCreditorAccountsSearchResponse.builder()
            .count(accounts.size())
            .creditorAccounts(accounts.isEmpty() ? null : accounts)
            .build();
    }

    private GetMinorCreditorAccountHeaderSummaryResponse toHeaderSummaryResponse(MinorCreditorAccountHeaderEntity e) {
        return GetMinorCreditorAccountHeaderSummaryResponse.builder()
            .creditorAccountId(String.valueOf(e.getCreditorAccountId()))
            .accountNumber(e.getCreditorAccountNumber())
            .creditorAccountType(e.getCreditorAccountType())
            .version(e.getVersionNumber() == null ? null : BigInteger.valueOf(e.getVersionNumber()))
            .businessUnitSummary(toBusinessUnitSummary(e))
            .partyDetails(toPartyDetails(e))
            .awardedAmount(e.getAwarded())
            .paidOutAmount(e.getPaidOut())
            .awaitingPayoutAmount(e.getAwaitingPayment())
            .outstandingAmount(e.getOutstanding())
            .hasAssociatedDefendant(hasAssociatedDefendant(e))
            .build();
    }

    private BusinessUnitSummary toBusinessUnitSummary(MinorCreditorAccountHeaderEntity e) {
        return BusinessUnitSummary.builder()
            .businessUnitId(String.valueOf(e.getBusinessUnitId()))
            .businessUnitName(e.getBusinessUnitName())
            .welshSpeaking(e.isWelshLanguage() ? "Y" : "N")
            .build();
    }

    private PartyDetails toPartyDetails(MinorCreditorAccountHeaderEntity e) {
        boolean isOrg = e.isOrganisation();
        return PartyDetails.builder()
            .partyId(String.valueOf(e.getPartyId()))
            .organisationFlag(isOrg)
            .organisationDetails(isOrg ? OrganisationDetails.builder()
                .organisationName(e.getOrganisationName())
                .build() : null)
            .individualDetails(!isOrg ? IndividualDetails.builder()
                .title(e.getTitle())
                .forenames(e.getForenames())
                .surname(e.getSurname())
                .build() : null)
            .build();
    }

    private static boolean hasAssociatedDefendant(MinorCreditorAccountHeaderEntity e) {
        return (e.getAwarded() != null && e.getAwarded().signum() > 0)
            || (e.getOutstanding() != null && e.getOutstanding().signum() > 0);
    }

}
