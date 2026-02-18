package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditorPayment;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.service.iface.MinorCreditorAccountServiceInterface;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalMinorCreditorAccountService")
@RequiredArgsConstructor
public class OpalMinorCreditorAccountService implements MinorCreditorAccountServiceInterface {

    private final CreditorAccountRepository creditorAccountRepository;
    private final PartyRepository partyRepository;
    private final AmendmentService amendmentService;

    @Override
    @Transactional
    public MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorAccountId,
        PatchMinorCreditorAccountRequest request,
        BigInteger etag,
        String postedBy) {
        log.debug(":updateMinorCreditorAccount (Opal): id={}", minorCreditorAccountId);

        if (request == null || request.getPayment() == null || request.getPayment().getHoldPayment() == null) {
            throw new IllegalArgumentException("Payment group must be provided");
        }

        CreditorAccountEntity.Lite entity = creditorAccountRepository.findById(minorCreditorAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId));

        if (entity.getCreditorAccountType() == null || !entity.getCreditorAccountType().isMinorCreditor()) {
            throw new EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId);
        }

        VersionUtils.verifyIfMatch(entity, etag, minorCreditorAccountId, "updateMinorCreditorAccount");

        amendmentService.auditInitialiseStoredProc(minorCreditorAccountId, RecordType.CREDITOR_ACCOUNTS);

        entity.setHoldPayout(request.getPayment().getHoldPayment());
        entity.setVersionNumber(entity.getVersion().add(BigInteger.ONE).longValueExact());
        creditorAccountRepository.save(entity);

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
}
