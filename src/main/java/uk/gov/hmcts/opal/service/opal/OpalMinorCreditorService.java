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
import uk.gov.hmcts.opal.dto.UpdateMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
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
    public MinorCreditorAccountResponse updateMinorCreditorAccount(Long minorCreditorAccountId,
                                                                   UpdateMinorCreditorAccountRequest request,
                                                                   String ifMatch,
                                                                   String postedBy) {
        log.debug(":updateMinorCreditorAccount (Opal): id={}", minorCreditorAccountId);

        if (request == null || request.getPayoutHold() == null || request.getPayoutHold().getPayoutHold() == null) {
            throw new IllegalArgumentException("payout_hold group must be provided");
        }

        CreditorAccountEntity.Lite entity = creditorAccountRepository.findById(minorCreditorAccountId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId));

        if (entity.getCreditorAccountType() == null || !entity.getCreditorAccountType().isMinorCreditor()) {
            throw new jakarta.persistence.EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId);
        }

        VersionUtils.verifyIfMatch(entity, ifMatch, minorCreditorAccountId, "updateMinorCreditorAccount");

        amendmentService.auditInitialiseStoredProc(minorCreditorAccountId, RecordType.CREDITOR_ACCOUNTS);

        entity.setHoldPayout(request.getPayoutHold().getPayoutHold());
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

        return MinorCreditorAccountResponse.builder()
            .creditorAccountId(entity.getCreditorAccountId())
            .payoutHold(MinorCreditorAccountResponse.PayoutHold.builder()
                            .payoutHold(entity.isHoldPayout())
                            .build())
            .version(newVersion)
            .build();
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
