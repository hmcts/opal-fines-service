package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.CreditorAccountDto;
import uk.gov.hmcts.opal.dto.DefendantDto;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditorPayment;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.jpa.MinorCreditorSpecs;
import uk.gov.hmcts.opal.service.iface.MinorCreditorAccountServiceInterface;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalMinorCreditorService")
@RequiredArgsConstructor
public class OpalMinorCreditorService implements MinorCreditorServiceInterface, MinorCreditorAccountServiceInterface {

    private final MinorCreditorRepository minorCreditorRepository;
    private final MinorCreditorAccountHeaderRepository minorCreditorAccountHeaderRepository;

    private final CreditorAccountRepository creditorAccountRepository;
    private final PartyRepository partyRepository;
    private final AmendmentService amendmentService;
    private final EntityManager em;

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

        MinorCreditorAccountHeaderEntity entity =
            minorCreditorAccountHeaderRepository.findById(minorCreditorAccountId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Minor creditor account not found: " + minorCreditorAccountId
                ));

        return GetMinorCreditorAccountHeaderSummaryResponse.fromEntity(entity);
    }

    @Override
    @Transactional
    public MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorAccountId,
        PatchMinorCreditorAccountRequest request,
        BigInteger etag,
        String postedBy) {
        log.debug(":updateMinorCreditorAccount (Opal): id={}", minorCreditorAccountId);

        if (request == null || request.getPayment() == null || request.getPayment().getHoldPayment() == null) {
            throw new IllegalArgumentException("payment group must be provided");
        }

        CreditorAccountEntity.Lite entity = creditorAccountRepository.findById(minorCreditorAccountId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId));

        if (entity.getCreditorAccountType() == null || !entity.getCreditorAccountType().isMinorCreditor()) {
            throw new jakarta.persistence.EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId);
        }

        VersionUtils.verifyIfMatch(entity, etag, minorCreditorAccountId, "updateMinorCreditorAccount");

        amendmentService.auditInitialiseStoredProc(minorCreditorAccountId, RecordType.CREDITOR_ACCOUNTS);

        entity.setHoldPayout(request.getPayment().getHoldPayment());
        creditorAccountRepository.save(entity);

        em.lock(entity, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        em.flush();

        BigInteger newVersion = entity.getVersion();

        amendmentService.auditFinaliseStoredProc(
            minorCreditorAccountId,
            RecordType.CREDITOR_ACCOUNTS,
            entity.getBusinessUnitId(),
            postedBy,
            null,
            "ACCOUNT_ENQUIRY"
        );

        MinorCreditorAccountResponse response = buildMinorCreditorAccountResponse(entity);
        response.setVersion(newVersion);
        return response;
    }

    private MinorCreditorAccountResponse buildMinorCreditorAccountResponse(
        CreditorAccountEntity.Lite account) {
        PartyEntity party = partyRepository.findById(account.getMinorCreditorPartyId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Party not found for minor creditor account: " + account.getCreditorAccountId()));

        PartyDetailsCommon partyDetails = new PartyDetailsCommon()
            .partyId(String.valueOf(party.getPartyId()))
            .organisationFlag(party.isOrganisation());

        if (party.isOrganisation()) {
            partyDetails.organisationDetails(new OrganisationDetailsCommon()
                                                 .organisationName(party.getOrganisationName()));
        } else {
            partyDetails.individualDetails(new IndividualDetailsCommon()
                                               .surname(party.getSurname())
                                               .forenames(party.getForenames())
                                               .title(party.getTitle()));
        }

        AddressDetailsCommon address = new AddressDetailsCommon()
            .addressLine1(party.getAddressLine1())
            .addressLine2(party.getAddressLine2())
            .addressLine3(party.getAddressLine3())
            .addressLine4(party.getAddressLine4())
            .addressLine5(party.getAddressLine5())
            .postcode(party.getPostcode());

        MinorCreditorAccountResponseMinorCreditorPayment payment =
            new MinorCreditorAccountResponseMinorCreditorPayment()
            .accountName(account.getBankAccountName())
            .sortCode(account.getBankSortCode())
            .accountNumber(account.getBankAccountNumber())
            .accountReference(account.getBankAccountReference())
            .payByBacs(account.isPayByBacs())
            .holdPayment(account.isHoldPayout());

        return (MinorCreditorAccountResponse) new MinorCreditorAccountResponse()
            .creditorAccountId(account.getCreditorAccountId())
            .partyDetails(partyDetails)
            .address(address)
            .payment(payment);
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

}
