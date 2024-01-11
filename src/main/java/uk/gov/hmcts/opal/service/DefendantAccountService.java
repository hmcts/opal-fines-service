package uk.gov.hmcts.opal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummary;
import uk.gov.hmcts.opal.entity.EnforcersEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcersRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;

import uk.gov.hmcts.opal.entity.DefendantAccountSummary.PartyLink;
import uk.gov.hmcts.opal.entity.DefendantAccountSummary.PartyDefendantAccountSummary;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.opal.dto.ToJsonString.newObjectMapper;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DefendantAccountService {

    private final DefendantAccountRepository defendantAccountRepository;

    private final DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    private final PaymentTermsRepository paymentTermsRepository;

    private final DebtorDetailRepository debtorDetailRepository;

    private final EnforcersRepository enforcersRepository;

    private final NoteRepository noteRepository;

    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {

        return defendantAccountRepository.findByBusinessUnitId_BusinessUnitIdAndAccountNumber(
            request.getBusinessUnitId(), request.getAccountNumber());
    }

    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {

        return defendantAccountRepository.save(defendantAccountEntity);
    }

    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {

        log.info(":getDefendantAccountsByBusinessUnit: busUnit: {}", businessUnitId);
        return defendantAccountRepository.findAllByBusinessUnitId_BusinessUnitId(businessUnitId);
    }

    public AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {

        if ("test".equalsIgnoreCase(accountSearchDto.getCourt())) {

            try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tempSearchData.json")) {
                ObjectMapper mapper = newObjectMapper();
                AccountSearchResultsDto dto = mapper.readValue(in, AccountSearchResultsDto.class);
                log.info(":searchDefendantAccounts: temporary Hack for Front End testing. Read JSON file: \n{}",
                         dto.toPrettyJsonString());
                return dto;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Page<DefendantAccountSummary> summariesPage = defendantAccountRepository
            .findBy(DefendantAccountSpecs.findByAccountSearch(accountSearchDto),
                    ffq -> ffq.as(DefendantAccountSummary.class).page(Pageable.unpaged()));

        List<AccountSummaryDto> dtos = summariesPage.getContent().stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return AccountSearchResultsDto.builder()
            .searchResults(dtos)
            .totalCount(summariesPage.getTotalElements())
            .cursor(summariesPage.getNumber())
            .build();
    }

    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {


        if (defendantAccountId.equals(0L)) {


            try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tempDetailsData.json")) {

                ObjectMapper mapper = newObjectMapper();
                AccountDetailsDto dto = mapper.readValue(in, AccountDetailsDto.class);
                log.info(
                        """
                        :getAccountDetailsByDefendantAccountId:
                        " temporary Hack for Front End testing. Read JSON file: \n{}
                        """,
                         dto.toPrettyJsonString());
                return dto;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        //query db for defendantAccountPartiesEntity
        DefendantAccountPartiesEntity defendantAccountPartiesEntity = defendantAccountPartiesRepository
            .findByDefendantAccountDefendantAccountId(defendantAccountId);

        //Extract unique defendantAccount and party entities
        final DefendantAccountEntity defendantAccountEntity = defendantAccountPartiesEntity.getDefendantAccount();
        final PartyEntity partyEntity = defendantAccountPartiesEntity.getParty();


        //query DB for PaymentTermsEntity
        PaymentTermsEntity paymentTermsEntity = paymentTermsRepository.findByDefendantAccount_DefendantAccountId(
            defendantAccountEntity.getDefendantAccountId());

        //query DB for EnforcementEntity
        EnforcersEntity enforcersEntity = enforcersRepository.findByEnforcerId(defendantAccountEntity
                                                                                   .getEnforcementOverrideEnforcerId());

        //query DB for NoteEntity by associatedRecordId (defendantAccountId) and noteType ("AC")
        List<NoteEntity> noteEntity = noteRepository.findByAssociatedRecordIdAndNoteType(
            defendantAccountEntity.getDefendantAccountId().toString(), "AC");


        //build fullAddress
        final String fullAddress = buildFullAddress(partyEntity);

        //build organisationOrFullName
        final String organisationOrFullName = getOrganisationOrFullName(partyEntity);

        //build paymentDetails
        final String paymentDetails = buildPaymentDetails(paymentTermsEntity);

        //build comments
        final List<String> comments = buildCommentsFromAssociatedNotes(noteEntity);

        //populate accountDetailsDto and return
        return AccountDetailsDto.builder()
            .defendantAccountId(defendantAccountEntity.getDefendantAccountId())
            .accountNumber(defendantAccountEntity.getAccountNumber())
            .fullName(organisationOrFullName)
            .accountCT(defendantAccountEntity.getBusinessUnitId().getBusinessUnitName())
            .accountType(defendantAccountEntity.getOriginatorType())
            .address(fullAddress)
            .postCode(partyEntity.getPostcode())
            .dob(partyEntity.getDateOfBirth())
            .detailsChanged(defendantAccountEntity.getLastChangedDate())
            .lastCourtAppAndCourtCode(defendantAccountEntity.getLastHearingDate().toString()
                                          + " " + defendantAccountEntity.getLastHearingCourtId().getCourtCode())
            .lastMovement(defendantAccountEntity.getLastMovementDate())
            .commentField(comments)
            .pcr(defendantAccountEntity.getProsecutorCaseReference())
            .paymentDetails(paymentDetails)
            .lumpSum(paymentTermsEntity.getInstalmentLumpSum())
            .commencing(paymentTermsEntity.getTermsTypeCode().equals("I")
                            ? paymentTermsEntity.getEffectiveDate()
                            : null)
            .daysInDefault(paymentTermsEntity.getJailDays())
            .lastEnforcement(defendantAccountEntity.getLastEnforcement())
            .override(defendantAccountEntity.getEnforcementOverrideResultId())
            .enforcer(enforcersEntity.getEnforcerCode())
            .enforcementCourt(defendantAccountEntity.getEnforcingCourtId().getCourtCode())
            .imposed(defendantAccountEntity.getAmountImposed())
            .amountPaid(defendantAccountEntity.getAmountPaid())
            .arrears(defendantAccountEntity.getAccountBalance())
            .balance(defendantAccountEntity.getAccountBalance())
            .build();
    }

    private String buildFullAddress(PartyEntity partyEntity) {
        List<String> addressLines = new ArrayList<>();

        addressLines.add(partyEntity.getAddressLine1());
        addressLines.add(partyEntity.getAddressLine2());
        addressLines.add(partyEntity.getAddressLine3());
        addressLines.add(partyEntity.getAddressLine4());
        addressLines.add(partyEntity.getAddressLine5());

        addressLines.removeIf(Objects::isNull);

        return String.join(", ", addressLines);
    }


    private String getOrganisationOrFullName(PartyEntity partyEntity) {

        if (partyEntity.getOrganisationName() != null) {

            return partyEntity.getOrganisationName();
        }

        return partyEntity.getFullName();
    }

    private String buildPaymentDetails(PaymentTermsEntity paymentTermsEntity) {
        final String paymentType = paymentTermsEntity.getTermsTypeCode();

        return switch (paymentType) {
            case "I" -> buildInstalmentDetails(paymentTermsEntity);
            case "B" -> buildByDateDetails(paymentTermsEntity);
            default -> "Paid";
        };
    }

    private String buildInstalmentDetails(PaymentTermsEntity paymentTermsEntity) {
        return paymentTermsEntity.getInstalmentAmount() + " / " + paymentTermsEntity.getInstalmentPeriod();
    }

    private String buildByDateDetails(PaymentTermsEntity paymentTermsEntity) {
        return paymentTermsEntity.getEffectiveDate() + " By Date";
    }

    private List<String> buildCommentsFromAssociatedNotes(List<NoteEntity> notes) {

        List<String> comments = new ArrayList<>();

        for (NoteEntity note : notes) {

            comments.add(note.getNoteText());
        }

        return comments;
    }

    public AccountSummaryDto toDto(DefendantAccountSummary summary) {
        Optional<PartyDefendantAccountSummary> party = summary.getParties().stream().findAny().map(PartyLink::getParty);
        return AccountSummaryDto.builder()
            .defendantAccountId(summary.getDefendantAccountId())
            .accountNo(summary.getAccountNumber())
            .court(summary.getImposingCourtId())
            .balance(summary.getAccountBalance())
            .name(party.map(PartyDefendantAccountSummary::getFullName).orElse(""))
            .addressLine1(party.map(PartyDefendantAccountSummary::getAddressLine1).orElse(""))
            .dateOfBirth(party.map(PartyDefendantAccountSummary::getDateOfBirth).orElse(null))
            .build();
    }
}
