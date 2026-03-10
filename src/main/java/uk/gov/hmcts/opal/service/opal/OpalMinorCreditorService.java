package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
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
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountUpdateMapper;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.jpa.MinorCreditorSpecs;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalMinorCreditorService")
@RequiredArgsConstructor
public class OpalMinorCreditorService implements MinorCreditorServiceInterface {

    private final MinorCreditorRepository minorCreditorRepository;
    private final MinorCreditorAccountHeaderRepository minorCreditorAccountHeaderRepository;
    private final CreditorAccountRepository creditorAccountRepository;
    private final PartyRepository partyRepository;
    private final AmendmentService amendmentService;
    private final MinorCreditorAccountUpdateMapper updateMapper;
    private final MinorCreditorAccountResponseMapper responseMapper;

    private final MinorCreditorSpecs specs = new MinorCreditorSpecs();

    @Override
    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch criteria) {
        Specification<MinorCreditorEntity> spec = specs.findBySearchCriteria(criteria);
        List<MinorCreditorEntity> results = minorCreditorRepository.findAll(spec);
        return toResponse(results);
    }

    @Override
    public GetMinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(String minorCreditorId) {

        // To do as a part of PO-1914
        throw new UnsupportedOperationException("Opal endpoint not supported yet.");
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

        if (request == null
            || request.getPayment() == null
            || request.getPayment().getHoldPayment() == null
            || request.getPartyDetails() == null
            || request.getAddress() == null) {
            throw new IllegalArgumentException("Payment, party_details and address groups must be provided");
        }

        CreditorAccountEntity.Lite creditorAccount = creditorAccountRepository.findById(minorCreditorAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId));

        if (creditorAccount.getCreditorAccountType() == null || !creditorAccount.getCreditorAccountType()
            .isMinorCreditor()) {
            throw new EntityNotFoundException("Account is not a minor creditor account: " + minorCreditorAccountId);
        }
        if (creditorAccount.getVersion() == null) {
            throw new ResourceConflictException("CreditorAccount", minorCreditorAccountId,
                "Current account version is missing", null);
        }
        VersionUtils.verifyIfMatch(creditorAccount, etag, minorCreditorAccountId, "updateMinorCreditorAccount");

        PartyEntity party = partyRepository.findById(creditorAccount.getMinorCreditorPartyId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Party not found for minor creditor account: " + creditorAccount.getCreditorAccountId()));

        validatePartyId(request.getPartyDetails().getPartyId(), party.getPartyId());

        amendmentService.auditInitialiseStoredProc(minorCreditorAccountId, RecordType.CREDITOR_ACCOUNTS);

        updateMapper.updateParty(request.getPartyDetails(), request.getAddress(), party);

        creditorAccount.setHoldPayout(request.getPayment().getHoldPayment());

        partyRepository.save(party);
        creditorAccountRepository.saveAndFlush(creditorAccount);

        amendmentService.auditFinaliseStoredProc(
            minorCreditorAccountId,
            RecordType.CREDITOR_ACCOUNTS,
            creditorAccount.getBusinessUnitId(),
            postedBy,
            null,
            "ACCOUNT_ENQUIRY"
        );

        MinorCreditorAccountResponse response = responseMapper.toMinorCreditorAccountResponse(creditorAccount, party);
        response.setVersion(creditorAccount.getVersion());
        return response;
    }

    private CreditorAccountDto toCreditorAccountDto(MinorCreditorEntity entity) {
        return CreditorAccountDto.builder()
            .creditorAccountId(String.valueOf(entity.getCreditorId()))
            .accountNumber(entity.getAccountNumber())
            .organisation(entity.isOrganisation())
            .organisationName(entity.getOrganisationName())
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
            .organisation(entity.isOrganisation())
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

    private void validatePartyId(String requestPartyId, Long existingPartyId) {
        if (requestPartyId == null || requestPartyId.isBlank()) {
            throw new IllegalArgumentException("party_details.party_id must be provided");
        }
        try {
            if (!Long.valueOf(requestPartyId).equals(existingPartyId)) {
                throw new IllegalArgumentException("party_details.party_id does not match account");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid party_details.party_id format", ex);
        }
    }
}
